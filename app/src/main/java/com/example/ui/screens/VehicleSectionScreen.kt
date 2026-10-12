package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.model.*
import com.example.ui.theme.*
import com.example.ui.viewmodel.MainViewModel

@Composable
fun VehicleSectionScreen(
    vehicleProfile: VehicleProfile,
    vehicleCostItems: List<VehicleCostItem>,
    fuelChargeRecords: List<FuelChargeRecord>,
    viabilityResult: ViabilityCalculationResult,
    onUpdateProfile: (VehicleProfile) -> Unit,
    onAddCostItem: (VehicleCostItem) -> Unit,
    onDeleteCostItem: (VehicleCostItem) -> Unit,
    onAddFuelChargeRecord: (FuelChargeRecord) -> Unit,
    onDeleteFuelChargeRecord: (FuelChargeRecord) -> Unit,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedTab by remember { mutableStateOf(0) } // 0 = Resumo & Custos, 1 = Lançamentos de Energia, 2 = Viabilidade Mínimos
    var showAddCostDialog by remember { mutableStateOf(false) }
    var showAddFuelChargeDialog by remember { mutableStateOf(false) }
    var showEditPowertrainDialog by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(BackgroundDark)
    ) {
        // Top Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                IconButton(
                    onClick = onNavigateBack,
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(CardDark)
                ) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "Voltar",
                        tint = TextWhite,
                        modifier = Modifier.size(20.dp)
                    )
                }

                Column {
                    Text(
                        text = "Meu Veículo",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextWhite
                    )
                    Text(
                        text = if (vehicleProfile.modelName.isNotBlank()) "${vehicleProfile.modelName} • ${vehicleProfile.vehiclePowertrain.displayName}" else "Cadastre os custos e propulsão",
                        fontSize = 12.sp,
                        color = EmeraldGreen
                    )
                }
            }

            IconButton(
                onClick = { showEditPowertrainDialog = true },
                modifier = Modifier
                    .size(38.dp)
                    .clip(CircleShape)
                    .background(CardHighlight)
            ) {
                Icon(
                    imageVector = Icons.Default.Settings,
                    contentDescription = "Editar Tipo de Veículo",
                    tint = EmeraldGreen,
                    modifier = Modifier.size(20.dp)
                )
            }
        }

        // Segmented Tabs: Custos & Visão Geral | Abastecimentos/Recargas | Viabilidade R$/km
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 4.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(CardDark)
                .padding(4.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            VehicleTabPill(
                label = "Custos Fixos",
                selected = selectedTab == 0,
                onClick = { selectedTab = 0 },
                modifier = Modifier.weight(1f)
            )
            VehicleTabPill(
                label = when (vehicleProfile.vehiclePowertrain) {
                    VehiclePowertrain.ELETRICO -> "Recargas"
                    VehiclePowertrain.HIBRIDO, VehiclePowertrain.HIBRIDO_PLUGIN -> "Abast. & Recargas"
                    else -> "Abastecimentos"
                },
                selected = selectedTab == 1,
                onClick = { selectedTab = 1 },
                modifier = Modifier.weight(1f)
            )
            VehicleTabPill(
                label = "Tarifa Mínima",
                selected = selectedTab == 2,
                onClick = { selectedTab = 2 },
                modifier = Modifier.weight(1f)
            )
        }

        when (selectedTab) {
            0 -> VehicleCostsTab(
                vehicleProfile = vehicleProfile,
                costItems = vehicleCostItems,
                viabilityResult = viabilityResult,
                onAddCostClick = { showAddCostDialog = true },
                onDeleteCostItem = onDeleteCostItem,
                onEditPowertrainClick = { showEditPowertrainDialog = true }
            )
            1 -> FuelAndChargeTab(
                vehicleProfile = vehicleProfile,
                records = fuelChargeRecords,
                onAddRecordClick = { showAddFuelChargeDialog = true },
                onDeleteRecord = onDeleteFuelChargeRecord
            )
            2 -> ViabilityTab(
                vehicleProfile = vehicleProfile,
                viabilityResult = viabilityResult,
                onUpdateProfile = onUpdateProfile
            )
        }
    }

    if (showAddCostDialog) {
        AddVehicleCostDialog(
            onDismiss = { showAddCostDialog = false },
            onConfirm = { costItem ->
                onAddCostItem(costItem)
                showAddCostDialog = false
            }
        )
    }

    if (showAddFuelChargeDialog) {
        AddFuelOrChargeRecordDialog(
            powertrain = vehicleProfile.vehiclePowertrain,
            homeKwhPrice = vehicleProfile.homeKwhPrice,
            publicKwhPrice = vehicleProfile.publicKwhPrice,
            fuelPrice = vehicleProfile.fuelOrEnergyPrice,
            onDismiss = { showAddFuelChargeDialog = false },
            onConfirm = { record ->
                onAddFuelChargeRecord(record)
                showAddFuelChargeDialog = false
            }
        )
    }

    if (showEditPowertrainDialog) {
        EditVehiclePowertrainDialog(
            currentProfile = vehicleProfile,
            onDismiss = { showEditPowertrainDialog = false },
            onSave = { updated ->
                onUpdateProfile(updated)
                showEditPowertrainDialog = false
            }
        )
    }
}

