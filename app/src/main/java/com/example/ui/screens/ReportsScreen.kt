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
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material.icons.outlined.Timer
import androidx.compose.material.icons.outlined.TrendingUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.DailyEarning
import com.example.model.Expense
import com.example.ui.components.DualBarChart
import com.example.ui.components.PeriodTabs
import com.example.ui.theme.*
import com.example.ui.viewmodel.ChartBarPoint
import com.example.ui.viewmodel.MainViewModel
import com.example.ui.viewmodel.PeriodFilter
import java.util.Calendar

@Composable
fun ReportsScreen(
    dailyEarnings: List<DailyEarning>,
    expenses: List<Expense>,
    selectedPeriod: PeriodFilter,
    onPeriodSelected: (PeriodFilter) -> Unit,
    chartData: List<ChartBarPoint>,
    onAddEarningsForDate: (String) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val periodTabs = listOf("Diário", "Semanal", "Mensal", "Anual")
    val currentPeriodIndex = when (selectedPeriod) {
        PeriodFilter.DIARIO -> 0
        PeriodFilter.SEMANAL -> 1
        PeriodFilter.MENSAL -> 2
        PeriodFilter.ANUAL -> 3
    }

    val todayDateStr = MainViewModel.getTodayDateString()
    var selectedDateStr by remember { mutableStateOf(todayDateStr) }
    var showDatePickerDialog by remember { mutableStateOf(false) }

    val cal = Calendar.getInstance()
    val currentYear = cal.get(Calendar.YEAR)
    val currentMonth = cal.get(Calendar.MONTH)
    val currentWeek = cal.get(Calendar.WEEK_OF_YEAR)

    // Filter earnings by period
    val filteredEarnings = when (selectedPeriod) {
        PeriodFilter.DIARIO -> dailyEarnings.filter { it.dateStr == selectedDateStr }
        PeriodFilter.SEMANAL -> dailyEarnings.filter {
            val parts = it.dateStr.split("/")
            if (parts.size == 3) {
                val d = parts[0].toIntOrNull() ?: 1
                val m = parts[1].toIntOrNull()?.minus(1) ?: 0
                val y = parts[2].toIntOrNull() ?: currentYear
                val c = Calendar.getInstance().apply { set(y, m, d) }
                c.get(Calendar.WEEK_OF_YEAR) == currentWeek && c.get(Calendar.YEAR) == currentYear
            } else true
        }
        PeriodFilter.MENSAL -> dailyEarnings.filter {
            val parts = it.dateStr.split("/")
            if (parts.size == 3) {
                val m = parts[1].toIntOrNull()?.minus(1) ?: 0
                val y = parts[2].toIntOrNull() ?: currentYear
                m == currentMonth && y == currentYear
            } else true
        }
        PeriodFilter.ANUAL -> dailyEarnings.filter {
            val parts = it.dateStr.split("/")
            if (parts.size == 3) {
                val y = parts[2].toIntOrNull() ?: currentYear
                y == currentYear
            } else true
        }
    }

    // Filter expenses by period
    val filteredExpenses = when (selectedPeriod) {
        PeriodFilter.DIARIO -> expenses.filter { it.dateStr == selectedDateStr }
        PeriodFilter.SEMANAL -> expenses.filter {
            val parts = it.dateStr.split("/")
            if (parts.size == 3) {
                val d = parts[0].toIntOrNull() ?: 1
                val m = parts[1].toIntOrNull()?.minus(1) ?: 0
                val y = parts[2].toIntOrNull() ?: currentYear
                val c = Calendar.getInstance().apply { set(y, m, d) }
                c.get(Calendar.WEEK_OF_YEAR) == currentWeek && c.get(Calendar.YEAR) == currentYear
            } else true
        }
        PeriodFilter.MENSAL -> expenses.filter {
            val parts = it.dateStr.split("/")
            if (parts.size == 3) {
                val m = parts[1].toIntOrNull()?.minus(1) ?: 0
                val y = parts[2].toIntOrNull() ?: currentYear
                m == currentMonth && y == currentYear
            } else true
        }
        PeriodFilter.ANUAL -> expenses.filter {
            val parts = it.dateStr.split("/")
            if (parts.size == 3) {
                val y = parts[2].toIntOrNull() ?: currentYear
                y == currentYear
            } else true
        }
    }

    // Totals for selected period
    val totalEarnings = filteredEarnings.sumOf { it.totalAmount }
    val totalExpensesAmount = filteredExpenses.sumOf { it.amount }
    val netProfit = totalEarnings - totalExpensesAmount
    val totalKm = filteredEarnings.sumOf { it.kmDriven }
    val totalTrips = filteredEarnings.sumOf { it.tripsCount }
    val ratePerKm = if (totalKm > 0) totalEarnings / totalKm else 0.0

    // Work Time Analytics
    val totalWorkedMinutes = filteredEarnings.sumOf { it.calculatedMinutes }
    val totalWorkedHours = totalWorkedMinutes / 60.0
    val daysWithLoggedStages = filteredEarnings.count { it.calculatedMinutes > 0 }
    val avgHoursPerDay = if (daysWithLoggedStages > 0) totalWorkedHours / daysWithLoggedStages else 0.0
    val ratePerHour = if (totalWorkedHours > 0) totalEarnings / totalWorkedHours else 0.0
    val netProfitPerHour = if (totalWorkedHours > 0) netProfit / totalWorkedHours else 0.0
    val tripsPerHour = if (totalWorkedHours > 0) totalTrips / totalWorkedHours else 0.0
    val kmPerHour = if (totalWorkedHours > 0) totalKm / totalWorkedHours else 0.0

    val formattedTotalWorkedTime = if (totalWorkedMinutes <= 0) "0h" else {
        val h = totalWorkedMinutes / 60
        val m = totalWorkedMinutes % 60
        if (m == 0) "${h}h" else "${h}h ${m}min"
    }

    // Category breakdown
    val fuelExpense = filteredExpenses.filter { it.category.equals("combustível", ignoreCase = true) || it.category.equals("combustivel", ignoreCase = true) }.sumOf { it.amount }
    val foodExpense = filteredExpenses.filter { it.category.equals("alimentação", ignoreCase = true) || it.category.equals("alimentacao", ignoreCase = true) }.sumOf { it.amount }
    val maintenanceExpense = filteredExpenses.filter { it.category.equals("manutenção", ignoreCase = true) || it.category.equals("manutencao", ignoreCase = true) }.sumOf { it.amount }

    val fuelPercent = if (totalExpensesAmount > 0) ((fuelExpense / totalExpensesAmount) * 100).toInt() else 0
    val foodPercent = if (totalExpensesAmount > 0) ((foodExpense / totalExpensesAmount) * 100).toInt() else 0
    val maintenancePercent = if (totalExpensesAmount > 0) ((maintenanceExpense / totalExpensesAmount) * 100).toInt() else 0

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(BackgroundDark)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Title
        Text(
            text = "Relatórios",
            fontSize = 24.sp,
            color = TextWhite,
            fontWeight = FontWeight.Bold
        )

        // Period filter tabs
        PeriodTabs(
            options = periodTabs,
            selectedIndex = currentPeriodIndex,
            onSelect = { idx ->
                val filter = when (idx) {
                    0 -> PeriodFilter.DIARIO
                    1 -> PeriodFilter.SEMANAL
                    2 -> PeriodFilter.MENSAL
                    else -> PeriodFilter.ANUAL
                }
                onPeriodSelected(filter)
            }
        )

        // Date Selector & Day Navigator
        Surface(
            shape = RoundedCornerShape(14.dp),
            color = CardDark,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (selectedPeriod == PeriodFilter.DIARIO) {
                    IconButton(
                        onClick = {
                            val sdf = java.text.SimpleDateFormat("dd/MM/yyyy", java.util.Locale.getDefault())
                            try {
                                val d = sdf.parse(selectedDateStr) ?: java.util.Date()
                                val c = Calendar.getInstance().apply { time = d }
                                c.add(Calendar.DAY_OF_YEAR, -1)
                                selectedDateStr = sdf.format(c.time)
                            } catch (e: Exception) {
                                // fallback
                            }
                        }
                    ) {
                        Icon(Icons.Default.ChevronLeft, contentDescription = "Dia Anterior", tint = EmeraldGreen)
                    }
                } else {
                    Spacer(modifier = Modifier.width(40.dp))
                }

                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .clickable { showDatePickerDialog = true }
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.CalendarToday,
                        contentDescription = null,
                        tint = EmeraldGreen,
                        modifier = Modifier.size(18.dp)
                    )
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = when (selectedPeriod) {
                                PeriodFilter.DIARIO -> {
                                    if (selectedDateStr == todayDateStr) "Hoje • $selectedDateStr"
                                    else "Dia: $selectedDateStr"
                                }
                                PeriodFilter.SEMANAL -> "Esta Semana"
                                PeriodFilter.MENSAL -> "Este Mês (${MainViewModel.getCurrentMonthName(currentMonth)})"
                                PeriodFilter.ANUAL -> "Este Ano ($currentYear)"
                            },
                            fontSize = 14.sp,
                            color = TextWhite,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = if (selectedPeriod == PeriodFilter.DIARIO) "Toque para escolher uma data específica" else "Toque para ver períodos",
                            fontSize = 11.sp,
                            color = EmeraldGreen
                        )
                    }
                }

                if (selectedPeriod == PeriodFilter.DIARIO) {
                    IconButton(
                        onClick = {
                            val sdf = java.text.SimpleDateFormat("dd/MM/yyyy", java.util.Locale.getDefault())
                            try {
                                val d = sdf.parse(selectedDateStr) ?: java.util.Date()
                                val c = Calendar.getInstance().apply { time = d }
                                c.add(Calendar.DAY_OF_YEAR, 1)
                                selectedDateStr = sdf.format(c.time)
                            } catch (e: Exception) {
                                // fallback
                            }
                        }
                    ) {
                        Icon(Icons.Default.ChevronRight, contentDescription = "Próximo Dia", tint = EmeraldGreen)
                    }
                } else {
                    Spacer(modifier = Modifier.width(40.dp))
                }
            }
        }

        // Resumo no período
        Text(
            text = "Resumo financeiro",
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = TextWhite
        )

        // 2x2 Grid of Summary Cards
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Ganhos
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = CardDark),
                modifier = Modifier.weight(1f)
            ) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(24.dp)
                                .clip(CircleShape)
                                .background(EmeraldGreen.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Default.AttachMoney,
                                contentDescription = null,
                                tint = EmeraldGreen,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                        Text("Ganhos", fontSize = 12.sp, color = TextSecondary)
                    }
                    Text(
                        text = MainViewModel.formatCurrency(totalEarnings),
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        color = ProfitGreen
                    )
                }
            }

            // Despesas
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = CardDark),
                modifier = Modifier.weight(1f)
            ) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(24.dp)
                                .clip(CircleShape)
                                .background(ExpenseRed.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Default.TrendingDown,
                                contentDescription = null,
                                tint = ExpenseRed,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                        Text("Despesas", fontSize = 12.sp, color = TextSecondary)
                    }
                    Text(
                        text = MainViewModel.formatCurrency(totalExpensesAmount),
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        color = ExpenseRedSoft
                    )
                }
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Lucro Líquido
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = CardDark),
                modifier = Modifier.weight(1f)
            ) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(24.dp)
                                .clip(CircleShape)
                                .background(EmeraldGreen.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Default.TrendingUp,
                                contentDescription = null,
                                tint = EmeraldGreen,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                        Text("Lucro líquido", fontSize = 12.sp, color = TextSecondary)
                    }
                    Text(
                        text = MainViewModel.formatCurrency(netProfit),
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (netProfit >= 0) ProfitGreen else ExpenseRedSoft
                    )
                }
            }

            // Km Rodados
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = CardDark),
                modifier = Modifier.weight(1f)
            ) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(24.dp)
                                .clip(CircleShape)
                                .background(MetricBlue.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Default.Speed,
                                contentDescription = null,
                                tint = MetricBlue,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                        Text("Km rodados", fontSize = 12.sp, color = TextSecondary)
                    }
                    Text(
                        text = MainViewModel.formatKm(totalKm),
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextWhite
                    )
                }
            }
        }

        // Dual Bar Chart
        DualBarChart(dataPoints = chartData)

        // Top Categorias de Despesas
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = CardDark),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Text(
                    text = "Top categorias de despesas",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextWhite
                )

                // Item 1: Combustível
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Icon(Icons.Default.LocalGasStation, contentDescription = null, tint = FuelOrange, modifier = Modifier.size(16.dp))
                            Text("Combustível / Energia", color = TextWhite, fontSize = 13.sp)
                        }
                        Text("$fuelPercent%", color = TextSecondary, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    }
                    LinearProgressIndicator(
                        progress = { fuelPercent / 100f },
                        modifier = Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(3.dp)),
                        color = FuelOrange,
                        trackColor = SurfaceDark
                    )
                }

                // Item 2: Alimentação
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Icon(Icons.Default.Restaurant, contentDescription = null, tint = ExpenseRed, modifier = Modifier.size(16.dp))
                            Text("Alimentação", color = TextWhite, fontSize = 13.sp)
                        }
                        Text("$foodPercent%", color = TextSecondary, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    }
                    LinearProgressIndicator(
                        progress = { foodPercent / 100f },
                        modifier = Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(3.dp)),
                        color = ExpenseRed,
                        trackColor = SurfaceDark
                    )
                }

                // Item 3: Manutenção
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Icon(Icons.Default.Build, contentDescription = null, tint = MetricBlue, modifier = Modifier.size(16.dp))
                            Text("Manutenção", color = TextWhite, fontSize = 13.sp)
                        }
                        Text("$maintenancePercent%", color = TextSecondary, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    }
                    LinearProgressIndicator(
                        progress = { maintenancePercent / 100f },
                        modifier = Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(3.dp)),
                        color = MetricBlue,
                        trackColor = SurfaceDark
                    )
                }
            }
        }

        // Efficiency Metrics with Tabela Fixa R$ 2,00/km
        val standardTableRate = 2.00
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = CardDark),
                modifier = Modifier.weight(1f)
            ) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text("Média R$/km no Período", fontSize = 12.sp, color = TextSecondary)
                    Text(
                        text = if (totalKm > 0) String.format("R$ %.2f/km", ratePerKm).replace(".", ",") else "R$ 2,00/km",
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (totalKm > 0 && ratePerKm >= standardTableRate) ProfitGreen else EmeraldGreen
                    )
                    Text(
                        text = if (totalKm > 0) {
                            if (ratePerKm >= standardTableRate) "✓ Acima da tabela R$ 2,00" else "⚠ Abaixo da tabela R$ 2,00"
                        } else "Tabela padrão: R$ 2,00/km",
                        fontSize = 10.sp,
                        color = if (totalKm > 0 && ratePerKm < standardTableRate) FuelOrange else EmeraldGreen
                    )
                }
            }
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = CardDark),
                modifier = Modifier.weight(1f)
            ) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text("Total de Corridas", fontSize = 12.sp, color = TextSecondary)
                    Text("$totalTrips corridas", fontSize = 17.sp, fontWeight = FontWeight.Bold, color = TextWhite)
                    Text(
                        text = if (filteredEarnings.isNotEmpty()) "${filteredEarnings.size} dias com registros" else "Nenhum registro",
                        fontSize = 10.sp,
                        color = TextSecondary
                    )
                }
            }
        }

        // Relatório de Tempo de Trabalho & Produtividade
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = CardDark),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(EmeraldGreen.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Schedule,
                            contentDescription = null,
                            tint = EmeraldGreen,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Column {
                        Text(
                            text = "Tempo de Trabalho & Produtividade",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextWhite
                        )
                        Text(
                            text = "Cálculo real das horas trabalhadas por etapas",
                            fontSize = 11.sp,
                            color = TextSecondary
                        )
                    }
                }

                HorizontalDivider(color = BorderDark, thickness = 0.5.dp)

                // Grid 1: Total Hours & Daily Average
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = SurfaceDark,
                        modifier = Modifier.weight(1f)
                    ) {
                        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text("Horas Trabalhadas", fontSize = 11.sp, color = TextSecondary)
                            Text(
                                text = formattedTotalWorkedTime,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = EmeraldGreen
                            )
                            Text(
                                text = if (daysWithLoggedStages > 0) "$daysWithLoggedStages dias com jornada" else "Nenhuma etapa lançada",
                                fontSize = 10.sp,
                                color = TextSecondary
                            )
                        }
                    }

                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = SurfaceDark,
                        modifier = Modifier.weight(1f)
                    ) {
                        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text("Média por Dia", fontSize = 11.sp, color = TextSecondary)
                            Text(
                                text = if (daysWithLoggedStages > 0) String.format(java.util.Locale.getDefault(), "%.1fh/dia", avgHoursPerDay) else "0h/dia",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = TextWhite
                            )
                            Text(
                                text = "Média de dias trabalhados",
                                fontSize = 10.sp,
                                color = TextSecondary
                            )
                        }
                    }
                }

                // Grid 2: Hourly Rate & Net Profit per Hour
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = SurfaceDark,
                        modifier = Modifier.weight(1f)
                    ) {
                        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text("Ganho Bruto / Hora", fontSize = 11.sp, color = TextSecondary)
                            Text(
                                text = if (totalWorkedHours > 0) String.format(java.util.Locale.getDefault(), "R$ %.2f/h", ratePerHour).replace(".", ",") else "—",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = ProfitGreen
                            )
                            Text(
                                text = "Faturamento por hora",
                                fontSize = 10.sp,
                                color = TextSecondary
                            )
                        }
                    }

                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = SurfaceDark,
                        modifier = Modifier.weight(1f)
                    ) {
                        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text("Lucro Líquido / Hora", fontSize = 11.sp, color = TextSecondary)
                            Text(
                                text = if (totalWorkedHours > 0) String.format(java.util.Locale.getDefault(), "R$ %.2f/h", netProfitPerHour).replace(".", ",") else "—",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (netProfitPerHour >= 0) EmeraldGreen else ExpenseRedSoft
                            )
                            Text(
                                text = "Após descontar custos",
                                fontSize = 10.sp,
                                color = TextSecondary
                            )
                        }
                    }
                }

                // Grid 3: Speed & Trips per hour
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = SurfaceDark,
                        modifier = Modifier.weight(1f)
                    ) {
                        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text("Velocidade Média", fontSize = 11.sp, color = TextSecondary)
                            Text(
                                text = if (totalWorkedHours > 0) String.format(java.util.Locale.getDefault(), "%.1f km/h", kmPerHour).replace(".", ",") else "—",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = MetricBlue
                            )
                            Text("Km rodados / hora", fontSize = 10.sp, color = TextSecondary)
                        }
                    }

                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = SurfaceDark,
                        modifier = Modifier.weight(1f)
                    ) {
                        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text("Ritmo de Corridas", fontSize = 11.sp, color = TextSecondary)
                            Text(
                                text = if (totalWorkedHours > 0) String.format(java.util.Locale.getDefault(), "%.2f /h", tripsPerHour).replace(".", ",") else "—",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = MetricPurple
                            )
                            Text("Corridas feitas / hora", fontSize = 10.sp, color = TextSecondary)
                        }
                    }
                }
            }
        }

        // Histórico Detalhado dos Dias Trabalhados e Horas
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = CardDark),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Histórico de Dias & Etapas",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextWhite
                    )
                    TextButton(onClick = { onAddEarningsForDate("") }) {
                        Icon(Icons.Default.Add, contentDescription = null, tint = EmeraldGreen, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("+ Lançar Dia", color = EmeraldGreen, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }

                if (filteredEarnings.isEmpty()) {
                    Text(
                        text = "Nenhum dia lançado neste período. Use o botão acima para adicionar ganhos de dias passados.",
                        fontSize = 12.sp,
                        color = TextSecondary,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                } else {
                    filteredEarnings.sortedByDescending { it.id }.forEach { earning ->
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = SurfaceDark,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onAddEarningsForDate(earning.dateStr) }
                        ) {
                            Column(
                                modifier = Modifier.padding(12.dp),
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Icon(Icons.Default.Event, contentDescription = null, tint = EmeraldGreen, modifier = Modifier.size(16.dp))
                                        Text(
                                            text = earning.dateStr,
                                            fontWeight = FontWeight.Bold,
                                            color = TextWhite,
                                            fontSize = 13.sp
                                        )
                                    }

                                    Surface(
                                        color = EmeraldGreen.copy(alpha = 0.2f),
                                        shape = RoundedCornerShape(6.dp)
                                    ) {
                                        Text(
                                            text = "⏱ ${earning.formattedWorkedTime}",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = EmeraldGreen,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                    }
                                }

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = "${MainViewModel.formatCurrency(earning.totalAmount)} • ${earning.tripsCount} corridas • ${MainViewModel.formatKm(earning.kmDriven)}",
                                        fontSize = 12.sp,
                                        color = ProfitGreen,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                    val dayRate = if (earning.kmDriven > 0) earning.totalAmount / earning.kmDriven else 0.0
                                    Text(
                                        text = if (dayRate > 0) String.format("R$ %.2f/km", dayRate).replace(".", ",") else "—",
                                        fontSize = 12.sp,
                                        color = if (dayRate >= 2.00) EmeraldGreen else FuelOrange,
                                        fontWeight = FontWeight.Bold
                                    )
                                }

                                if (earning.startTime.isNotBlank() || earning.endTime.isNotBlank()) {
                                    Text(
                                        text = "Etapas: Início ${earning.startTime.ifBlank { "--:--" }} • Pausa ${earning.lunchStartTime.ifBlank { "--:--" }} • Retorno ${earning.lunchEndTime.ifBlank { "--:--" }} • Fim ${earning.endTime.ifBlank { "--:--" }}",
                                        fontSize = 10.5.sp,
                                        color = TextSecondary
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // Action Button: Lançar / Editar Faturamento Desta Data
        OutlinedButton(
            onClick = { onAddEarningsForDate(selectedDateStr) },
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = EmeraldGreen),
            border = androidx.compose.foundation.BorderStroke(1.dp, EmeraldGreen)
        ) {
            Icon(Icons.Default.EditCalendar, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = if (filteredEarnings.isNotEmpty()) "Editar Informações de $selectedDateStr" else "Lançar Informações de $selectedDateStr",
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp
            )
        }

        Spacer(modifier = Modifier.height(20.dp))
    }

    // Modal to Select Specific Date for Report
    if (showDatePickerDialog) {
        var dateTextInput by remember { mutableStateOf(selectedDateStr) }
        val yesterdayStr = remember {
            val c = Calendar.getInstance()
            c.add(Calendar.DAY_OF_YEAR, -1)
            java.text.SimpleDateFormat("dd/MM/yyyy", java.util.Locale.getDefault()).format(c.time)
        }
        val dayBeforeYesterdayStr = remember {
            val c = Calendar.getInstance()
            c.add(Calendar.DAY_OF_YEAR, -2)
            java.text.SimpleDateFormat("dd/MM/yyyy", java.util.Locale.getDefault()).format(c.time)
        }

        val datesWithData = (dailyEarnings.map { it.dateStr } + expenses.map { it.dateStr })
            .distinct()
            .take(6)

        AlertDialog(
            onDismissRequest = { showDatePickerDialog = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Icon(Icons.Default.CalendarToday, contentDescription = null, tint = EmeraldGreen)
                    Text("Selecionar Data do Relatório", fontWeight = FontWeight.Bold, color = TextWhite, fontSize = 18.sp)
                }
            },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Text(
                        text = "Escolha um dia rápido ou digite a data para visualizar o relatório detalhado de faturamento e despesas:",
                        fontSize = 12.sp,
                        color = TextSecondary
                    )

                    // Quick Chips
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        FilterChip(
                            selected = dateTextInput == todayDateStr,
                            onClick = { dateTextInput = todayDateStr },
                            label = { Text("Hoje", fontSize = 11.sp) },
                            colors = FilterChipDefaults.filterChipColors(selectedContainerColor = EmeraldGreen, selectedLabelColor = Color.White)
                        )
                        FilterChip(
                            selected = dateTextInput == yesterdayStr,
                            onClick = { dateTextInput = yesterdayStr },
                            label = { Text("Ontem", fontSize = 11.sp) },
                            colors = FilterChipDefaults.filterChipColors(selectedContainerColor = EmeraldGreen, selectedLabelColor = Color.White)
                        )
                        FilterChip(
                            selected = dateTextInput == dayBeforeYesterdayStr,
                            onClick = { dateTextInput = dayBeforeYesterdayStr },
                            label = { Text("Anteontem", fontSize = 11.sp) },
                            colors = FilterChipDefaults.filterChipColors(selectedContainerColor = EmeraldGreen, selectedLabelColor = Color.White)
                        )
                    }

                    OutlinedTextField(
                        value = dateTextInput,
                        onValueChange = { dateTextInput = it },
                        label = { Text("Data (DD/MM/AAAA)") },
                        placeholder = { Text("Ex: 14/10/2026") },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = EmeraldGreen,
                            unfocusedBorderColor = BorderDark,
                            focusedTextColor = TextWhite,
                            unfocusedTextColor = TextWhite,
                            focusedContainerColor = SurfaceDark,
                            unfocusedContainerColor = SurfaceDark
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )

                    if (datesWithData.isNotEmpty()) {
                        Text(
                            text = "Datas com lançamentos gravados:",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = TextSecondary
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            datesWithData.take(3).forEach { dStr ->
                                SuggestionChip(
                                    onClick = { dateTextInput = dStr },
                                    label = { Text(dStr, fontSize = 11.sp, color = TextWhite) },
                                    colors = SuggestionChipDefaults.suggestionChipColors(containerColor = SurfaceDark)
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val clean = dateTextInput.trim()
                        if (clean.isNotBlank()) {
                            selectedDateStr = clean
                            onPeriodSelected(PeriodFilter.DIARIO)
                        }
                        showDatePickerDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = EmeraldGreen)
                ) {
                    Text("Visualizar Relatório", color = Color.White, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePickerDialog = false }) {
                    Text("Cancelar", color = TextSecondary)
                }
            },
            containerColor = CardDark
        )
    }
}
