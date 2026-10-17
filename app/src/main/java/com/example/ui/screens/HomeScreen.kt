package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.DailyEarning
import com.example.model.Expense
import com.example.model.FuelChargeRecord
import com.example.model.VehicleProfile
import com.example.model.ViabilityCalculationResult
import com.example.model.WorkDay
import com.example.ui.theme.*
import com.example.ui.viewmodel.MainViewModel
import java.util.Calendar

@Composable
fun HomeScreen(
    dailyEarnings: List<DailyEarning>,
    expenses: List<Expense>,
    fuelChargeRecords: List<FuelChargeRecord> = emptyList(),
    workDays: List<WorkDay>,
    vehicleProfile: VehicleProfile,
    viabilityResult: ViabilityCalculationResult,
    onAddDailyWorkClick: () -> Unit,
    onNavigateToPlanning: () -> Unit,
    onNavigateToVehicle: () -> Unit,
    onOpenProfileMenu: () -> Unit,
    modifier: Modifier = Modifier
) {
    val todayDateStr = MainViewModel.getTodayDateString()
    val todayEarningRecord = dailyEarnings.find { it.dateStr == todayDateStr }

    // Today's values
    val todayEarnings = todayEarningRecord?.totalAmount ?: 0.0
    val todayTripsCount = todayEarningRecord?.tripsCount ?: 0
    val todayKmDriven = todayEarningRecord?.kmDriven ?: 0.0
    val todayRatePerKm = if (todayKmDriven > 0) todayEarnings / todayKmDriven else 0.0

    val hasTodayData = todayEarningRecord != null && (todayEarnings > 0 || todayKmDriven > 0 || todayTripsCount > 0)

    // Monthly & Planning Calculations
    val cal = Calendar.getInstance()
    val thisYear = cal.get(Calendar.YEAR)
    val thisMonth = cal.get(Calendar.MONTH)
    val todayDay = cal.get(Calendar.DAY_OF_MONTH)

    val monthEarnings = dailyEarnings.filter {
        val parts = it.dateStr.split("/")
        if (parts.size == 3) {
            val dMonth = parts[1].toIntOrNull()?.minus(1)
            val dYear = parts[2].toIntOrNull()
            dMonth == thisMonth && dYear == thisYear
        } else false
    }
    val totalEarnedThisMonth = monthEarnings.sumOf { it.totalAmount }

    val monthExpenses = expenses.filter {
        val parts = it.dateStr.split("/")
        if (parts.size == 3) {
            val dMonth = parts[1].toIntOrNull()?.minus(1)
            val dYear = parts[2].toIntOrNull()
            dMonth == thisMonth && dYear == thisYear
        } else false
    }
    val totalExpensesThisMonth = monthExpenses.sumOf { it.amount }
    val totalProfitThisMonth = totalEarnedThisMonth - totalExpensesThisMonth

    val monthWorkDays = workDays.filter {
        val parts = it.dateStr.split("/")
        if (parts.size == 3) {
            val dMonth = parts[1].toIntOrNull()?.minus(1)
            val dYear = parts[2].toIntOrNull()
            dMonth == thisMonth && dYear == thisYear && it.isWorkDay
        } else false
    }

    val remainingWorkDays = monthWorkDays.count {
        val parts = it.dateStr.split("/")
        val dayNum = parts.getOrNull(0)?.toIntOrNull() ?: 0
        dayNum >= todayDay
    }

    // Goal: profit-based target
    val monthlyGoal = vehicleProfile.monthlyGoal
    val remainingMonthlyProfit = (monthlyGoal - totalProfitThisMonth).coerceAtLeast(0.0)
    val requiredDailyProfitGoal = if (remainingWorkDays > 0) {
        remainingMonthlyProfit / remainingWorkDays
    } else {
        if (remainingMonthlyProfit > 0) remainingMonthlyProfit else 0.0
    }

    // Daily Goal considering Fuel or Recharge:
    // Today's recorded fuel/charge or estimated daily energy cost
    val todayFuelChargeRecords = fuelChargeRecords.filter { it.dateStr == todayDateStr }
    val todayFuelChargeCost = todayFuelChargeRecords.sumOf { it.totalCost }
    val estimatedDailyFuelCost = if (viabilityResult.energyCostPerKm > 0 && viabilityResult.avgDailyKm > 0) {
        viabilityResult.energyCostPerKm * viabilityResult.avgDailyKm
    } else {
        70.0
    }
    val dailyFuelOrChargeCost = if (todayFuelChargeCost > 0) todayFuelChargeCost else estimatedDailyFuelCost

    // Gross target for today = required net profit + fuel/charge cost
    val requiredDailyGrossTarget = requiredDailyProfitGoal + dailyFuelOrChargeCost

    val dailyGrossProgressPercent = if (requiredDailyGrossTarget > 0) {
        ((todayEarnings / requiredDailyGrossTarget) * 100).toInt().coerceIn(0, 100)
    } else if (todayEarnings > 0) 100 else 0

    // Minimum baseline of R$ 2.10 per km rodado
    val minFloorRatePerKm = 2.10
    val effectiveMinRatePerKm = maxOf(viabilityResult.recommendedMinRatePerKm, minFloorRatePerKm)

    // Probable km driven during the day to reach the daily goal
    val estimatedKmToReachDailyGoal = if (effectiveMinRatePerKm > 0 && requiredDailyGrossTarget > 0) {
        kotlin.math.ceil(requiredDailyGrossTarget / effectiveMinRatePerKm)
    } else if (viabilityResult.estimatedDailyKmToGoal > 0) {
        viabilityResult.estimatedDailyKmToGoal
    } else {
        150.0
    }

    val remainingGrossForToday = (requiredDailyGrossTarget - todayEarnings).coerceAtLeast(0.0)
    val remainingKmToGoal = if (effectiveMinRatePerKm > 0) {
        kotlin.math.ceil(remainingGrossForToday / effectiveMinRatePerKm)
    } else 0.0

    // Passenger fare simulator state
    var simulatorDistanceKmText by remember { mutableStateOf("10") }
    val simulatedKm = simulatorDistanceKmText.replace(",", ".").toDoubleOrNull() ?: 10.0
    val simulatedGrossPrice = simulatedKm * effectiveMinRatePerKm

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(BackgroundDark)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Top Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = MainViewModel.getTodayHeaderString(),
                    fontSize = 13.sp,
                    color = TextSecondary,
                    fontWeight = FontWeight.Medium
                )
                val firstName = vehicleProfile.driverName.trim().split("\\s+".toRegex()).firstOrNull().orEmpty()
                Text(
                    text = if (firstName.isNotBlank()) "Olá, $firstName" else "Início",
                    fontSize = 24.sp,
                    color = TextWhite,
                    fontWeight = FontWeight.Bold
                )
            }

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                // Vehicle Shortcut
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(CardDark)
                        .clickable { onNavigateToVehicle() }
                        .testTag("btn_vehicle_shortcut"),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Outlined.DirectionsCar,
                        contentDescription = "Meu Veículo",
                        tint = EmeraldGreen,
                        modifier = Modifier.size(22.dp)
                    )
                }

                // Calendar Shortcut
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(CardDark)
                        .clickable { onNavigateToPlanning() }
                        .testTag("btn_planning_shortcut"),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Outlined.CalendarMonth,
                        contentDescription = "Planejamento e Calendário",
                        tint = EmeraldGreen,
                        modifier = Modifier.size(22.dp)
                    )
                }

                // Profile button
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(CardDark)
                        .clickable { onOpenProfileMenu() }
                        .testTag("btn_open_profile_menu"),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Person,
                        contentDescription = "Menu do Usuário e Veículo",
                        tint = TextWhite,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }
        }

        // Notification Banner: Shown when earnings, km and trip count for today haven't been entered yet
        if (!hasTodayData) {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = CardHighlight),
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onAddDailyWorkClick() }
                    .testTag("banner_no_today_data")
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(EmeraldGreen.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.EditCalendar,
                            contentDescription = null,
                            tint = EmeraldGreen,
                            modifier = Modifier.size(24.dp)
                        )
                    }

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Informações do dia ainda não preenchidas",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextWhite
                        )
                        Text(
                            text = "Ganhos, km rodados e quantidade de corridas de hoje ainda não foram lançados. Estimativa para a meta: ~${estimatedKmToReachDailyGoal.toInt()} km.",
                            fontSize = 12.sp,
                            color = TextSecondary
                        )
                    }

                    Icon(
                        imageVector = Icons.Default.ChevronRight,
                        contentDescription = null,
                        tint = EmeraldGreen
                    )
                }
            }
        }

        // Hero Card: Faturamento / Ganhos do Dia (Sem informação de despesas)
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = CardDark),
            modifier = Modifier
                .fillMaxWidth()
                .testTag("hero_net_profit_card")
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Faturamento de hoje",
                        fontSize = 14.sp,
                        color = TextSecondary,
                        fontWeight = FontWeight.Medium
                    )

                    Surface(
                        color = if (hasTodayData) EmeraldGreen.copy(alpha = 0.15f) else SurfaceDark,
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = if (hasTodayData) "Dia em andamento" else "Aguardando lançamento",
                            fontSize = 11.sp,
                            color = if (hasTodayData) EmeraldGreen else TextSecondary,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }

                Text(
                    text = MainViewModel.formatCurrency(todayEarnings),
                    fontSize = 36.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = if (todayEarnings > 0) ProfitGreen else TextWhite
                )

                if (hasTodayData) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "Média: ${String.format("R$ %.2f", todayRatePerKm).replace(".", ",")}/km",
                            fontSize = 13.sp,
                            color = if (todayRatePerKm >= minFloorRatePerKm) ProfitGreen else FuelOrange,
                            fontWeight = FontWeight.Bold
                        )
                        Text(text = "•", color = TextMuted, fontSize = 13.sp)
                        Text(
                            text = "$todayTripsCount corridas",
                            fontSize = 13.sp,
                            color = TextWhite,
                            fontWeight = FontWeight.Medium
                        )
                        Text(text = "•", color = TextMuted, fontSize = 13.sp)
                        Text(
                            text = "${MainViewModel.formatKm(todayKmDriven)} (meta: ~${estimatedKmToReachDailyGoal.toInt()} km)",
                            fontSize = 13.sp,
                            color = TextSecondary
                        )
                    }
                } else {
                    Text(
                        text = "Toque em 'Registrar faturamento do dia' para começar seus lançamentos",
                        fontSize = 12.sp,
                        color = TextSecondary
                    )
                }
            }
        }

        // Metrics Grid Row: Corridas | Km rodados | R$/km (Piso mínimo R$ 2,10/km)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Corridas do dia
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = CardDark),
                modifier = Modifier.weight(1f)
            ) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text("Corridas", fontSize = 12.sp, color = TextSecondary)
                    Text(
                        text = if (hasTodayData) "$todayTripsCount" else "—",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextWhite
                    )
                    Text(
                        text = if (hasTodayData) "feitas hoje" else "Aguardando",
                        fontSize = 10.sp,
                        color = TextSecondary
                    )
                }
            }

            // Km Rodados no dia
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = CardDark),
                modifier = Modifier.weight(1f)
            ) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text("Km rodados", fontSize = 12.sp, color = TextSecondary)
                    Text(
                        text = if (hasTodayData) MainViewModel.formatKm(todayKmDriven) else "—",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextWhite
                    )
                    Text(
                        text = if (hasTodayData) "de ~${estimatedKmToReachDailyGoal.toInt()} km est." else "Meta: ~${estimatedKmToReachDailyGoal.toInt()} km",
                        fontSize = 10.sp,
                        color = EmeraldGreen,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            // R$/km no dia (Sempre considerando mínimo R$ 2,10)
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = CardDark),
                modifier = Modifier.weight(1f)
            ) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text("Média R$/km", fontSize = 12.sp, color = TextSecondary)
                    Text(
                        text = if (hasTodayData) String.format("R$ %.2f", todayRatePerKm).replace(".", ",") else String.format("R$ %.2f", minFloorRatePerKm).replace(".", ","),
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (hasTodayData) (if (todayRatePerKm >= minFloorRatePerKm) ProfitGreen else FuelOrange) else EmeraldGreen
                    )
                    Text(
                        text = if (hasTodayData) {
                            if (todayRatePerKm >= minFloorRatePerKm) "✓ Acima de R$ 2,10" else "⚠ Abaixo de R$ 2,10"
                        } else "Piso mín: R$ 2,10",
                        fontSize = 10.sp,
                        color = if (hasTodayData && todayRatePerKm < minFloorRatePerKm) FuelOrange else EmeraldGreen
                    )
                }
            }
        }

        // Card da Meta Diária (Considerando Abastecimento ou Recarga para exibir o Valor Bruto Necessário)
        Card(
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = CardDark),
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onNavigateToPlanning() }
                .testTag("card_daily_gross_goal")
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Meta Bruta de Hoje: ${MainViewModel.formatCurrency(requiredDailyGrossTarget)}",
                            fontSize = 16.sp,
                            color = TextWhite,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Lucro desejado: ${MainViewModel.formatCurrency(requiredDailyProfitGoal)} + Combustível/Recarga: ${MainViewModel.formatCurrency(dailyFuelOrChargeCost)}",
                            fontSize = 11.sp,
                            color = EmeraldGreen
                        )
                    }
                    Text(
                        text = "$dailyGrossProgressPercent%",
                        fontSize = 16.sp,
                        color = EmeraldGreen,
                        fontWeight = FontWeight.Bold
                    )
                }

                LinearProgressIndicator(
                    progress = { dailyGrossProgressPercent / 100f },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(RoundedCornerShape(4.dp)),
                    color = EmeraldGreen,
                    trackColor = SurfaceDark
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Faturado: ${MainViewModel.formatCurrency(todayEarnings)} de ${MainViewModel.formatCurrency(requiredDailyGrossTarget)}",
                        fontSize = 12.sp,
                        color = TextSecondary
                    )
                    Text(
                        text = "Ver planejamento >",
                        fontSize = 12.sp,
                        color = EmeraldGreen,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                HorizontalDivider(color = BorderDark, thickness = 0.5.dp)

                // Estimativa de km que o motorista provavelmente rodará durante o dia para atingir a meta
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = SurfaceDark,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("box_estimated_km_to_goal")
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(EmeraldGreen.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Route,
                                contentDescription = null,
                                tint = EmeraldGreen,
                                modifier = Modifier.size(22.dp)
                            )
                        }

                        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Quilometragem Provável no Dia",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = TextWhite
                                )
                                Surface(
                                    color = EmeraldGreen.copy(alpha = 0.2f),
                                    shape = RoundedCornerShape(6.dp)
                                ) {
                                    Text(
                                        text = "~${estimatedKmToReachDailyGoal.toInt()} km",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = EmeraldGreen,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }

                            if (hasTodayData && todayKmDriven > 0) {
                                Text(
                                    text = if (todayEarnings >= requiredDailyGrossTarget) {
                                        "🎉 Meta de hoje atingida com ${MainViewModel.formatKm(todayKmDriven)} rodados!"
                                    } else {
                                        "Você já rodou ${MainViewModel.formatKm(todayKmDriven)}. Faltam cerca de ~${remainingKmToGoal.toInt()} km para bater a meta de hoje."
                                    },
                                    fontSize = 11.sp,
                                    color = if (todayEarnings >= requiredDailyGrossTarget) ProfitGreen else TextSecondary
                                )
                            } else {
                                Text(
                                    text = "Para bater a meta de faturamento de hoje, você provavelmente rodará cerca de ~${estimatedKmToReachDailyGoal.toInt()} km (considerando a média mínima de R$ ${String.format("%.2f", effectiveMinRatePerKm).replace(".", ",")}/km).",
                                    fontSize = 11.sp,
                                    color = TextSecondary
                                )
                            }
                        }
                    }
                }
            }
        }

        // Simulador de Corrida: Mostrar para o passageiro qual o valor bruto da sua média
        Card(
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = CardHighlight),
            modifier = Modifier
                .fillMaxWidth()
                .testTag("card_passenger_fare_simulator")
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(EmeraldGreen.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Calculate,
                            contentDescription = null,
                            tint = EmeraldGreen,
                            modifier = Modifier.size(22.dp)
                        )
                    }

                    Column {
                        Text(
                            text = "Simulador de Corrida (Média Bruta)",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = EmeraldGreen
                        )
                        Text(
                            text = "Mostre ao passageiro ou calcule o valor bruto de uma corrida:",
                            fontSize = 11.sp,
                            color = TextSecondary
                        )
                    }
                }

                // Distance input with quick chips
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    listOf("5", "10", "15", "20", "30").forEach { chipKm ->
                        val isSelected = simulatorDistanceKmText == chipKm
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isSelected) EmeraldGreen else SurfaceDark)
                                .clickable { simulatorDistanceKmText = chipKm }
                                .padding(vertical = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "${chipKm}km",
                                fontSize = 11.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                color = if (isSelected) Color.White else TextSecondary
                            )
                        }
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = simulatorDistanceKmText,
                        onValueChange = { simulatorDistanceKmText = it },
                        label = { Text("Distância da Corrida (km)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true,
                        modifier = Modifier.weight(1.1f),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = EmeraldGreen,
                            unfocusedBorderColor = BorderDark
                        )
                    )

                    Surface(
                        modifier = Modifier.weight(1.3f),
                        shape = RoundedCornerShape(12.dp),
                        color = SurfaceDark
                    ) {
                        Column(
                            modifier = Modifier.padding(10.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "Valor Bruto da Corrida",
                                fontSize = 10.sp,
                                color = TextSecondary
                            )
                            Text(
                                text = String.format("R$ %.2f", simulatedGrossPrice).replace(".", ","),
                                fontSize = 20.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = ProfitGreen
                            )
                        }
                    }
                }

                Text(
                    text = "Média mínima considerada: R$ ${String.format("%.2f", effectiveMinRatePerKm).replace(".", ",")}/km (piso de R$ 2,10/km, incluindo custos de abastecimento/recarga).",
                    fontSize = 11.sp,
                    color = TextSecondary
                )
            }
        }

        // Vehicle Viability Card: Quanto devo aceitar por km e por hora
        Card(
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = CardDark),
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onNavigateToVehicle() }
                .testTag("card_viability_banner")
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(46.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(EmeraldGreen.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Speed,
                        contentDescription = null,
                        tint = EmeraldGreen,
                        modifier = Modifier.size(26.dp)
                    )
                }

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Tarifas Mínimas Recomendadas",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = EmeraldGreen
                    )
                    Text(
                        text = "Aceite no mínimo R$ ${String.format("%.2f", effectiveMinRatePerKm).replace(".", ",")}/km ou R$ ${String.format("%.2f", viabilityResult.recommendedMinRatePerHour).replace(".", ",")}/h",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = TextWhite
                    )
                    Text(
                        text = "Piso mínimo de segurança: R$ 2,10 por km rodado",
                        fontSize = 11.sp,
                        color = TextSecondary
                    )
                }

                Icon(
                    imageVector = Icons.Default.ChevronRight,
                    contentDescription = null,
                    tint = EmeraldGreen
                )
            }
        }

        // Action Button: + Registrar Faturamento e Corridas do Dia (sem botão de despesa)
        Button(
            onClick = onAddDailyWorkClick,
            modifier = Modifier
                .fillMaxWidth()
                .height(54.dp)
                .testTag("btn_register_earning"),
            colors = ButtonDefaults.buttonColors(containerColor = EmeraldGreen),
            shape = RoundedCornerShape(14.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(22.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Registrar Faturamento do Dia",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}