@Composable
fun VehicleTabPill(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(if (selected) EmeraldGreen else Color.Transparent)
            .clickable { onClick() }
            .padding(vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            fontSize = 11.sp,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
            color = if (selected) Color.White else TextSecondary
        )
    }
}

@Composable
fun VehicleCostsTab(
    vehicleProfile: VehicleProfile,
    costItems: List<VehicleCostItem>,
    viabilityResult: ViabilityCalculationResult,
    onAddCostClick: () -> Unit,
    onDeleteCostItem: (VehicleCostItem) -> Unit,
    onEditPowertrainClick: () -> Unit
) {
    val totalMonthlyFixed = costItems.filter { it.category.isFixedCost }.sumOf { it.monthlyEquivalentCost }
    val totalMonthlyVariable = costItems.filter { !it.category.isFixedCost }.sumOf { it.monthlyEquivalentCost }
    val totalMonthlyVehicle = totalMonthlyFixed + totalMonthlyVariable

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 10.dp, bottom = 80.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Powertrain Summary Card
        item {
            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = CardDark),
                modifier = Modifier.fillMaxWidth()
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
                            .size(50.dp)
                            .clip(RoundedCornerShape(12.dp))
                        .background(EmeraldGreen.copy(alpha = 0.18f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = when (vehicleProfile.vehiclePowertrain) {
                                VehiclePowertrain.ELETRICO -> Icons.Default.ElectricCar
                                VehiclePowertrain.HIBRIDO, VehiclePowertrain.HIBRIDO_PLUGIN -> Icons.Default.ElectricBolt
                                else -> Icons.Default.DirectionsCar
                            },
                            contentDescription = null,
                            tint = EmeraldGreen,
                            modifier = Modifier.size(28.dp)
                        )
                    }

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = if (vehicleProfile.modelName.isNotBlank()) "${vehicleProfile.modelName} (${vehicleProfile.manufacturingYear})" else "Veículo não configurado",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextWhite
                        )
                        Text(
                            text = "Propulsão: ${vehicleProfile.vehiclePowertrain.displayName}",
                            fontSize = 12.sp,
                            color = EmeraldGreen,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = when (vehicleProfile.vehiclePowertrain) {
                                VehiclePowertrain.ELETRICO -> "Consumo: ${vehicleProfile.averageConsumption} km/kWh"
                                VehiclePowertrain.HIBRIDO_PLUGIN -> "Consumo: ${vehicleProfile.averageConsumption} km/L • ${vehicleProfile.electricConsumption} km/kWh"
                                else -> "Consumo: ${vehicleProfile.averageConsumption} km/L (${vehicleProfile.fuelType})"
                            },
                            fontSize = 11.sp,
                            color = TextSecondary
                        )
                    }

                    OutlinedButton(
                        onClick = onEditPowertrainClick,
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = EmeraldGreen),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text("Alterar", fontSize = 11.sp)
                    }
                }
            }
        }

        // Summary Metric Badges: Custo Fixo Mensal | Custo Variável | Custo Total | Custo Médio por Km
        item {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    MetricBox(
                        title = "Custo Fixo Mensal",
                        value = MainViewModel.formatCurrency(totalMonthlyFixed),
                        subtitle = "Financ./Seguro/IPVA",
                        color = MetricBlue,
                        modifier = Modifier.weight(1f)
                    )
                    MetricBox(
                        title = "Custo Variável Mensal",
                        value = MainViewModel.formatCurrency(totalMonthlyVariable),
                        subtitle = "Manutenção/Pneus/Óleo",
                        color = FuelOrange,
                        modifier = Modifier.weight(1f)
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    MetricBox(
                        title = "Custo Total do Carro",
                        value = MainViewModel.formatCurrency(totalMonthlyVehicle),
                        subtitle = "Fixo + Variáveis/mês",
                        color = ExpenseRedSoft,
                        modifier = Modifier.weight(1f)
                    )
                    MetricBox(
                        title = "Custo Médio por Km",
                        value = if (viabilityResult.hasSufficientData) "R$ ${String.format("%.2f", viabilityResult.estimatedCostPerKm).replace(".", ",")}" else "Estimando...",
                        subtitle = if (viabilityResult.hasSufficientData) "Combustível + Desgaste" else "Requer dados",
                        color = EmeraldGreen,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        // Header for Cost Items List
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Custos e Despesas do Veículo",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextWhite
                )

                Button(
                    onClick = onAddCostClick,
                    colors = ButtonDefaults.buttonColors(containerColor = EmeraldGreen),
                    shape = RoundedCornerShape(10.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Cadastrar Custo", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        if (costItems.isEmpty()) {
            item {
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
                            imageVector = Icons.Outlined.DirectionsCar,
                            contentDescription = null,
                            tint = TextSecondary,
                            modifier = Modifier.size(36.dp)
                        )
                        Text(
                            text = "Nenhum custo veicular cadastrado",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextWhite
                        )
                        Text(
                            text = "Cadastre financiamento/aluguel, seguro, IPVA, trocas de óleo, pneus e revisões para calcular o custo real por km.",
                            fontSize = 12.sp,
                            color = TextSecondary,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                    }
                }
            }
        } else {
            items(costItems, key = { it.id }) { item ->
                VehicleCostCard(item = item, onDelete = { onDeleteCostItem(item) })
            }
        }
    }
}

