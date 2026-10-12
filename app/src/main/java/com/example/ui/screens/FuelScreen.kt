package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.FuelLog
import com.example.model.VehicleProfile
import com.example.ui.components.SubFilterTabs
import com.example.ui.theme.*
import com.example.ui.viewmodel.FuelTabFilter
import com.example.ui.viewmodel.MainViewModel

@Composable
fun FuelScreen(
    fuelLogs: List<FuelLog>,
    vehicleProfile: VehicleProfile,
    selectedFilter: FuelTabFilter,
    onFilterSelected: (FuelTabFilter) -> Unit,
    onAddFuelClick: () -> Unit,
    onDeleteFuelLog: (FuelLog) -> Unit,
    modifier: Modifier = Modifier
) {
    val tabs = listOf("Registros", "Consumo", "Estatísticas")
    val selectedIndex = when (selectedFilter) {
        FuelTabFilter.REGISTROS -> 0
        FuelTabFilter.CONSUMO -> 1
        FuelTabFilter.ESTATISTICAS -> 2
    }

    // Real dynamic totals (starts at zero if empty)
    val totalSpent = fuelLogs.sumOf { it.totalAmount }
    val totalLiters = fuelLogs.sumOf { it.liters }
    val avgConsumption = if (fuelLogs.isNotEmpty()) {
        fuelLogs.map { it.averageKmPerLiter }.average()
    } else {
        if (vehicleProfile.averageConsumption > 0) vehicleProfile.averageConsumption else 0.0
    }
    val avgPrice = if (totalLiters > 0) {
        totalSpent / totalLiters
    } else {
        if (vehicleProfile.fuelOrEnergyPrice > 0) vehicleProfile.fuelOrEnergyPrice else 0.0
    }

    val unitLabel = if (vehicleProfile.isElectric) "kWh" else "L"
    val consumptionUnit = if (vehicleProfile.isElectric) "km/kWh" else "km/L"

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
                    text = if (vehicleProfile.isElectric) "Recarga & Energia" else "Combustível",
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
                            0 -> FuelTabFilter.REGISTROS
                            1 -> FuelTabFilter.CONSUMO
                            else -> FuelTabFilter.ESTATISTICAS
                        }
                        onFilterSelected(f)
                    }
                )
            }

            // Hero Card: Total gasto (mês)
            item {
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = CardDark),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(18.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        Column {
                            Text(
                                text = "Total gasto",
                                fontSize = 13.sp,
                                color = TextSecondary,
                                fontWeight = FontWeight.Medium
                            )
                            Text(
                                text = MainViewModel.formatCurrency(totalSpent),
                                fontSize = 30.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = TextWhite
                            )
                        }

                        // 3 Metric Pills
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            // Litros / kWh
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(SurfaceDark)
                                    .padding(vertical = 10.dp, horizontal = 8.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Icon(
                                        imageVector = if (vehicleProfile.isElectric) Icons.Default.ElectricBolt else Icons.Default.LocalGasStation,
                                        contentDescription = null,
                                        tint = FuelOrange,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Column {
                                        Text(if (vehicleProfile.isElectric) "Energia" else "Litros", fontSize = 10.sp, color = TextSecondary)
                                        Text(
                                            text = "${String.format("%.1f", totalLiters).replace(".", ",")} $unitLabel",
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = TextWhite
                                        )
                                    }
                                }
                            }

                            // Média
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(SurfaceDark)
                                    .padding(vertical = 10.dp, horizontal = 8.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Speed,
                                        contentDescription = null,
                                        tint = MetricBlue,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Column {
                                        Text("Média", fontSize = 10.sp, color = TextSecondary)
                                        Text(
                                            text = "${String.format("%.1f", avgConsumption).replace(".", ",")} $consumptionUnit",
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = TextWhite
                                        )
                                    }
                                }
                            }

                            // Preço médio
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(SurfaceDark)
                                    .padding(vertical = 10.dp, horizontal = 8.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.AttachMoney,
                                        contentDescription = null,
                                        tint = EmeraldGreen,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Column {
                                        Text("Preço médio", fontSize = 10.sp, color = TextSecondary)
                                        Text(
                                            text = String.format("R$ %.2f", avgPrice).replace(".", ","),
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = TextWhite
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Section Header
            item {
                Text(
                    text = "Últimos registros",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextWhite
                )
            }

            // Empty state when zero records
            if (fuelLogs.isEmpty()) {
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
                                imageVector = if (vehicleProfile.isElectric) Icons.Default.ElectricBolt else Icons.Default.LocalGasStation,
                                contentDescription = null,
                                tint = TextSecondary,
                                modifier = Modifier.size(36.dp)
                            )
                            Text(
                                text = "Nenhum abastecimento registrado",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = TextWhite
                            )
                            Text(
                                text = "Clique no botão abaixo para cadastrar seu primeiro abastecimento ou recarga e acompanhar o custo real por km.",
                                fontSize = 12.sp,
                                color = TextSecondary,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            } else {
                // Fuel Logs List
                items(fuelLogs, key = { it.id }) { log ->
                    FuelLogRow(fuelLog = log, unit = unitLabel, consumptionUnit = consumptionUnit)
                }
            }
        }

        // Action Button: Registrar abastecimento (Bottom pinned button)
        Button(
            onClick = onAddFuelClick,
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .padding(16.dp)
                .height(52.dp)
                .testTag("btn_register_fuel"),
            colors = ButtonDefaults.buttonColors(containerColor = EmeraldGreen),
            shape = RoundedCornerShape(14.dp)
        ) {
            Icon(
                imageVector = if (vehicleProfile.isElectric) Icons.Default.ElectricBolt else Icons.Default.LocalGasStation,
                contentDescription = null,
                tint = Color.White
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = if (vehicleProfile.isElectric) "Registrar recarga elétrica" else "Registrar abastecimento",
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        }
    }
}

@Composable
fun FuelLogRow(
    fuelLog: FuelLog,
    unit: String = "L",
    consumptionUnit: String = "km/L"
) {
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
                    .background(EmeraldGreen.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (fuelLog.fuelType == "Elétrico") Icons.Default.ElectricBolt else Icons.Default.LocalGasStation,
                    contentDescription = null,
                    tint = EmeraldGreen,
                    modifier = Modifier.size(20.dp)
                )
            }

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = fuelLog.dateStr,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextWhite
                    )
                    Text(
                        text = MainViewModel.formatCurrency(fuelLog.totalAmount),
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextWhite
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "${String.format("%.1f", fuelLog.liters).replace(".", ",")} $unit  •  ${fuelLog.stationName}",
                        fontSize = 12.sp,
                        color = TextSecondary
                    )

                    // Fuel type badge
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(EmeraldGreen.copy(alpha = 0.2f))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = fuelLog.fuelType,
                            color = EmeraldGreen,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        }
    }
}
