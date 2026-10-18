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
    onRecordStageAction: (stage: String) -> Unit = {},
    onAdjustStagesClick: () -> Unit = {},
    onAddPastDailyWorkClick: () -> Unit = onAddDailyWorkClick,
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

    // Tabela fixa padrão do app de R$ 2,00 por km rodado
    val minFloorRatePerKm = 2.00
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

    // Daily Work Stages variables
    val todayStartTime = todayEarningRecord?.startTime.orEmpty()
    val todayLunchStartTime = todayEarningRecord?.lunchStartTime.orEmpty()
    val todayLunchEndTime = todayEarningRecord?.lunchEndTime.orEmpty()
    val todayEndTime = todayEarningRecord?.endTime.orEmpty()
    val todayWorkedMinutes = todayEarningRecord?.calculatedMinutes ?: 0
    val todayWorkedHours = todayEarningRecord?.calculatedHours ?: 0.0
    val todayFormattedTime = todayEarningRecord?.formattedWorkedTime ?: "0h"
    val todayHourlyRate = if (todayWorkedHours > 0 && todayEarnings > 0) todayEarnings / todayWorkedHours else 0.0

    val isJourneyCompleted = todayEndTime.isNotBlank()
    val isInSecondShift = todayLunchEndTime.isNotBlank() && todayEndTime.isBlank()
    val isInLunchBreak = todayLunchStartTime.isNotBlank() && todayLunchEndTime.isBlank()
    val isInFirstShift = todayStartTime.isNotBlank() && todayLunchStartTime.isBlank() && todayEndTime.isBlank()
    val isJourneyNotStarted = todayStartTime.isBlank()

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
                            text = if (todayStartTime.isNotBlank()) "Jornada em andamento (${todayFormattedTime})" else "Informações do dia ainda não preenchidas",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextWhite
                        )
                        Text(
                            text = if (todayStartTime.isNotBlank()) {
                                "Início às $todayStartTime. Toque para lançar ganhos, km e corridas. Estimativa para a meta: ~${estimatedKmToReachDailyGoal.toInt()} km."
                            } else {
                                "Ganhos, km rodados e quantidade de corridas de hoje ainda não foram lançados. Estimativa para a meta: ~${estimatedKmToReachDailyGoal.toInt()} km."
                            },
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

        // Metrics Grid Row: Corridas | Km rodados | R$/km (Tabela padrão R$ 2,00/km)
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

            // R$/km no dia (Tabela fixa padrão R$ 2,00/km)
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = CardDark),
                modifier = Modifier.weight(1f)
            ) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text("Tabela R$/km", fontSize = 12.sp, color = TextSecondary)
                    Text(
                        text = if (hasTodayData) String.format("R$ %.2f", todayRatePerKm).replace(".", ",") else String.format("R$ %.2f", minFloorRatePerKm).replace(".", ","),
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (hasTodayData) (if (todayRatePerKm >= minFloorRatePerKm) ProfitGreen else FuelOrange) else EmeraldGreen
                    )
                    Text(
                        text = if (hasTodayData) {
                            if (todayRatePerKm >= minFloorRatePerKm) "✓ Acima de R$ 2,00" else "⚠ Abaixo de R$ 2,00"
                        } else "Tabela: R$ 2,00",
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

        // Card: Etapas da Jornada de Trabalho (Cálculo de Horas Trabalhadas)
        Card(
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = CardDark),
            modifier = Modifier
                .fillMaxWidth()
                .testTag("card_work_stages")
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Header with title and "Ajustar" button
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
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
                                imageVector = Icons.Outlined.Schedule,
                                contentDescription = null,
                                tint = EmeraldGreen,
                                modifier = Modifier.size(22.dp)
                            )
                        }

                        Column {
                            Text(
                                text = "Etapas da Jornada de Trabalho",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextWhite
                            )
                            Text(
                                text = when {
                                    isJourneyCompleted -> "Jornada do dia finalizada"
                                    isInSecondShift -> "2º Turno em andamento"
                                    isInLunchBreak -> "Em pausa para almoço"
                                    isInFirstShift -> "1º Turno em andamento"
                                    else -> "Aguardando início do dia"
                                },
                                fontSize = 11.sp,
                                color = if (isInLunchBreak) FuelOrange else if (isJourneyCompleted) ProfitGreen else EmeraldGreen,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }

                    OutlinedButton(
                        onClick = onAdjustStagesClick,
                        modifier = Modifier.testTag("btn_adjust_stages"),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = EmeraldGreen),
                        border = androidx.compose.foundation.BorderStroke(1.dp, EmeraldGreen.copy(alpha = 0.5f))
                    ) {
                        Icon(Icons.Outlined.Edit, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Ajustar", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }

                // Summary Row: Horas Trabalhadas & Média R$/h
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(SurfaceDark)
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("Tempo trabalhado hoje", fontSize = 11.sp, color = TextSecondary)
                        Text(
                            text = todayFormattedTime,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = if (todayWorkedMinutes > 0) EmeraldGreen else TextSecondary
                        )
                    }

                    if (todayHourlyRate > 0) {
                        Column(horizontalAlignment = Alignment.End) {
                            Text("Ganho por hora real", fontSize = 11.sp, color = TextSecondary)
                            Text(
                                text = String.format(java.util.Locale.US, "R$ %.2f/h", todayHourlyRate).replace(".", ","),
                                fontSize = 18.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = ProfitGreen
                            )
                        }
                    } else {
                        Column(horizontalAlignment = Alignment.End) {
                            Text("Piso recomendado", fontSize = 11.sp, color = TextSecondary)
                            Text(
                                text = String.format(java.util.Locale.US, "R$ %.2f/h", viabilityResult.recommendedMinRatePerHour).replace(".", ","),
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = TextSecondary
                            )
                        }
                    }
                }

                // Visual Stepper of 4 stages: Início, Pausa, Retorno, Fim
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    WorkStageChip(
                        stageNumber = "1",
                        label = "Início",
                        timeStr = todayStartTime,
                        isActive = isInFirstShift,
                        isDone = todayStartTime.isNotBlank(),
                        modifier = Modifier.weight(1f)
                    )
                    Text("→", color = TextSecondary, fontSize = 11.sp)
                    WorkStageChip(
                        stageNumber = "2",
                        label = "Pausa",
                        timeStr = todayLunchStartTime,
                        isActive = isInLunchBreak,
                        isDone = todayLunchStartTime.isNotBlank(),
                        modifier = Modifier.weight(1f)
                    )
                    Text("→", color = TextSecondary, fontSize = 11.sp)
                    WorkStageChip(
                        stageNumber = "3",
                        label = "Retorno",
                        timeStr = todayLunchEndTime,
                        isActive = isInSecondShift,
                        isDone = todayLunchEndTime.isNotBlank(),
                        modifier = Modifier.weight(1f)
                    )
                    Text("→", color = TextSecondary, fontSize = 11.sp)
                    WorkStageChip(
                        stageNumber = "4",
                        label = "Fim",
                        timeStr = todayEndTime,
                        isActive = false,
                        isDone = todayEndTime.isNotBlank(),
                        modifier = Modifier.weight(1f)
                    )
                }

                // Interactive Quick-Action Buttons (advances stages with current time)
                when {
                    isJourneyNotStarted -> {
                        Button(
                            onClick = { onRecordStageAction("START") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(46.dp)
                                .testTag("btn_action_start_day"),
                            colors = ButtonDefaults.buttonColors(containerColor = EmeraldGreen),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Iniciar Dia de Trabalho", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        }
                    }
                    isInFirstShift -> {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Button(
                                onClick = { onRecordStageAction("LUNCH_START") },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(46.dp)
                                    .testTag("btn_action_lunch_start"),
                                colors = ButtonDefaults.buttonColors(containerColor = FuelOrange),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Icon(Icons.Default.Pause, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Pausa Almoço", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            }

                            OutlinedButton(
                                onClick = { onRecordStageAction("END") },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(46.dp)
                                    .testTag("btn_action_end_day_direct"),
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = ExpenseRedSoft),
                                border = androidx.compose.foundation.BorderStroke(1.dp, ExpenseRedSoft)
                            ) {
                                Icon(Icons.Default.Stop, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Fim da Jornada", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            }
                        }
                    }
                    isInLunchBreak -> {
                        Button(
                            onClick = { onRecordStageAction("LUNCH_END") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(46.dp)
                                .testTag("btn_action_lunch_end"),
                            colors = ButtonDefaults.buttonColors(containerColor = EmeraldGreen),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Retorno do Almoço", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        }
                    }
                    isInSecondShift -> {
                        Button(
                            onClick = { onRecordStageAction("END") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(46.dp)
                                .testTag("btn_action_end_day"),
                            colors = ButtonDefaults.buttonColors(containerColor = ExpenseRedSoft),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(Icons.Default.Stop, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Fim da Jornada", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color.White)
                        }
                    }
                    isJourneyCompleted -> {
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = EmeraldGreen.copy(alpha = 0.15f),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(10.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(Icons.Default.CheckCircle, contentDescription = null, tint = EmeraldGreen, modifier = Modifier.size(18.dp))
                                Text(
                                    text = "Jornada de hoje finalizada com sucesso ($todayFormattedTime trabalhadas).",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = EmeraldGreen
                                )
                            }
                        }
                    }
                }
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
                        text = "Tabela Padrão de Corridas",
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
                        text = "Tabela fixa padrão do app: R$ 2,00 por km rodado",
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

        // Action Buttons: + Registrar Faturamento de Hoje & + Lançar Outro Dia / Passado
        Button(
            onClick = onAddDailyWorkClick,
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
                .testTag("btn_register_earning"),
            colors = ButtonDefaults.buttonColors(containerColor = EmeraldGreen),
            shape = RoundedCornerShape(14.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Registrar Faturamento de Hoje",
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        }

        OutlinedButton(
            onClick = onAddPastDailyWorkClick,
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .testTag("btn_register_past_earning"),
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = EmeraldGreen),
            border = androidx.compose.foundation.BorderStroke(1.dp, EmeraldGreen)
        ) {
            Icon(
                imageVector = Icons.Default.DateRange,
                contentDescription = null,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Lançar Faturamento de Outro Dia / Passado",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
private fun WorkStageChip(
    stageNumber: String,
    label: String,
    timeStr: String,
    isActive: Boolean,
    isDone: Boolean,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(10.dp),
        color = when {
            isActive -> EmeraldGreen.copy(alpha = 0.25f)
            isDone -> SurfaceDark
            else -> BackgroundDark
        },
        border = if (isActive) androidx.compose.foundation.BorderStroke(1.dp, EmeraldGreen) else null,
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 4.dp, vertical = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "$stageNumber. $label",
                fontSize = 10.sp,
                fontWeight = FontWeight.SemiBold,
                color = if (isActive || isDone) EmeraldGreen else TextSecondary,
                maxLines = 1
            )
            Text(
                text = if (timeStr.isNotBlank()) timeStr else "--:--",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = if (isDone) TextWhite else TextSecondary
            )
        }
    }
}