@Composable
fun MetricBox(
    title: String,
    value: String,
    subtitle: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = CardDark),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Text(title, fontSize = 11.sp, color = TextSecondary, fontWeight = FontWeight.Medium)
            Text(value, fontSize = 16.sp, fontWeight = FontWeight.ExtraBold, color = color)
            Text(subtitle, fontSize = 10.sp, color = TextMuted)
        }
    }
}

@Composable
fun VehicleCostCard(
    item: VehicleCostItem,
    onDelete: () -> Unit
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
                    .size(42.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(if (item.category.isFixedCost) MetricBlue.copy(alpha = 0.2f) else FuelOrange.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = when (item.category) {
                        VehicleExpenseCategory.FINANCIAMENTO_ALUGUEL -> Icons.Outlined.AccountBalance
                        VehicleExpenseCategory.SEGURO_PROTECAO -> Icons.Outlined.Shield
                        VehicleExpenseCategory.IPVA_LICENCIAMENTO -> Icons.Outlined.Receipt
                        VehicleExpenseCategory.OLEO_FILTROS -> Icons.Outlined.Opacity
                        VehicleExpenseCategory.PNEUS_FREIOS -> Icons.Outlined.TireRepair
                        VehicleExpenseCategory.REVISAO_MANUTENCAO -> Icons.Outlined.Build
                        VehicleExpenseCategory.LAVAGEM_ESTACIONAMENTO -> Icons.Outlined.LocalCarWash
                        VehicleExpenseCategory.OUTRA -> Icons.Outlined.Category
                    },
                    contentDescription = null,
                    tint = if (item.category.isFixedCost) MetricBlue else FuelOrange,
                    modifier = Modifier.size(22.dp)
                )
            }

            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    text = item.title.ifBlank { item.category.displayName },
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextWhite
                )
                Text(
                    text = "${item.category.displayName} • ${item.periodicity.displayName}",
                    fontSize = 11.sp,
                    color = TextSecondary
                )

                if (item.category == VehicleExpenseCategory.FINANCIAMENTO_ALUGUEL) {
                    if (item.isPaidOff) {
                        Text("Veículo Quitado ✅", fontSize = 11.sp, color = EmeraldGreen, fontWeight = FontWeight.Bold)
                    } else if (item.totalInstallments > 0) {
                        Text(
                            text = "Parcela ${item.totalInstallments - item.remainingInstallments}/${item.totalInstallments} (faltam ${item.remainingInstallments})",
                            fontSize = 11.sp,
                            color = MetricBlue
                        )
                    }
                }

                if (item.periodicity == VehicleExpensePeriodicity.ANUAL) {
                    Text(
                        text = "Valor pago: ${MainViewModel.formatCurrency(item.amountPaid)} (Média mensal: ${MainViewModel.formatCurrency(item.monthlyEquivalentCost)})",
                        fontSize = 10.sp,
                        color = LightEmeraldGreen
                    )
                }
            }

            Column(horizontalAlignment = Alignment.End, verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = MainViewModel.formatCurrency(item.monthlyEquivalentCost) + "/mês",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextWhite
                )

                IconButton(
                    onClick = onDelete,
                    modifier = Modifier.size(24.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.DeleteOutline,
                        contentDescription = "Excluir Custo",
                        tint = ExpenseRedSoft,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun FuelAndChargeTab(
    vehicleProfile: VehicleProfile,
    records: List<FuelChargeRecord>,
    onAddRecordClick: () -> Unit,
    onDeleteRecord: (FuelChargeRecord) -> Unit
) {
    val isElectricOrHybrid = vehicleProfile.vehiclePowertrain != VehiclePowertrain.COMBUSTAO

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 10.dp, bottom = 80.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = if (isElectricOrHybrid) "Histórico de Abastecimento / Recarga" else "Histórico de Abastecimento",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextWhite
                    )
                    Text(
                        text = "Acompanhe litros, kWh consumidos e preços",
                        fontSize = 11.sp,
                        color = TextSecondary
                    )
                }

                Button(
                    onClick = onAddRecordClick,
                    colors = ButtonDefaults.buttonColors(containerColor = EmeraldGreen),
                    shape = RoundedCornerShape(10.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(if (vehicleProfile.vehiclePowertrain == VehiclePowertrain.ELETRICO) "Nova Recarga" else "Novo Registro", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        if (records.isEmpty()) {
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
                            imageVector = if (isElectricOrHybrid) Icons.Default.EvStation else Icons.Default.LocalGasStation,
                            contentDescription = null,
                            tint = TextSecondary,
                            modifier = Modifier.size(36.dp)
                        )
                        Text(
                            text = "Nenhum abastecimento ou recarga registrado",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextWhite
                        )
                        Text(
                            text = "Toque no botão acima para registrar seus abastecimentos (litros e R$/L) ou recargas elétricas (kWh em casa ou pública).",
                            fontSize = 12.sp,
                            color = TextSecondary,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                    }
                }
            }
        } else {
            items(records, key = { it.id }) { record ->
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
                                .size(42.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(if (record.isElectricCharge) EmeraldGreen.copy(alpha = 0.2f) else FuelOrange.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = if (record.isElectricCharge) Icons.Default.EvStation else Icons.Default.LocalGasStation,
                                contentDescription = null,
                                tint = if (record.isElectricCharge) EmeraldGreen else FuelOrange,
                                modifier = Modifier.size(22.dp)
                            )
                        }

                        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                            Text(
                                text = if (record.isElectricCharge) "Recarga Elétrica" else "Abastecimento",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextWhite
                            )
                            Text(
                                text = if (record.isElectricCharge) {
                                    "${String.format("%.1f", record.quantity)} kWh • R$ ${String.format("%.2f", record.unitPrice)}/kWh • ${record.chargeLocation.displayName}"
                                } else {
                                    "${String.format("%.2f", record.quantity)} L • R$ ${String.format("%.2f", record.unitPrice)}/L"
                                },
                                fontSize = 11.sp,
                                color = TextSecondary
                            )
                            Text(
                                text = "${record.dateStr} ${if (record.notes.isNotBlank()) "• ${record.notes}" else ""}",
                                fontSize = 10.sp,
                                color = TextMuted
                            )
                        }

                        Column(horizontalAlignment = Alignment.End, verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text(
                                text = MainViewModel.formatCurrency(record.totalCost),
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextWhite
                            )
                            IconButton(onClick = { onDeleteRecord(record) }, modifier = Modifier.size(24.dp)) {
                                Icon(Icons.Default.DeleteOutline, contentDescription = null, tint = ExpenseRedSoft, modifier = Modifier.size(16.dp))
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ViabilityTab(
    vehicleProfile: VehicleProfile,
    viabilityResult: ViabilityCalculationResult,
    onUpdateProfile: (VehicleProfile) -> Unit
) {
    var isNetProfitGoal by remember { mutableStateOf(vehicleProfile.isGoalNetProfit) }
    var avgDailyKmText by remember { mutableStateOf(vehicleProfile.avgDailyKm.toInt().toString()) }
    var avgDailyHoursText by remember { mutableStateOf(vehicleProfile.avgDailyHours.toInt().toString()) }
    var deadheadText by remember { mutableStateOf(vehicleProfile.deadheadPercentage.toInt().toString()) }
    var safetyMarginText by remember { mutableStateOf(vehicleProfile.safetyMarginPercentage.toInt().toString()) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 10.dp, bottom = 90.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Hero Highlight: Tarifa Mínima por Km e por Hora
        item {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = CardHighlight),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "Valores Mínimos Recomendados",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = EmeraldGreen
                    )

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
                                Text("Mínimo por Km", fontSize = 11.sp, color = TextSecondary)
                                Text(
                                    text = if (viabilityResult.hasSufficientData) "R$ ${String.format("%.2f", viabilityResult.recommendedMinRatePerKm).replace(".", ",")}" else "Aguardando dados",
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = EmeraldGreen
                                )
                                Text("Km remunerado", fontSize = 10.sp, color = TextMuted)
                            }
                        }

                        Card(
                            shape = RoundedCornerShape(14.dp),
                            colors = CardDefaults.cardColors(containerColor = CardDark),
                            modifier = Modifier.weight(1f)
                        ) {
                            Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Text("Mínimo por Hora", fontSize = 11.sp, color = TextSecondary)
                                Text(
                                    text = if (viabilityResult.hasSufficientData) "R$ ${String.format("%.2f", viabilityResult.recommendedMinRatePerHour).replace(".", ",")}" else "Aguardando dados",
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = MetricBlue
                                )
                                Text("Hora trabalhada", fontSize = 10.sp, color = TextMuted)
                            }
                        }
                    }

                    if (!viabilityResult.hasSufficientData) {
                        Text(
                            text = viabilityResult.missingDataMessage,
                            fontSize = 12.sp,
                            color = FuelOrangeLight
                        )
                    }
                }
            }
        }

        // Detailed Calculations Breakdown Card
        item {
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
                    Text(
                        text = "Detalhamento e Metas Calculadas",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextWhite
                    )

                    CalculationLine("Meta de faturamento diário:", MainViewModel.formatCurrency(viabilityResult.dailyRevenueTarget), TextWhite)
                    CalculationLine("Meta de lucro líquido diário:", MainViewModel.formatCurrency(viabilityResult.dailyNetProfitTarget), ProfitGreen)
                    CalculationLine("Custo diário estimado do veículo:", MainViewModel.formatCurrency(viabilityResult.estimatedDailyVehicleCost), ExpenseRedSoft)
                    CalculationLine("Custo estimado por quilômetro:", "R$ ${String.format("%.2f", viabilityResult.estimatedCostPerKm).replace(".", ",")}/km", FuelOrange)
                    CalculationLine("Faturamento mensal necessário:", MainViewModel.formatCurrency(viabilityResult.monthlyNecessaryRevenue), TextWhite)
                    CalculationLine("Lucro líquido mensal estimado:", MainViewModel.formatCurrency(viabilityResult.estimatedNetProfit), ProfitGreen)
                    CalculationLine("Dias de trabalho planejados:", "${viabilityResult.plannedWorkDaysCount} dias", MetricBlue)
                }
            }
        }

        // Didactic Simple Explanation
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = SurfaceDark),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Icon(Icons.Outlined.Lightbulb, contentDescription = null, tint = EmeraldGreen, modifier = Modifier.size(20.dp))
                        Text("Como calculamos estes valores?", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = TextWhite)
                    }

                    Text(
                        text = viabilityResult.explanationText,
                        fontSize = 12.sp,
                        color = TextSecondary,
                        lineHeight = 18.sp
                    )
                }
            }
        }

        // Adjustable Parameters Form
        item {
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
                    Text(
                        text = "Ajustar Parâmetros de Trabalho e Margem",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = EmeraldGreen
                    )

                    // Switch Faturamento Bruto vs Lucro Líquido
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = if (isNetProfitGoal) "Meta representa Lucro Líquido Desejado" else "Meta representa Faturamento Bruto",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = TextWhite
                            )
                            Text(
                                text = if (isNetProfitGoal) "O app somará todos os custos do carro por cima para atingir o lucro limpo." else "Padrão original de faturamento bruto total.",
                                fontSize = 11.sp,
                                color = TextSecondary
                            )
                        }
                        Switch(
                            checked = isNetProfitGoal,
                            onCheckedChange = {
                                isNetProfitGoal = it
                                onUpdateProfile(vehicleProfile.copy(isGoalNetProfit = it))
                            },
                            colors = SwitchDefaults.colors(checkedThumbColor = EmeraldGreen)
                        )
                    }

                    HorizontalDivider(color = BorderDark, thickness = 0.5.dp)

                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        OutlinedTextField(
                            value = avgDailyKmText,
                            onValueChange = { avgDailyKmText = it },
                            label = { Text("Km médios/dia") },
                            placeholder = { Text("150") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )

                        OutlinedTextField(
                            value = avgDailyHoursText,
                            onValueChange = { avgDailyHoursText = it },
                            label = { Text("Horas/dia") },
                            placeholder = { Text("8") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        OutlinedTextField(
                            value = deadheadText,
                            onValueChange = { deadheadText = it },
                            label = { Text("% Sem Passageiro") },
                            placeholder = { Text("25") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )

                        OutlinedTextField(
                            value = safetyMarginText,
                            onValueChange = { safetyMarginText = it },
                            label = { Text("% Margem Segur.") },
                            placeholder = { Text("10") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Button(
                        onClick = {
                            val km = avgDailyKmText.toDoubleOrNull() ?: 150.0
                            val hrs = avgDailyHoursText.toDoubleOrNull() ?: 8.0
                            val deadhead = deadheadText.toDoubleOrNull() ?: 25.0
                            val safety = safetyMarginText.toDoubleOrNull() ?: 10.0

                            onUpdateProfile(
                                vehicleProfile.copy(
                                    isGoalNetProfit = isNetProfitGoal,
                                    avgDailyKm = km,
                                    avgDailyHours = hrs,
                                    deadheadPercentage = deadhead,
                                    safetyMarginPercentage = safety
                                )
                            )
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = EmeraldGreen),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("Atualizar Cálculos de Viabilidade", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
fun CalculationLine(label: String, value: String, valueColor: Color) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label, fontSize = 12.sp, color = TextSecondary)
        Text(value, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = valueColor)
    }
}

// Dialog to Add Vehicle Cost
@Composable
fun AddVehicleCostDialog(
    onDismiss: () -> Unit,
    onConfirm: (VehicleCostItem) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf(VehicleExpenseCategory.FINANCIAMENTO_ALUGUEL) }
    var periodicity by remember { mutableStateOf(VehicleExpensePeriodicity.MENSAL) }
    var amountPaidText by remember { mutableStateOf("") }
    var isPaidOff by remember { mutableStateOf(false) }
    var installmentAmountText by remember { mutableStateOf("") }
    var totalInstallmentsText by remember { mutableStateOf("") }
    var remainingInstallmentsText by remember { mutableStateOf("") }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = CardDark,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Cadastrar Custo do Veículo", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = TextWhite)
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Fechar", tint = TextSecondary)
                    }
                }

                Text("Categoria", fontSize = 12.sp, color = TextSecondary)
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    VehicleExpenseCategory.entries.chunked(2).forEach { row ->
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.fillMaxWidth()) {
                            row.forEach { cat ->
                                val isSelected = selectedCategory == cat
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(if (isSelected) EmeraldGreen else SurfaceDark)
                                        .clickable { selectedCategory = cat }
                                        .padding(vertical = 8.dp, horizontal = 6.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = cat.displayName,
                                        fontSize = 11.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                        color = if (isSelected) Color.White else TextSecondary,
                                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                    )
                                }
                            }
                        }
                    }
                }

                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Nome / Descrição (opcional)") },
                    placeholder = { Text("Ex: Parcela Carro, Porto Seguro") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Text("Periodicidade", fontSize = 12.sp, color = TextSecondary)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                    VehicleExpensePeriodicity.values().forEach { per ->
                        val isSelected = periodicity == per
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isSelected) MetricBlue else SurfaceDark)
                                .clickable { periodicity = per }
                                .padding(vertical = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = per.displayName,
                                fontSize = 11.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                color = if (isSelected) Color.White else TextSecondary
                            )
                        }
                    }
                }

                // If Financiamento: show quitado, parcelas
                if (selectedCategory == VehicleExpenseCategory.FINANCIAMENTO_ALUGUEL) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Veículo quitado?", fontSize = 13.sp, color = TextWhite)
                        Switch(checked = isPaidOff, onCheckedChange = { isPaidOff = it })
                    }

                    if (!isPaidOff) {
                        OutlinedTextField(
                            value = installmentAmountText,
                            onValueChange = { installmentAmountText = it },
                            label = { Text("Valor da Parcela Mensal (R$)") },
                            placeholder = { Text("Ex: 1150.00") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )

                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedTextField(
                                value = totalInstallmentsText,
                                onValueChange = { totalInstallmentsText = it },
                                label = { Text("Total de Parcelas") },
                                placeholder = { Text("48") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                singleLine = true,
                                modifier = Modifier.weight(1f)
                            )
                            OutlinedTextField(
                                value = remainingInstallmentsText,
                                onValueChange = { remainingInstallmentsText = it },
                                label = { Text("Quantas Faltam") },
                                placeholder = { Text("24") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                singleLine = true,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                } else {
                    OutlinedTextField(
                        value = amountPaidText,
                        onValueChange = { amountPaidText = it },
                        label = { Text(if (periodicity == VehicleExpensePeriodicity.ANUAL) "Valor Total Pago no Ano (R$)" else "Valor (R$)") },
                        placeholder = { Text("Ex: 1800.00") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                Button(
                    onClick = {
                        val amt = amountPaidText.replace(",", ".").toDoubleOrNull() ?: 0.0
                        val instAmt = installmentAmountText.replace(",", ".").toDoubleOrNull() ?: amt
                        val totalInst = totalInstallmentsText.toIntOrNull() ?: 0
                        val remInst = remainingInstallmentsText.toIntOrNull() ?: 0

                        onConfirm(
                            VehicleCostItem(
                                title = title.ifBlank { selectedCategory.displayName },
                                category = selectedCategory,
                                amountPaid = if (selectedCategory == VehicleExpenseCategory.FINANCIAMENTO_ALUGUEL && !isPaidOff) instAmt else amt,
                                periodicity = periodicity,
                                isPaidOff = isPaidOff,
                                installmentAmount = instAmt,
                                totalInstallments = totalInst,
                                remainingInstallments = remInst,
                                dateStr = MainViewModel.getTodayDateString()
                            )
                        )
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = EmeraldGreen),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Salvar Custo", color = Color.White, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

// Dialog to Add Fuel or Electric Charge Record
@Composable
fun AddFuelOrChargeRecordDialog(
    powertrain: VehiclePowertrain,
    homeKwhPrice: Double,
    publicKwhPrice: Double,
    fuelPrice: Double,
    onDismiss: () -> Unit,
    onConfirm: (FuelChargeRecord) -> Unit
) {
    val canElectric = powertrain == VehiclePowertrain.ELETRICO || powertrain == VehiclePowertrain.HIBRIDO_PLUGIN
    var isElectricCharge by remember { mutableStateOf(powertrain == VehiclePowertrain.ELETRICO) }
    var location by remember { mutableStateOf(ChargeLocation.CASA) }
    var quantityText by remember { mutableStateOf("") }
    var unitPriceText by remember {
        mutableStateOf(
            if (isElectricCharge) {
                if (location == ChargeLocation.CASA) String.format("%.2f", homeKwhPrice) else String.format("%.2f", publicKwhPrice)
            } else {
                String.format("%.2f", fuelPrice)
            }
        )
    }
    var dateText by remember { mutableStateOf(MainViewModel.getTodayDateString()) }
    var notesText by remember { mutableStateOf("") }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = CardDark,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Registrar Energia / Combustível", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = TextWhite)
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Fechar", tint = TextSecondary)
                    }
                }

                // If hybrid: choose between fuel and electric charge
                if (powertrain == VehiclePowertrain.HIBRIDO || powertrain == VehiclePowertrain.HIBRIDO_PLUGIN) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(
                            onClick = {
                                isElectricCharge = false
                                unitPriceText = String.format("%.2f", fuelPrice)
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = if (!isElectricCharge) FuelOrange else SurfaceDark),
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("Abastecimento (L)")
                        }

                        Button(
                            onClick = {
                                isElectricCharge = true
                                unitPriceText = if (location == ChargeLocation.CASA) String.format("%.2f", homeKwhPrice) else String.format("%.2f", publicKwhPrice)
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = if (isElectricCharge) EmeraldGreen else SurfaceDark),
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("Recarga (kWh)")
                        }
                    }
                }

                if (isElectricCharge) {
                    Text("Local da Recarga", fontSize = 12.sp, color = TextSecondary)
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (location == ChargeLocation.CASA) EmeraldGreen else SurfaceDark)
                                .clickable {
                                    location = ChargeLocation.CASA
                                    unitPriceText = String.format("%.2f", homeKwhPrice)
                                }
                                .padding(vertical = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("Em Casa", fontSize = 12.sp, color = Color.White, fontWeight = FontWeight.Bold)
                        }

                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (location == ChargeLocation.PUBLICA) EmeraldGreen else SurfaceDark)
                                .clickable {
                                    location = ChargeLocation.PUBLICA
                                    unitPriceText = String.format("%.2f", publicKwhPrice)
                                }
                                .padding(vertical = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("Eletroposto Público", fontSize = 12.sp, color = Color.White, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = quantityText,
                        onValueChange = { quantityText = it },
                        label = { Text(if (isElectricCharge) "kWh Consumidos" else "Litros (L)") },
                        placeholder = { Text(if (isElectricCharge) "Ex: 28.5" else "Ex: 40.0") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )

                    OutlinedTextField(
                        value = unitPriceText,
                        onValueChange = { unitPriceText = it },
                        label = { Text(if (isElectricCharge) "R$/kWh" else "R$/Litro") },
                        placeholder = { Text("0.85") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                }

                val qty = quantityText.replace(",", ".").toDoubleOrNull() ?: 0.0
                val unitPrice = unitPriceText.replace(",", ".").toDoubleOrNull() ?: 0.0
                val computedTotal = qty * unitPrice

                Text(
                    text = "Total a Pagar: ${MainViewModel.formatCurrency(computedTotal)}",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = EmeraldGreen
                )

                OutlinedTextField(
                    value = dateText,
                    onValueChange = { dateText = it },
                    label = { Text("Data (DD/MM/AAAA)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Button(
                    onClick = {
                        onConfirm(
                            FuelChargeRecord(
                                dateStr = dateText,
                                isElectricCharge = isElectricCharge,
                                quantity = qty,
                                unitPrice = unitPrice,
                                totalCost = computedTotal,
                                chargeLocation = location,
                                notes = notesText
                            )
                        )
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = EmeraldGreen),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Salvar Registro", color = Color.White, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

// Dialog to Edit Vehicle Powertrain and Energy Settings
@Composable
fun EditVehiclePowertrainDialog(
    currentProfile: VehicleProfile,
    onDismiss: () -> Unit,
    onSave: (VehicleProfile) -> Unit
) {
    var powertrain by remember { mutableStateOf(currentProfile.vehiclePowertrain) }
    var modelName by remember { mutableStateOf(currentProfile.modelName) }
    var year by remember { mutableStateOf(currentProfile.manufacturingYear) }
    var consumptionText by remember { mutableStateOf(currentProfile.averageConsumption.toString()) }
    var electricConsumptionText by remember { mutableStateOf(currentProfile.electricConsumption.toString()) }
    var fuelPriceText by remember { mutableStateOf(currentProfile.fuelOrEnergyPrice.toString()) }
    var homeKwhText by remember { mutableStateOf(currentProfile.homeKwhPrice.toString()) }
    var publicKwhText by remember { mutableStateOf(currentProfile.publicKwhPrice.toString()) }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = CardDark,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Configurar Tipo de Veículo", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = TextWhite)
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Fechar", tint = TextSecondary)
                    }
                }

                Text("Selecione a Propulsão", fontSize = 12.sp, color = TextSecondary)
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    VehiclePowertrain.entries.chunked(2).forEach { row ->
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            row.forEach { pt ->
                                val isSelected = powertrain == pt
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(if (isSelected) EmeraldGreen else SurfaceDark)
                                        .clickable {
                                            powertrain = pt
                                            if (pt == VehiclePowertrain.ELETRICO && consumptionText == "0.0") {
                                                consumptionText = "6.5"
                                            }
                                        }
                                        .padding(vertical = 10.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = pt.displayName,
                                        fontSize = 11.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                        color = if (isSelected) Color.White else TextSecondary
                                    )
                                }
                            }
                        }
                    }
                }

                OutlinedTextField(
                    value = modelName,
                    onValueChange = { modelName = it },
                    label = { Text("Modelo do Carro") },
                    placeholder = { Text("Ex: BYD Dolphin, Onix Plus, Corolla Hybrid") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                // Adapt fields according to powertrain
                when (powertrain) {
                    VehiclePowertrain.COMBUSTAO, VehiclePowertrain.HIBRIDO -> {
                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            OutlinedTextField(
                                value = consumptionText,
                                onValueChange = { consumptionText = it },
                                label = { Text("Consumo (km/L)") },
                                placeholder = { Text("12.5") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                singleLine = true,
                                modifier = Modifier.weight(1f)
                            )
                            OutlinedTextField(
                                value = fuelPriceText,
                                onValueChange = { fuelPriceText = it },
                                label = { Text("Preço Gasolina/Etanol (R$/L)") },
                                placeholder = { Text("5.89") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                singleLine = true,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                    VehiclePowertrain.ELETRICO -> {
                        OutlinedTextField(
                            value = consumptionText,
                            onValueChange = { consumptionText = it },
                            label = { Text("Consumo Elétrico (km/kWh)") },
                            placeholder = { Text("6.5") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )

                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            OutlinedTextField(
                                value = homeKwhText,
                                onValueChange = { homeKwhText = it },
                                label = { Text("Recarga Casa (R$/kWh)") },
                                placeholder = { Text("0.85") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                singleLine = true,
                                modifier = Modifier.weight(1f)
                            )
                            OutlinedTextField(
                                value = publicKwhText,
                                onValueChange = { publicKwhText = it },
                                label = { Text("Recarga Pública (R$/kWh)") },
                                placeholder = { Text("2.10") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                singleLine = true,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                    VehiclePowertrain.HIBRIDO_PLUGIN -> {
                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            OutlinedTextField(
                                value = consumptionText,
                                onValueChange = { consumptionText = it },
                                label = { Text("Consumo Gasolina (km/L)") },
                                placeholder = { Text("18.0") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                singleLine = true,
                                modifier = Modifier.weight(1f)
                            )
                            OutlinedTextField(
                                value = electricConsumptionText,
                                onValueChange = { electricConsumptionText = it },
                                label = { Text("Consumo Elétrico (km/kWh)") },
                                placeholder = { Text("5.8") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                singleLine = true,
                                modifier = Modifier.weight(1f)
                            )
                        }

                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            OutlinedTextField(
                                value = fuelPriceText,
                                onValueChange = { fuelPriceText = it },
                                label = { Text("Preço Combustível (R$/L)") },
                                placeholder = { Text("5.89") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                singleLine = true,
                                modifier = Modifier.weight(1f)
                            )
                            OutlinedTextField(
                                value = homeKwhText,
                                onValueChange = { homeKwhText = it },
                                label = { Text("Tarifa Casa (R$/kWh)") },
                                placeholder = { Text("0.85") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                singleLine = true,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }

                Button(
                    onClick = {
                        val consumption = consumptionText.replace(",", ".").toDoubleOrNull() ?: 10.0
                        val elecConsumption = electricConsumptionText.replace(",", ".").toDoubleOrNull() ?: 6.0
                        val fuelPrice = fuelPriceText.replace(",", ".").toDoubleOrNull() ?: 5.89
                        val homeKwh = homeKwhText.replace(",", ".").toDoubleOrNull() ?: 0.85
                        val publicKwh = publicKwhText.replace(",", ".").toDoubleOrNull() ?: 2.10

                        onSave(
                            currentProfile.copy(
                                vehiclePowertrain = powertrain,
                                isElectric = (powertrain == VehiclePowertrain.ELETRICO),
                                modelName = modelName.ifBlank { currentProfile.modelName },
                                averageConsumption = consumption,
                                electricConsumption = elecConsumption,
                                fuelOrEnergyPrice = fuelPrice,
                                homeKwhPrice = homeKwh,
                                publicKwhPrice = publicKwh,
                                isProfileConfigured = true
                            )
                        )
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = EmeraldGreen),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Salvar Configuração", color = Color.White, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
