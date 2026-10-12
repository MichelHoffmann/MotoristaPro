package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.DailyEarning
import com.example.model.Expense
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
    workDays: List<WorkDay>,
    vehicleProfile: VehicleProfile,
    viabilityResult: ViabilityCalculationResult,
    onAddDailyWorkClick: () -> Unit,
    onAddExpenseClick: () -> Unit,
    onNavigateToPlanning: () -> Unit,
    onNavigateToVehicle: () -> Unit,
    onOpenProfileMenu: () -> Unit,
    modifier: Modifier = Modifier
) {
    val todayDateStr = MainViewModel.getTodayDateString()
    val todayEarningRecord = dailyEarnings.find { it.dateStr == todayDateStr }
    val todayExpensesList = expenses.filter { it.dateStr == todayDateStr }

    // Today's values
    val todayEarnings = todayEarningRecord?.totalAmount ?: 0.0
    val todayTripsCount = todayEarningRecord?.tripsCount ?: 0
    val todayKmDriven = todayEarningRecord?.kmDriven ?: 0.0
    val todayExpenses = todayExpensesList.sumOf { it.amount }
    val todayNetProfit = todayEarnings - todayExpenses
    val todayRatePerKm = if (todayKmDriven > 0) todayEarnings / todayKmDriven else 0.0

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

    val monthlyGoal = vehicleProfile.monthlyGoal
    val remainingMonthlyAmount = (monthlyGoal - totalEarnedThisMonth).coerceAtLeast(0.0)
    val requiredDailyGoal = if (remainingWorkDays > 0) {
        remainingMonthlyAmount / remainingWorkDays
    } else {
        if (remainingMonthlyAmount > 0) remainingMonthlyAmount else 0.0
    }

    val dailyProgressPercent = if (requiredDailyGoal > 0) {
        ((todayEarnings / requiredDailyGoal) * 100).toInt().coerceIn(0, 100)
    } else if (todayEarnings > 0) 100 else 0

    val monthlyProgressPercent = if (monthlyGoal > 0) {
        ((totalEarnedThisMonth / monthlyGoal) * 100).toInt().coerceIn(0, 100)
    } else 0

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
                Text(
                    text = if (vehicleProfile.driverName.isNotBlank()) "Olá, ${vehicleProfile.driverName}" else "Início",
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

        // Hero Card: Lucro Líquido do Dia
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
                Text(
                    text = "Lucro líquido do dia",
                    fontSize = 14.sp,
                    color = TextSecondary,
                    fontWeight = FontWeight.Medium
                )

                Text(
                    text = MainViewModel.formatCurrency(todayNetProfit),
                    fontSize = 34.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = if (todayNetProfit >= 0) ProfitGreen else ExpenseRedSoft
                )

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = "Ganhos: ${MainViewModel.formatCurrency(todayEarnings)}",
                        fontSize = 13.sp,
                        color = TextWhite,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(text = "|", color = TextMuted, fontSize = 13.sp)
                    Text(
                        text = "Despesas: ${MainViewModel.formatCurrency(todayExpenses)}",
                        fontSize = 13.sp,
                        color = ExpenseRedSoft,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }

        // Metrics Grid Row: Corridas | Km rodados | R$/km
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
                        text = "$todayTripsCount",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextWhite
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
                        text = MainViewModel.formatKm(todayKmDriven),
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextWhite
                    )
                }
            }

            // R$/km no dia
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = CardDark),
                modifier = Modifier.weight(1f)
            ) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text("R$/km", fontSize = 12.sp, color = TextSecondary)
                    Text(
                        text = String.format("R$ %.2f", todayRatePerKm).replace(".", ","),
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextWhite
                    )
                }
            }
        }

        // Vehicle Viability Pill Card: Quanto devo aceitar por km e por hora
        Card(
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = CardHighlight),
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
                        text = if (viabilityResult.hasSufficientData) {
                            "Aceite no mínimo R$ ${String.format("%.2f", viabilityResult.recommendedMinRatePerKm).replace(".", ",")}/km ou R$ ${String.format("%.2f", viabilityResult.recommendedMinRatePerHour).replace(".", ",")}/h"
                        } else {
                            "Configure o consumo e custos do veículo para calcular suas tarifas mínimas"
                        },
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = TextWhite
                    )
                    Text(
                        text = "Custo do carro: ~R$ ${String.format("%.2f", viabilityResult.estimatedCostPerKm).replace(".", ",")}/km (inclui combustível e deslocamento vazio)",
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

        // Dynamic Daily Goal Card (calculated from Monthly Goal & Remaining Work Days)
        Card(
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = CardDark),
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onNavigateToPlanning() }
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
                    Column {
                        Text(
                            text = "Meta de hoje: ${MainViewModel.formatCurrency(requiredDailyGoal)}",
                            fontSize = 15.sp,
                            color = TextWhite,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Para bater ${MainViewModel.formatCurrency(monthlyGoal)} no mês ($remainingWorkDays dias restantes)",
                            fontSize = 11.sp,
                            color = TextSecondary
                        )
                    }
                    Text(
                        text = "$dailyProgressPercent%",
                        fontSize = 15.sp,
                        color = EmeraldGreen,
                        fontWeight = FontWeight.Bold
                    )
                }

                LinearProgressIndicator(
                    progress = { dailyProgressPercent / 100f },
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
                        text = "Mês: ${MainViewModel.formatCurrency(totalEarnedThisMonth)} / ${MainViewModel.formatCurrency(monthlyGoal)} ($monthlyProgressPercent%)",
                        fontSize = 12.sp,
                        color = TextSecondary
                    )
                    Text(
                        text = "Ver calendário >",
                        fontSize = 12.sp,
                        color = EmeraldGreen,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }

        // Action Buttons: + Registrar ganho do dia & - Registrar despesa
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
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
                    text = "Registrar faturamento do dia",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }

            OutlinedButton(
                onClick = onAddExpenseClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .testTag("btn_register_expense"),
                colors = ButtonDefaults.outlinedButtonColors(
                    containerColor = CardDark,
                    contentColor = TextWhite
                ),
                border = ButtonDefaults.outlinedButtonBorder.copy(brush = androidx.compose.ui.graphics.SolidColor(BorderDark)),
                shape = RoundedCornerShape(14.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Remove,
                    contentDescription = null,
                    tint = ExpenseRedSoft,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Registrar despesa",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = TextWhite
                )
            }
        }

        // Planning card banner
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = SurfaceDark),
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onNavigateToPlanning() }
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
                        imageVector = Icons.Outlined.CalendarMonth,
                        contentDescription = null,
                        tint = EmeraldGreen,
                        modifier = Modifier.size(24.dp)
                    )
                }

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Planeje seus dias de trabalho e feriados",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextWhite
                    )
                    Text(
                        text = "Veja feriados nacionais, do Acre e Rio Branco para ajustar sua escala e meta diária.",
                        fontSize = 11.sp,
                        color = TextSecondary
                    )
                }

                Icon(
                    imageVector = Icons.Default.ChevronRight,
                    contentDescription = null,
                    tint = TextSecondary
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}
