package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.CheckCircle
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
import com.example.model.MaintenanceItem
import com.example.ui.components.SubFilterTabs
import com.example.ui.theme.*
import com.example.ui.viewmodel.MainViewModel
import com.example.ui.viewmodel.MaintenanceTabFilter

@Composable
fun MaintenanceScreen(
    items: List<MaintenanceItem>,
    selectedFilter: MaintenanceTabFilter,
    onFilterSelected: (MaintenanceTabFilter) -> Unit,
    onAddMaintenanceClick: () -> Unit,
    onToggleCompleted: (MaintenanceItem) -> Unit,
    modifier: Modifier = Modifier
) {
    val tabs = listOf("Serviços", "Lembretes", "Histórico")
    val selectedIndex = when (selectedFilter) {
        MaintenanceTabFilter.SERVICOS -> 0
        MaintenanceTabFilter.LEMBRETES -> 1
        MaintenanceTabFilter.HISTORICO -> 2
    }

    val upcoming = items.filter { !it.isCompleted }
    val history = items.filter { it.isCompleted }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(BackgroundDark)
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            contentPadding = PaddingValues(top = 12.dp, bottom = 90.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header
            item {
                Text(
                    text = "Manutenção",
                    fontSize = 24.sp,
                    color = TextWhite,
                    fontWeight = FontWeight.Bold
                )
            }

            // Sub-filter tabs
            item {
                SubFilterTabs(
                    tabs = tabs,
                    selectedIndex = selectedIndex,
                    onTabSelected = { idx ->
                        val f = when (idx) {
                            0 -> MaintenanceTabFilter.SERVICOS
                            1 -> MaintenanceTabFilter.LEMBRETES
                            else -> MaintenanceTabFilter.HISTORICO
                        }
                        onFilterSelected(f)
                    }
                )
            }

            // Próximas manutenções section
            if (selectedFilter != MaintenanceTabFilter.HISTORICO && upcoming.isNotEmpty()) {
                item {
                    Text(
                        text = "Próximas manutenções",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextWhite
                    )
                }

                items(upcoming, key = { it.id }) { item ->
                    Card(
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = CardDark),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .background(EmeraldGreen.copy(alpha = 0.2f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Build,
                                    contentDescription = null,
                                    tint = EmeraldGreen,
                                    modifier = Modifier.size(20.dp)
                                )
                            }

                            Column(
                                modifier = Modifier.weight(1f),
                                verticalArrangement = Arrangement.spacedBy(2.dp)
                            ) {
                                Text(
                                    text = item.title,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TextWhite
                                )
                                Text(
                                    text = "Em ${String.format("%,d", item.alertKmRemaining)} km (ou ${item.alertDaysRemaining} dias)",
                                    fontSize = 12.sp,
                                    color = TextSecondary
                                )
                            }

                            IconButton(onClick = { onToggleCompleted(item) }) {
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = "Concluir",
                                    tint = TextSecondary
                                )
                            }
                        }
                    }
                }
            }

            // Últimos serviços section
            if (selectedFilter != MaintenanceTabFilter.LEMBRETES) {
                item {
                    Text(
                        text = "Últimos serviços",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextWhite
                    )
                }

                items(history, key = { it.id }) { item ->
                    Card(
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = CardDark),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .background(MetricBlue.copy(alpha = 0.2f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Build,
                                    contentDescription = null,
                                    tint = MetricBlue,
                                    modifier = Modifier.size(20.dp)
                                )
                            }

                            Column(
                                modifier = Modifier.weight(1f),
                                verticalArrangement = Arrangement.spacedBy(2.dp)
                            ) {
                                Text(
                                    text = item.title,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TextWhite
                                )
                                Text(
                                    text = item.dateStr,
                                    fontSize = 12.sp,
                                    color = TextSecondary
                                )
                            }

                            Text(
                                text = MainViewModel.formatCurrency(item.cost),
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextWhite
                            )
                        }
                    }
                }
            }
        }

        // Action Button: + Registrar serviço (Pinned at bottom)
        Button(
            onClick = onAddMaintenanceClick,
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .padding(16.dp)
                .height(52.dp)
                .testTag("btn_register_service"),
            colors = ButtonDefaults.buttonColors(containerColor = EmeraldGreen),
            shape = RoundedCornerShape(14.dp)
        ) {
            Icon(Icons.Default.Add, contentDescription = null, tint = Color.White)
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Registrar serviço",
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        }
    }
}
