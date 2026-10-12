package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.Trip
import com.example.ui.components.PlatformBadge
import com.example.ui.components.SubFilterTabs
import com.example.ui.theme.*
import com.example.ui.viewmodel.MainViewModel
import com.example.ui.viewmodel.TripTabFilter

@Composable
fun TripsScreen(
    trips: List<Trip>,
    selectedFilter: TripTabFilter,
    onFilterSelected: (TripTabFilter) -> Unit,
    onAddTripClick: () -> Unit,
    onDeleteTrip: (Trip) -> Unit,
    modifier: Modifier = Modifier
) {
    val filterTabs = listOf("Histórico", "Por app", "Por período")
    val selectedIndex = when (selectedFilter) {
        TripTabFilter.HISTORICO -> 0
        TripTabFilter.POR_APP -> 1
        TripTabFilter.POR_PERIODO -> 2
    }

    var selectedAppFilter by remember { mutableStateOf<String?>(null) }

    val displayedTrips = remember(trips, selectedFilter, selectedAppFilter) {
        when (selectedFilter) {
            TripTabFilter.POR_APP -> {
                if (selectedAppFilter == null) trips
                else trips.filter { it.platform.equals(selectedAppFilter, ignoreCase = true) }
            }
            else -> trips
        }
    }

    // Real summary values (starts at zero if empty)
    val totalEarnings = displayedTrips.sumOf { it.netAmount }
    val totalCount = displayedTrips.size
    val totalDist = displayedTrips.sumOf { it.distanceKm }
    val ratePerKm = if (totalDist > 0) totalEarnings / totalDist else 0.0

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(BackgroundDark)
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            contentPadding = PaddingValues(top = 12.dp, bottom = 80.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Header
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Corridas",
                        fontSize = 24.sp,
                        color = TextWhite,
                        fontWeight = FontWeight.Bold
                    )
                    IconButton(
                        onClick = onAddTripClick,
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(EmeraldGreen)
                            .testTag("add_trip_icon_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Nova corrida",
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }

            // Sub filter tabs
            item {
                SubFilterTabs(
                    tabs = filterTabs,
                    selectedIndex = selectedIndex,
                    onTabSelected = { idx ->
                        val f = when (idx) {
                            0 -> TripTabFilter.HISTORICO
                            1 -> TripTabFilter.POR_APP
                            else -> TripTabFilter.POR_PERIODO
                        }
                        onFilterSelected(f)
                    }
                )
            }

            // App Filter Chips if "Por app" selected
            if (selectedFilter == TripTabFilter.POR_APP) {
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf("Todos", "99", "Uber", "InDrive", "Particular").forEach { appName ->
                            val isSelected = (appName == "Todos" && selectedAppFilter == null) ||
                                    (selectedAppFilter?.equals(appName, ignoreCase = true) == true)
                            FilterChip(
                                selected = isSelected,
                                onClick = {
                                    selectedAppFilter = if (appName == "Todos") null else appName
                                },
                                label = { Text(appName, fontSize = 12.sp) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = EmeraldGreen,
                                    selectedLabelColor = Color.White,
                                    containerColor = CardDark,
                                    labelColor = TextSecondary
                                )
                            )
                        }
                    }
                }
            }

            // Date Group Header
            item {
                Text(
                    text = "26/05/2025",
                    fontSize = 15.sp,
                    color = TextWhite,
                    fontWeight = FontWeight.SemiBold
                )
            }

            // Summary Header Card
            item {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = CardDark),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("Ganho total", fontSize = 11.sp, color = TextSecondary)
                            Text(
                                text = MainViewModel.formatCurrency(totalEarnings),
                                fontSize = 17.sp,
                                fontWeight = FontWeight.Bold,
                                color = ProfitGreen
                            )
                        }

                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("Corridas", fontSize = 11.sp, color = TextSecondary)
                            Text(
                                text = "$totalCount",
                                fontSize = 17.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextWhite
                            )
                        }

                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("Distância", fontSize = 11.sp, color = TextSecondary)
                            Text(
                                text = MainViewModel.formatKm(totalDist),
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextWhite
                            )
                        }

                        Column(horizontalAlignment = Alignment.End) {
                            Text("R$/km", fontSize = 11.sp, color = TextSecondary)
                            Text(
                                text = String.format("R$ %.2f", ratePerKm).replace(".", ","),
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextWhite
                            )
                        }
                    }
                }
            }

            if (displayedTrips.isEmpty()) {
                item {
                    Card(
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = CardDark),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.DirectionsCar,
                                contentDescription = null,
                                tint = TextSecondary,
                                modifier = Modifier.size(36.dp)
                            )
                            Text(
                                text = "Nenhuma corrida cadastrada",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = TextWhite
                            )
                            Text(
                                text = "Toque no botão '+' abaixo para registrar sua primeira corrida de app.",
                                fontSize = 12.sp,
                                color = TextSecondary,
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                        }
                    }
                }
            } else {
                // Trips List
                items(displayedTrips, key = { it.id }) { trip ->
                    TripItemRow(trip = trip, onDelete = { onDeleteTrip(trip) })
                }
            }
        }

        // Floating Action Button
        FloatingActionButton(
            onClick = onAddTripClick,
            containerColor = EmeraldGreen,
            contentColor = Color.White,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp)
                .testTag("fab_add_trip")
        ) {
            Icon(Icons.Default.Add, contentDescription = "Registrar corrida")
        }
    }
}

@Composable
fun TripItemRow(
    trip: Trip,
    onDelete: () -> Unit
) {
    var showDeleteConfirm by remember { mutableStateOf(false) }

    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            title = { Text("Excluir Corrida?") },
            text = { Text("Deseja excluir a corrida de ${MainViewModel.formatCurrency(trip.netAmount)}?") },
            confirmButton = {
                TextButton(onClick = { onDelete(); showDeleteConfirm = false }) {
                    Text("Excluir", color = ExpenseRed)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirm = false }) {
                    Text("Cancelar", color = TextWhite)
                }
            },
            containerColor = CardDark
        )
    }

    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = CardDark),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { showDeleteConfirm = true }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                PlatformBadge(platform = trip.platform)

                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text(
                        text = MainViewModel.formatCurrency(trip.netAmount),
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextWhite
                    )
                    Text(
                        text = "${trip.distanceKm} km",
                        fontSize = 12.sp,
                        color = TextSecondary
                    )
                }
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "${trip.durationMinutes} min",
                    fontSize = 13.sp,
                    color = TextSecondary
                )

                Text(
                    text = trip.timeStr,
                    fontSize = 13.sp,
                    color = TextSecondary,
                    fontWeight = FontWeight.Medium
                )

                Icon(
                    imageVector = Icons.Default.ChevronRight,
                    contentDescription = null,
                    tint = TextMuted,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}
