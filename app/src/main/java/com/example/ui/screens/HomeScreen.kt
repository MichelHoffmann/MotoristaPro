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
import androidx.compose.material.icons.outlined.Person
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
import com.example.model.Expense
import com.example.model.Trip
import com.example.model.VehicleProfile
import com.example.ui.components.PlatformBadge
import com.example.ui.theme.*
import com.example.ui.viewmodel.MainViewModel

@Composable
fun HomeScreen(
    trips: List<Trip>,
    expenses: List<Expense>,
    vehicleProfile: VehicleProfile,
    onAddTripClick: () -> Unit,
    onAddExpenseClick: () -> Unit,
    onNavigateToTrips: () -> Unit,
    onNavigateToExpenses: () -> Unit,
    onOpenProfileMenu: () -> Unit,
    modifier: Modifier = Modifier
) {
    val todayDateStr = MainViewModel.getTodayDateString()
    val todayTrips = trips.filter { it.dateStr == todayDateStr || (trips.isNotEmpty() && it.dateStr == trips.first().dateStr) }
    val todayExpenses = expenses.filter { it.dateStr == todayDateStr || (expenses.isNotEmpty() && it.dateStr == expenses.first().dateStr) }

    // Real dynamic totals (starts at zero if no records exist)
    val totalEarnings = todayTrips.sumOf { it.netAmount }
    val totalExpensesAmount = todayExpenses.sumOf { it.amount }
    val netProfit = totalEarnings - totalExpensesAmount
    val tripsCount = todayTrips.size
    val totalKm = todayTrips.sumOf { it.distanceKm }
    val ratePerKm = if (totalKm > 0) totalEarnings / totalKm else 0.0

    val goal = if (vehicleProfile.dailyGoal > 0) vehicleProfile.dailyGoal else 250.0
    val progressPercent = if (goal > 0) ((totalEarnings / goal) * 100).toInt().coerceIn(0, 100) else 0

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

            // Profile / New User Menu button
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
                    tint = EmeraldGreen,
                    modifier = Modifier.size(22.dp)
                )
            }
        }

        // Vehicle Info Badge
        if (vehicleProfile.modelName.isNotBlank()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(CardDark.copy(alpha = 0.6f))
                    .clickable { onOpenProfileMenu() }
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = if (vehicleProfile.isElectric) Icons.Default.ElectricCar else Icons.Default.DirectionsCar,
                        contentDescription = null,
                        tint = EmeraldGreen,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = "${vehicleProfile.modelName} ${vehicleProfile.manufacturingYear} • ${vehicleProfile.fuelType}",
                        fontSize = 12.sp,
                        color = TextWhite,
                        fontWeight = FontWeight.Medium
                    )
                }

                Text(
                    text = if (vehicleProfile.isElectric) "${vehicleProfile.averageConsumption} km/kWh" else "${vehicleProfile.averageConsumption} km/L",
                    fontSize = 11.sp,
                    color = TextSecondary
                )
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
                    text = MainViewModel.formatCurrency(netProfit),
                    fontSize = 34.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = if (netProfit >= 0) ProfitGreen else ExpenseRedSoft
                )

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = "Ganhos: ${MainViewModel.formatCurrency(totalEarnings)}",
                        fontSize = 13.sp,
                        color = TextWhite,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(text = "|", color = TextMuted, fontSize = 13.sp)
                    Text(
                        text = "Despesas: ${MainViewModel.formatCurrency(totalExpensesAmount)}",
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
            // Corridas
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
                        text = "$tripsCount",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextWhite
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
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text("Km rodados", fontSize = 12.sp, color = TextSecondary)
                    Text(
                        text = MainViewModel.formatKm(totalKm),
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextWhite
                    )
                }
            }

            // R$/km
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
                        text = String.format("R$ %.2f", ratePerKm).replace(".", ","),
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextWhite
                    )
                }
            }
        }

        // Meta do Dia Card
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
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Meta do dia (${MainViewModel.formatCurrency(goal)})",
                        fontSize = 14.sp,
                        color = TextWhite,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = "$progressPercent%",
                        fontSize = 14.sp,
                        color = EmeraldGreen,
                        fontWeight = FontWeight.Bold
                    )
                }

                LinearProgressIndicator(
                    progress = { progressPercent / 100f },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(RoundedCornerShape(4.dp)),
                    color = EmeraldGreen,
                    trackColor = SurfaceDark
                )

                val remaining = (goal - totalEarnings).coerceAtLeast(0.0)
                if (remaining > 0) {
                    Text(
                        text = "Faltam ${MainViewModel.formatCurrency(remaining)} para bater a meta de hoje",
                        fontSize = 12.sp,
                        color = TextSecondary
                    )
                } else {
                    Text(
                        text = "Parabéns! Meta do dia atingida! 🎉",
                        fontSize = 12.sp,
                        color = EmeraldGreen,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }

        // Quick Action Buttons
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Button(
                onClick = onAddTripClick,
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
                    text = "Registrar ganho",
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

        // Recent Trips Section
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Últimas corridas",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = TextWhite
            )
            if (todayTrips.isNotEmpty()) {
                Text(
                    text = "Ver todas",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = EmeraldGreen,
                    modifier = Modifier.clickable { onNavigateToTrips() }
                )
            }
        }

        if (todayTrips.isEmpty()) {
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = CardDark),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.DirectionsCar,
                        contentDescription = null,
                        tint = TextSecondary,
                        modifier = Modifier.size(32.dp)
                    )
                    Text(
                        text = "Nenhuma corrida registrada hoje",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = TextWhite
                    )
                    Text(
                        text = "Toque em '+ Registrar ganho' acima para lançar sua primeira corrida do dia.",
                        fontSize = 12.sp,
                        color = TextSecondary,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                }
            }
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                todayTrips.take(4).forEach { trip ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(CardDark)
                            .padding(horizontal = 14.dp, vertical = 12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            PlatformBadge(platform = trip.platform)
                            Column {
                                Text(
                                    text = MainViewModel.formatCurrency(trip.netAmount),
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TextWhite
                                )
                                Text(
                                    text = "${trip.distanceKm} km  •  ${trip.durationMinutes} min",
                                    fontSize = 12.sp,
                                    color = TextSecondary
                                )
                            }
                        }

                        Text(
                            text = trip.timeStr,
                            fontSize = 13.sp,
                            color = TextSecondary,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}
