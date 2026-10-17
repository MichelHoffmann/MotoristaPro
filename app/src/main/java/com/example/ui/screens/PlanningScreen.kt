package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.model.*
import com.example.ui.theme.*
import com.example.ui.viewmodel.CalendarDay
import com.example.ui.viewmodel.MainViewModel
import com.example.ui.viewmodel.QuickWorkSchedule
import java.util.Calendar
import java.util.Locale

@Composable
fun PlanningScreen(
    currentYearMonth: Pair<Int, Int>,
    workDays: List<WorkDay>,
    dailyEarnings: List<DailyEarning>,
    expenses: List<Expense> = emptyList(),
    vehicleProfile: VehicleProfile,
    onNextMonth: () -> Unit,
    onPreviousMonth: () -> Unit,
    onToggleWorkDay: (String) -> Unit,
    onApplySchedule: (QuickWorkSchedule) -> Unit,
    onUpdateMonthlyGoal: (Double) -> Unit,
    modifier: Modifier = Modifier
) {
    val (year, month) = currentYearMonth
    var showEditGoalDialog by remember { mutableStateOf(false) }
    var selectedHolidayForDetails by remember { mutableStateOf<Holiday?>(null) }

    // Calendar Calculations
    val cal = Calendar.getInstance()
    val todayYear = cal.get(Calendar.YEAR)
    val todayMonth = cal.get(Calendar.MONTH)
    val todayDay = cal.get(Calendar.DAY_OF_MONTH)

    cal.set(year, month, 1)
    val daysInMonth = cal.getActualMaximum(Calendar.DAY_OF_MONTH)
    val firstDayOfWeek = cal.get(Calendar.DAY_OF_WEEK) // 1 = Sunday ... 7 = Saturday

    // Holidays of the month
    val monthHolidays = BrazilianHolidays.getHolidaysForYear(year).filter { it.month == month + 1 }

    // Filter earnings for this month
    val monthEarnings = dailyEarnings.filter {
        val parts = it.dateStr.split("/")
        if (parts.size == 3) {
            val dMonth = parts[1].toIntOrNull()?.minus(1)
            val dYear = parts[2].toIntOrNull()
            dMonth == month && dYear == year
        } else false
    }
    val totalEarnedThisMonth = monthEarnings.sumOf { it.totalAmount }

    val monthExpenses = expenses.filter {
        val parts = it.dateStr.split("/")
        if (parts.size == 3) {
            val dMonth = parts[1].toIntOrNull()?.minus(1)
            val dYear = parts[2].toIntOrNull()
            dMonth == month && dYear == year
        } else false
    }
    val totalExpensesThisMonth = monthExpenses.sumOf { it.amount }
    val totalProfitThisMonth = totalEarnedThisMonth - totalExpensesThisMonth

    // Work days for this month
    val monthWorkDays = workDays.filter {
        val parts = it.dateStr.split("/")
        if (parts.size == 3) {
            val dMonth = parts[1].toIntOrNull()?.minus(1)
            val dYear = parts[2].toIntOrNull()
            dMonth == month && dYear == year && it.isWorkDay
        } else false
    }
    val totalPlannedDays = monthWorkDays.size

    // Remaining work days: days in this month that are today or future and marked as work day
    val remainingWorkDays = monthWorkDays.count {
        val parts = it.dateStr.split("/")
        val dayNum = parts.getOrNull(0)?.toIntOrNull() ?: 0
        if (year > todayYear) true
        else if (year == todayYear && month > todayMonth) true
        else if (year == todayYear && month == todayMonth) dayNum >= todayDay
        else false
    }

    // Monthly Goal Calculations (Baseada em Lucro Líquido)
    val monthlyGoal = vehicleProfile.monthlyGoal
    val remainingAmount = (monthlyGoal - totalProfitThisMonth).coerceAtLeast(0.0)
    val requiredDailyGoal = if (remainingWorkDays > 0) {
        remainingAmount / remainingWorkDays
    } else {
        if (remainingAmount > 0) remainingAmount else 0.0
    }
    val progressPercent = if (monthlyGoal > 0) {
        (((totalProfitThisMonth.coerceAtLeast(0.0)) / monthlyGoal) * 100).toInt().coerceIn(0, 100)
    } else 0

    val minRatePerKm = 2.10
    val estimatedDailyKm = if (requiredDailyGoal > 0) kotlin.math.ceil(requiredDailyGoal / minRatePerKm) else 0.0

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
                    text = "Planejamento Mensal",
                    fontSize = 24.sp,
                    color = TextWhite,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Defina seus dias de trabalho e acompanhe feriados",
                    fontSize = 12.sp,
                    color = TextSecondary
                )
            }

            IconButton(
                onClick = { showEditGoalDialog = true },
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(CardDark)
            ) {
                Icon(
                    imageVector = Icons.Default.Edit,
                    contentDescription = "Editar Meta",
                    tint = EmeraldGreen,
                    modifier = Modifier.size(20.dp)
                )
            }
        }

        // Summary Goal Card
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = CardHighlight),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Meta Mensal de Lucro Líquido",
                            fontSize = 13.sp,
                            color = TextSecondary,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = MainViewModel.formatCurrency(monthlyGoal),
                            fontSize = 28.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = TextWhite
                        )
                    }

                    Text(
                        text = "$progressPercent%",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = EmeraldGreen
                    )
                }

                LinearProgressIndicator(
                    progress = { progressPercent / 100f },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(10.dp)
                        .clip(RoundedCornerShape(5.dp)),
                    color = EmeraldGreen,
                    trackColor = SurfaceDark
                )

                HorizontalDivider(color = BorderDark, thickness = 0.5.dp)

                // 3 columns: Realizado | Falta | Meta Diária Necessária
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text("Lucro Realizado", fontSize = 11.sp, color = TextSecondary)
                        Text(
                            text = MainViewModel.formatCurrency(totalProfitThisMonth),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (totalProfitThisMonth >= 0) TextWhite else ExpenseRedSoft
                        )
                    }

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Restante", fontSize = 11.sp, color = TextSecondary)
                        Text(
                            text = MainViewModel.formatCurrency(remainingAmount),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (remainingAmount > 0) ExpenseRedSoft else ProfitGreen
                        )
                    }

                    Column(horizontalAlignment = Alignment.End) {
                        Text("Meta diária necessária", fontSize = 11.sp, color = EmeraldGreen, fontWeight = FontWeight.Bold)
                        Text(
                            text = MainViewModel.formatCurrency(requiredDailyGoal),
                            fontSize = 16.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = EmeraldGreen
                        )
                    }
                }

                HorizontalDivider(color = BorderDark, thickness = 0.5.dp)

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Route,
                        contentDescription = null,
                        tint = EmeraldGreen,
                        modifier = Modifier.size(18.dp)
                    )
                    Text(
                        text = "Quilometragem provável: ~${estimatedDailyKm.toInt()} km/dia para atingir a meta diária (considerando piso de R$ 2,10/km).",
                        fontSize = 11.sp,
                        color = TextSecondary
                    )
                }
            }
        }

        // Planning Stats Grid (Dias planejados, Dias restantes, Feriados no mês)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = CardDark),
                modifier = Modifier.weight(1f)
            ) {
                Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text("Dias de trampo", fontSize = 11.sp, color = TextSecondary)
                    Text("$totalPlannedDays dias", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = TextWhite)
                }
            }

            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = CardDark),
                modifier = Modifier.weight(1f)
            ) {
                Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text("Dias restantes", fontSize = 11.sp, color = TextSecondary)
                    Text("$remainingWorkDays dias", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = EmeraldGreen)
                }
            }

            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = CardDark),
                modifier = Modifier.weight(1f)
            ) {
                Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text("Feriados no mês", fontSize = 11.sp, color = TextSecondary)
                    Text("${monthHolidays.size} datas", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = FuelOrange)
                }
            }
        }

        // Month Selector Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(14.dp))
                .background(CardDark)
                .padding(horizontal = 8.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onPreviousMonth) {
                Icon(Icons.Default.ChevronLeft, contentDescription = "Mês anterior", tint = TextWhite)
            }

            Text(
                text = "${MainViewModel.getCurrentMonthName(month)} de $year",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = TextWhite
            )

            IconButton(onClick = onNextMonth) {
                Icon(Icons.Default.ChevronRight, contentDescription = "Próximo mês", tint = TextWhite)
            }
        }

        // Quick schedule buttons
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Button(
                onClick = { onApplySchedule(QuickWorkSchedule.MON_TO_FRI) },
                modifier = Modifier.weight(1f).height(36.dp),
                contentPadding = PaddingValues(horizontal = 4.dp),
                colors = ButtonDefaults.buttonColors(containerColor = CardDark),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text("Seg-Sex", fontSize = 11.sp, color = TextWhite, fontWeight = FontWeight.SemiBold)
            }

            Button(
                onClick = { onApplySchedule(QuickWorkSchedule.MON_TO_SAT) },
                modifier = Modifier.weight(1f).height(36.dp),
                contentPadding = PaddingValues(horizontal = 4.dp),
                colors = ButtonDefaults.buttonColors(containerColor = CardDark),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text("Seg-Sáb", fontSize = 11.sp, color = TextWhite, fontWeight = FontWeight.SemiBold)
            }

            Button(
                onClick = { onApplySchedule(QuickWorkSchedule.ALL_DAYS) },
                modifier = Modifier.weight(1f).height(36.dp),
                contentPadding = PaddingValues(horizontal = 4.dp),
                colors = ButtonDefaults.buttonColors(containerColor = CardDark),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text("Todos", fontSize = 11.sp, color = TextWhite, fontWeight = FontWeight.SemiBold)
            }

            Button(
                onClick = { onApplySchedule(QuickWorkSchedule.CLEAR) },
                modifier = Modifier.weight(1f).height(36.dp),
                contentPadding = PaddingValues(horizontal = 4.dp),
                colors = ButtonDefaults.buttonColors(containerColor = SurfaceDark),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text("Limpar", fontSize = 11.sp, color = ExpenseRedSoft, fontWeight = FontWeight.SemiBold)
            }
        }

        // Calendar Grid Card with Holidays
        Card(
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = CardDark),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Day of Week Header
                val weekDays = listOf("Dom", "Seg", "Ter", "Qua", "Qui", "Sex", "Sáb")
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceAround
                ) {
                    weekDays.forEach { wDay ->
                        Text(
                            text = wDay,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (wDay == "Dom" || wDay == "Sáb") TextSecondary else EmeraldGreen,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                HorizontalDivider(color = BorderDark, thickness = 0.5.dp)

                // Calendar Grid items
                val offset = firstDayOfWeek - 1 // Empty cells before day 1
                val totalCells = offset + daysInMonth
                val rows = (totalCells + 6) / 7

                for (r in 0 until rows) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceAround
                    ) {
                        for (c in 0 until 7) {
                            val cellIndex = r * 7 + c
                            val dayNum = cellIndex - offset + 1

                            if (dayNum in 1..daysInMonth) {
                                val dateStr = String.format(Locale.getDefault(), "%02d/%02d/%04d", dayNum, month + 1, year)
                                val isWork = workDays.any { it.dateStr == dateStr && it.isWorkDay }
                                val isToday = (year == todayYear && month == todayMonth && dayNum == todayDay)
                                val earnedOnDay = dailyEarnings.find { it.dateStr == dateStr }?.totalAmount ?: 0.0
                                val holiday = BrazilianHolidays.getHoliday(dayNum, month + 1, year)

                                val holidayBorderColor = when (holiday?.type) {
                                    HolidayType.NACIONAL -> FuelOrange
                                    HolidayType.ESTADUAL_ACRE -> MetricBlue
                                    HolidayType.MUNICIPAL_RIO_BRANCO -> MetricPurple
                                    HolidayType.FACULTATIVO -> TextSecondary
                                    null -> null
                                }

                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .aspectRatio(1f)
                                        .padding(2.5.dp)
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(
                                            when {
                                                isWork -> EmeraldGreen.copy(alpha = 0.25f)
                                                holiday != null -> holidayBorderColor?.copy(alpha = 0.12f) ?: SurfaceDark
                                                else -> SurfaceDark
                                            }
                                        )
                                        .then(
                                            if (isToday) Modifier.border(1.8.dp, EmeraldGreen, RoundedCornerShape(10.dp))
                                            else if (holidayBorderColor != null) Modifier.border(1.2.dp, holidayBorderColor, RoundedCornerShape(10.dp))
                                            else if (isWork) Modifier.border(1.dp, EmeraldGreen.copy(alpha = 0.4f), RoundedCornerShape(10.dp))
                                            else Modifier
                                        )
                                        .clickable {
                                            if (holiday != null) {
                                                // If tapped and has holiday, show details or toggle work
                                                onToggleWorkDay(dateStr)
                                            } else {
                                                onToggleWorkDay(dateStr)
                                            }
                                        },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Column(
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        verticalArrangement = Arrangement.Center
                                    ) {
                                        Text(
                                            text = "$dayNum",
                                            fontSize = 12.sp,
                                            fontWeight = if (isToday || isWork || holiday != null) FontWeight.Bold else FontWeight.Medium,
                                            color = when {
                                                holiday != null -> holidayBorderColor ?: TextWhite
                                                isWork -> Color.White
                                                else -> TextSecondary
                                            }
                                        )

                                        if (earnedOnDay > 0) {
                                            Box(
                                                modifier = Modifier
                                                    .size(4.dp)
                                                    .clip(CircleShape)
                                                    .background(EmeraldGreen)
                                            )
                                        } else if (holiday != null) {
                                            Text(
                                                text = when (holiday.type) {
                                                    HolidayType.NACIONAL -> "Feriado"
                                                    HolidayType.ESTADUAL_ACRE -> "Acre"
                                                    HolidayType.MUNICIPAL_RIO_BRANCO -> "R.Branco"
                                                    HolidayType.FACULTATIVO -> "Facult."
                                                },
                                                fontSize = 6.5.sp,
                                                color = holidayBorderColor ?: FuelOrange,
                                                fontWeight = FontWeight.Bold,
                                                maxLines = 1
                                            )
                                        } else if (isWork) {
                                            Text(
                                                text = "Trampo",
                                                fontSize = 7.sp,
                                                color = EmeraldGreen,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }
                                }
                            } else {
                                // Empty spacer cell
                                Spacer(modifier = Modifier.weight(1f).aspectRatio(1f))
                            }
                        }
                    }
                }

                // Legend
                Column(
                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        LegendBadge("Dia de Trabalho", EmeraldGreen)
                        LegendBadge("Folga", BorderDark)
                        LegendBadge("Com ganhos", EmeraldGreen, isDot = true)
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        LegendBadge("Feriado Nacional", FuelOrange)
                        LegendBadge("Estadual Acre", MetricBlue)
                        LegendBadge("Municipal R.Branco", MetricPurple)
                    }
                }
            }
        }

        // List of Holidays in this Month with full description and details
        if (monthHolidays.isNotEmpty()) {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = CardDark),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Event,
                            contentDescription = null,
                            tint = FuelOrange,
                            modifier = Modifier.size(20.dp)
                        )
                        Text(
                            text = "Feriados e Pontos Facultativos de ${MainViewModel.getCurrentMonthName(month)}",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextWhite
                        )
                    }

                    monthHolidays.forEach { h ->
                        val (chipBg, chipText) = when (h.type) {
                            HolidayType.NACIONAL -> Pair(FuelOrange.copy(alpha = 0.2f), FuelOrange)
                            HolidayType.ESTADUAL_ACRE -> Pair(MetricBlue.copy(alpha = 0.2f), MetricBlue)
                            HolidayType.MUNICIPAL_RIO_BRANCO -> Pair(MetricPurple.copy(alpha = 0.2f), MetricPurple)
                            HolidayType.FACULTATIVO -> Pair(SurfaceDark, TextSecondary)
                        }

                        Card(
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = SurfaceDark),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { selectedHolidayForDetails = h }
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(38.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(chipBg),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "${h.day}",
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = chipText
                                    )
                                }

                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = h.name,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = TextWhite
                                    )
                                    Text(
                                        text = h.type.displayName,
                                        fontSize = 11.sp,
                                        color = chipText,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                    Text(
                                        text = h.description,
                                        fontSize = 11.sp,
                                        color = TextSecondary,
                                        maxLines = 2
                                    )
                                }

                                Icon(
                                    imageVector = Icons.Default.Info,
                                    contentDescription = "Ver detalhes",
                                    tint = TextSecondary,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))
    }

    // Modal to View Holiday Details
    if (selectedHolidayForDetails != null) {
        val hol = selectedHolidayForDetails!!
        Dialog(onDismissRequest = { selectedHolidayForDetails = null }) {
            Surface(
                shape = RoundedCornerShape(18.dp),
                color = CardDark,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = hol.name,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextWhite
                        )
                        IconButton(onClick = { selectedHolidayForDetails = null }) {
                            Icon(Icons.Default.Close, contentDescription = "Fechar", tint = TextSecondary)
                        }
                    }

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(
                                when (hol.type) {
                                    HolidayType.NACIONAL -> FuelOrange.copy(alpha = 0.2f)
                                    HolidayType.ESTADUAL_ACRE -> MetricBlue.copy(alpha = 0.2f)
                                    HolidayType.MUNICIPAL_RIO_BRANCO -> MetricPurple.copy(alpha = 0.2f)
                                    HolidayType.FACULTATIVO -> SurfaceDark
                                }
                            )
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = "${hol.day}/${hol.month} • ${hol.type.displayName}",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = when (hol.type) {
                                HolidayType.NACIONAL -> FuelOrange
                                HolidayType.ESTADUAL_ACRE -> MetricBlue
                                HolidayType.MUNICIPAL_RIO_BRANCO -> MetricPurple
                                HolidayType.FACULTATIVO -> TextWhite
                            }
                        )
                    }

                    Text(
                        text = hol.description,
                        fontSize = 14.sp,
                        color = TextWhite,
                        lineHeight = 20.sp
                    )

                    Text(
                        text = "Dias de feriado costumam ter aumento de demanda e tarifas dinâmicas nas corridas por aplicativo. Planeje rodar nesses dias para potencializar seu faturamento!",
                        fontSize = 12.sp,
                        color = LightEmeraldGreen,
                        fontWeight = FontWeight.Medium
                    )

                    Button(
                        onClick = { selectedHolidayForDetails = null },
                        colors = ButtonDefaults.buttonColors(containerColor = EmeraldGreen),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("Entendido", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }

    // Dialog to update monthly goal
    if (showEditGoalDialog) {
        var goalInput by remember { mutableStateOf(vehicleProfile.monthlyGoal.toInt().toString()) }

        Dialog(onDismissRequest = { showEditGoalDialog = false }) {
            Surface(
                shape = RoundedCornerShape(18.dp),
                color = CardDark,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = "Definir Meta de Lucro Líquido",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextWhite
                    )

                    OutlinedTextField(
                        value = goalInput,
                        onValueChange = { goalInput = it },
                        label = { Text("Meta de Lucro Líquido Mensal (R$)") },
                        placeholder = { Text("Ex: 5000.00") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = EmeraldGreen,
                            unfocusedBorderColor = BorderDark
                        )
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        TextButton(onClick = { showEditGoalDialog = false }) {
                            Text("Cancelar", color = TextSecondary)
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = {
                                val g = goalInput.replace(",", ".").toDoubleOrNull() ?: 6000.0
                                onUpdateMonthlyGoal(g)
                                showEditGoalDialog = false
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = EmeraldGreen),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text("Salvar Meta", color = Color.White, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun LegendBadge(label: String, color: Color, isDot: Boolean = false) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
        if (isDot) {
            Box(modifier = Modifier.size(6.dp).clip(CircleShape).background(color))
        } else {
            Box(modifier = Modifier.size(10.dp).clip(RoundedCornerShape(3.dp)).background(color))
        }
        Text(label, fontSize = 10.sp, color = TextSecondary)
    }
}
