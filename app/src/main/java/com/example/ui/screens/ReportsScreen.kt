package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.Expense
import com.example.model.Trip
import com.example.ui.components.DualBarChart
import com.example.ui.components.PeriodTabs
import com.example.ui.theme.*
import com.example.ui.viewmodel.ChartBarPoint
import com.example.ui.viewmodel.MainViewModel
import com.example.ui.viewmodel.PeriodFilter

@Composable
fun ReportsScreen(
    trips: List<Trip>,
    expenses: List<Expense>,
    selectedPeriod: PeriodFilter,
    onPeriodSelected: (PeriodFilter) -> Unit,
    chartData: List<ChartBarPoint>,
    modifier: Modifier = Modifier
) {
    val periodTabs = listOf("Diário", "Semanal", "Mensal", "Anual")
    val currentPeriodIndex = when (selectedPeriod) {
        PeriodFilter.DIARIO -> 0
        PeriodFilter.SEMANAL -> 1
        PeriodFilter.MENSAL -> 2
        PeriodFilter.ANUAL -> 3
    }

    // Real dynamic totals from database (defaults to 0 when empty)
    val totalEarnings = trips.sumOf { it.netAmount }
    val totalExpensesAmount = expenses.sumOf { it.amount }
    val netProfit = totalEarnings - totalExpensesAmount
    val totalKm = trips.sumOf { it.distanceKm }
    val ratePerKm = if (totalKm > 0) totalEarnings / totalKm else 0.0

    // Category breakdown
    val fuelExpense = expenses.filter { it.category.equals("combustível", ignoreCase = true) || it.category.equals("combustivel", ignoreCase = true) }.sumOf { it.amount }
    val foodExpense = expenses.filter { it.category.equals("alimentação", ignoreCase = true) || it.category.equals("alimentacao", ignoreCase = true) }.sumOf { it.amount }
    val maintenanceExpense = expenses.filter { it.category.equals("manutenção", ignoreCase = true) || it.category.equals("manutencao", ignoreCase = true) }.sumOf { it.amount }

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

        // Date selector box
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(CardDark)
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.CalendarToday,
                    contentDescription = null,
                    tint = TextSecondary,
                    modifier = Modifier.size(18.dp)
                )
                Text(
                    text = when (selectedPeriod) {
                        PeriodFilter.DIARIO -> "Hoje - ${MainViewModel.getTodayDateString()}"
                        PeriodFilter.SEMANAL -> "Esta Semana"
                        PeriodFilter.MENSAL -> "Este Mês"
                        PeriodFilter.ANUAL -> "Este Ano"
                    },
                    fontSize = 13.sp,
                    color = TextWhite,
                    fontWeight = FontWeight.SemiBold
                )
            }
            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = null,
                tint = TextSecondary,
                modifier = Modifier.size(20.dp)
            )
        }

        // Resumo no período
        Text(
            text = "Resumo no período",
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

        // Efficiency Metrics
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = CardDark),
                modifier = Modifier.weight(1f)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text("Ganho por km", fontSize = 12.sp, color = TextSecondary)
                    Text(String.format("R$ %.2f/km", ratePerKm).replace(".", ","), fontSize = 16.sp, fontWeight = FontWeight.Bold, color = EmeraldGreen)
                }
            }
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = CardDark),
                modifier = Modifier.weight(1f)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text("Total Corridas", fontSize = 12.sp, color = TextSecondary)
                    Text("${trips.size} corridas", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = EmeraldGreen)
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))
    }
}
