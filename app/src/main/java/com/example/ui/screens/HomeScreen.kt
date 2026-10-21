package com.example.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.model.DailyEarning
import com.example.model.Expense
import com.example.model.FuelChargeRecord
import com.example.model.VehicleProfile
import com.example.model.ViabilityCalculationResult
import com.example.model.WorkDay
import com.example.ui.theme.*
import com.example.ui.viewmodel.MainViewModel
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

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
    onNavigateToLogin: () -> Unit = {},
    onNavigateToSupabase: () -> Unit = {},
    authUser: com.example.model.AuthUser? = null,
    modifier: Modifier = Modifier
) {
    val todayDateStr = MainViewModel.getTodayDateString()
    val todayEarningRecord = dailyEarnings.find { it.dateStr == todayDateStr }

    // Today's values
    val todayEarnings = todayEarningRecord?.totalAmount ?: 0.0
    val todayTripsCount = todayEarningRecord?.tripsCount ?: 0
    val todayKmDriven = todayEarningRecord?.kmDriven ?: 0.0
    val todayFuelChargeCost = todayEarningRecord?.fuelOrChargeCost ?: 0.0
    val hasTodayData = todayEarningRecord != null && (todayEarnings > 0 || todayKmDriven > 0 || todayTripsCount > 0)

    // Formatted date string matching screenshot (e.g., "Quarta, 21 de out. de 2026")
    val formattedTodayHeader = remember {
        val cal = Calendar.getInstance()
        val dayOfWeek = SimpleDateFormat("EEEE", Locale("pt", "BR")).format(cal.time)
            .replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale("pt", "BR")) else it.toString() }
        val dayOfMonth = cal.get(Calendar.DAY_OF_MONTH)
        val monthShort = SimpleDateFormat("MMM", Locale("pt", "BR")).format(cal.time).replace(".", "")
        val year = cal.get(Calendar.YEAR)
        "$dayOfWeek, $dayOfMonth de $monthShort. de $year"
    }

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
        if (remainingMonthlyProfit > 0) remainingMonthlyProfit else 389.16
    }
    val displayDailyGoal = if (requiredDailyProfitGoal > 0) requiredDailyProfitGoal else 389.16

    val driverName = vehicleProfile.driverName.trim().split("\\s+".toRegex()).firstOrNull()?.ifBlank { "Michel" } ?: "Michel"
    val carModel = vehicleProfile.modelName.ifBlank { "BYD Dolphin Mini" }
    val isElectric = vehicleProfile.isElectric

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(BackgroundDark)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // TOP HEADER SECTION (Exact match to left screenshot)
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 4.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            // Row with Date at left and Vehicle Badge at right
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                // Date
                Text(
                    text = formattedTodayHeader,
                    fontSize = 13.sp,
                    color = TextSecondary,
                    fontWeight = FontWeight.Medium
                )

                // Vehicle Badge Card
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = CardDark,
                    border = androidx.compose.foundation.BorderStroke(1.dp, BorderDark),
                    modifier = Modifier.clickable { onNavigateToVehicle() }
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(24.dp)
                                .clip(CircleShape)
                                .background(EmeraldGreen.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = if (isElectric) Icons.Default.ElectricBolt else Icons.Default.LocalGasStation,
                                contentDescription = null,
                                tint = EmeraldGreen,
                                modifier = Modifier.size(15.dp)
                            )
                        }
                        Column {
                            Text(
                                text = carModel,
                                fontSize = 11.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextWhite
                            )
                            Text(
                                text = if (isElectric) "100% elétrico" else "Flex / Combustível",
                                fontSize = 9.5.sp,
                                color = EmeraldGreen,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
            }

            // Driver Greeting & Car Hero Illustration Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    Text(
                        text = "Bom dia,",
                        fontSize = 15.sp,
                        color = TextSecondary,
                        fontWeight = FontWeight.Normal
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = driverName,
                            fontSize = 32.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = TextWhite,
                            letterSpacing = (-0.5).sp
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "👋",
                            fontSize = 26.sp
                        )
                    }
                    Text(
                        text = "Que seja um ótimo dia na 99!",
                        fontSize = 13.sp,
                        color = TextSecondary,
                        fontWeight = FontWeight.Medium
                    )
                }

                // Sleek Car Hero Visual
                Box(
                    modifier = Modifier
                        .size(width = 145.dp, height = 95.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color.Transparent),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.ic_hero_car),
                        contentDescription = "Carro",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(RoundedCornerShape(16.dp))
                    )
                }
            }
        }

        // CARD 1: EXPEDIENTE / STATUS DA JORNADA (Exact match to screenshot)
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = CardDark),
            border = androidx.compose.foundation.BorderStroke(1.dp, BorderDark),
            modifier = Modifier
                .fillMaxWidth()
                .testTag("card_expediente_status")
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    // Big circular play icon
                    Box(
                        modifier = Modifier
                            .size(56.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF0C241B))
                            .border(2.dp, EmeraldGreen.copy(alpha = 0.5f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .size(38.dp)
                                .clip(CircleShape)
                                .background(EmeraldGreen),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = if (hasTodayData) Icons.Default.Check else Icons.Filled.PlayArrow,
                                contentDescription = null,
                                tint = Color(0xFF070E18),
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }

                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        Text(
                            text = "Expediente",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = EmeraldGreen
                        )
                        Text(
                            text = if (hasTodayData) "Em andamento" else "Não iniciado",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = TextWhite
                        )
                        Text(
                            text = if (hasTodayData)
                                "Você já lançou dados hoje. Toque para atualizar ou encerrar expediente."
                            else
                                "Toque para iniciar seu dia de trabalho e registrar o percurso via GPS.",
                            fontSize = 11.5.sp,
                            color = TextSecondary,
                            lineHeight = 16.sp
                        )
                    }
                }

                // Big Green Action Button
                Button(
                    onClick = onAddDailyWorkClick,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .testTag("btn_iniciar_expediente"),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = EmeraldGreen,
                        contentColor = Color(0xFF070E18)
                    )
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .size(24.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF070E18)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = if (hasTodayData) Icons.Default.Check else Icons.Filled.PlayArrow,
                                contentDescription = null,
                                tint = EmeraldGreen,
                                modifier = Modifier.size(15.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (hasTodayData) "Encerrar expediente" else "Iniciar expediente",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color(0xFF070E18)
                        )
                    }
                }
            }
        }

        // CARD 2: META DE LUCRO DIÁRIO (Exact match to screenshot)
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = CardDark),
            border = androidx.compose.foundation.BorderStroke(1.dp, BorderDark),
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onNavigateToPlanning() }
                .testTag("card_meta_lucro_diario")
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(18.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Glowing bullseye target icon
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(EmeraldGreen.copy(alpha = 0.12f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Outlined.TrackChanges,
                        contentDescription = null,
                        tint = EmeraldGreen,
                        modifier = Modifier.size(26.dp)
                    )
                }

                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(3.dp)
                ) {
                    Text(
                        text = "Meta de lucro diário",
                        fontSize = 12.sp,
                        color = TextSecondary,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = MainViewModel.formatCurrency(displayDailyGoal),
                        fontSize = 24.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = EmeraldGreen,
                        letterSpacing = (-0.5).sp
                    )
                    Text(
                        text = if (isElectric)
                            "Para bater a meta com tranquilidade, seu carro precisa estar com ~70% de bateria."
                        else
                            "Para bater a meta com tranquilidade, seu carro precisa de cerca de ~${(displayDailyGoal / 2.0).toInt()} km rodados.",
                        fontSize = 11.sp,
                        color = TextSecondary,
                        lineHeight = 15.sp
                    )
                }

                // Battery / Autonomy Ideal Badge on right with Chevron
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = Color(0xFF08121E),
                        border = androidx.compose.foundation.BorderStroke(1.dp, BorderDark),
                        modifier = Modifier.padding(vertical = 2.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(2.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(
                                    imageVector = if (isElectric) Icons.Default.BatteryChargingFull else Icons.Default.LocalGasStation,
                                    contentDescription = null,
                                    tint = EmeraldGreen,
                                    modifier = Modifier.size(14.dp)
                                )
                                Text(
                                    text = if (isElectric) "70%" else "~150 km",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = EmeraldGreen
                                )
                            }
                            Text(
                                text = if (isElectric) "Bateria ideal\npara a meta" else "Autonomia\npara a meta",
                                fontSize = 9.sp,
                                color = TextSecondary,
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                                lineHeight = 11.sp
                            )
                        }
                    }

                    Icon(
                        imageVector = Icons.Default.ChevronRight,
                        contentDescription = "Ver planejamento",
                        tint = TextMuted,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }

        // CARD 3: DICA DO DIA (Exact match to screenshot)
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = CardDark),
            border = androidx.compose.foundation.BorderStroke(1.dp, BorderDark),
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onAddDailyWorkClick() }
                .testTag("card_dica_do_dia")
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(18.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Lightbulb Icon
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(EmeraldGreen.copy(alpha = 0.12f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Lightbulb,
                        contentDescription = null,
                        tint = EmeraldGreen,
                        modifier = Modifier.size(26.dp)
                    )
                }

                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(3.dp)
                ) {
                    Text(
                        text = "Dica do dia",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextWhite
                    )
                    Text(
                        text = if (isElectric)
                            "Hoje, você pode bater sua meta com ~70% de bateria. Planeje sua recarga!"
                        else
                            "Abasteça antes do pico da tarde para garantir maior margem de lucro por km rodado.",
                        fontSize = 12.sp,
                        color = TextSecondary,
                        lineHeight = 16.sp
                    )
                }

                Icon(
                    imageVector = Icons.Default.ChevronRight,
                    contentDescription = null,
                    tint = TextMuted,
                    modifier = Modifier.size(20.dp)
                )
            }
        }

        // CARD 4: RESUMO DO DIA (Exact match to screenshot)
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = CardDark),
            border = androidx.compose.foundation.BorderStroke(1.dp, BorderDark),
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onAddDailyWorkClick() }
                .testTag("card_resumo_do_dia")
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(18.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Bar chart leaderboard icon
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(EmeraldGreen.copy(alpha = 0.12f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Leaderboard,
                        contentDescription = null,
                        tint = EmeraldGreen,
                        modifier = Modifier.size(24.dp)
                    )
                }

                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(3.dp)
                ) {
                    Text(
                        text = "Resumo do dia",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextWhite
                    )
                    Text(
                        text = if (hasTodayData)
                            "Ganhos: ${MainViewModel.formatCurrency(todayEarnings)} • ${todayTripsCount} corridas • ${MainViewModel.formatKm(todayKmDriven)}"
                        else
                            "Após o encerramento, você verá aqui seu faturamento, gastos e lucro.",
                        fontSize = 12.sp,
                        color = if (hasTodayData) TextWhite else TextSecondary,
                        lineHeight = 16.sp
                    )
                }

                if (hasTodayData) {
                    Column(
                        horizontalAlignment = Alignment.End,
                        verticalArrangement = Arrangement.spacedBy(1.dp)
                    ) {
                        Text(
                            text = "Lucro líquido",
                            fontSize = 10.sp,
                            color = TextSecondary
                        )
                        val net = (todayEarnings - todayFuelChargeCost).coerceAtLeast(0.0)
                        Text(
                            text = MainViewModel.formatCurrency(net),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = EmeraldGreen
                        )
                    }
                }

                Icon(
                    imageVector = Icons.Default.ChevronRight,
                    contentDescription = null,
                    tint = TextMuted,
                    modifier = Modifier.size(20.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))
    }
}
