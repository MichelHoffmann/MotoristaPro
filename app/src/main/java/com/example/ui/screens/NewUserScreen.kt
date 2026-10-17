package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
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
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.VehicleCatalog
import com.example.model.VehiclePowertrain
import com.example.model.VehicleProfile
import com.example.ui.components.VehicleBrandModelSelector
import com.example.ui.theme.*

@Composable
fun NewUserScreen(
    currentProfile: VehicleProfile,
    onSaveProfile: (
        name: String,
        model: String,
        year: String,
        fuelType: String,
        isElectric: Boolean,
        consumption: Double,
        price: Double,
        mileage: Double,
        monthlyGoal: Double
    ) -> Unit,
    onCancel: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    var name by remember { mutableStateOf(currentProfile.driverName) }
    var model by remember { mutableStateOf(currentProfile.modelName) }
    var year by remember { mutableStateOf(currentProfile.manufacturingYear) }
    var selectedPowertrain by remember { mutableStateOf(currentProfile.vehiclePowertrain) }
    var fuelType by remember { mutableStateOf(if (currentProfile.fuelType.isNotBlank()) currentProfile.fuelType else "Flex") }
    var isElectric by remember { mutableStateOf(currentProfile.isElectric) }

    val initialBrand = remember(currentProfile.modelName) {
        val parts = currentProfile.modelName.trim().split(" ")
        val matched = VehicleCatalog.brands.find { it.brandName.equals(parts.firstOrNull(), ignoreCase = true) }
        matched?.brandName ?: "Fiat"
    }
    var selectedBrandName by remember { mutableStateOf(initialBrand) }

    var consumptionText by remember {
        mutableStateOf(if (currentProfile.averageConsumption > 0) currentProfile.averageConsumption.toString() else "")
    }
    var priceText by remember {
        mutableStateOf(if (currentProfile.fuelOrEnergyPrice > 0) currentProfile.fuelOrEnergyPrice.toString() else "5.89")
    }
    var mileageText by remember {
        mutableStateOf(if (currentProfile.currentMileage > 0) currentProfile.currentMileage.toInt().toString() else "0")
    }
    var monthlyGoalText by remember {
        mutableStateOf(if (currentProfile.monthlyGoal > 0) currentProfile.monthlyGoal.toInt().toString() else "5000")
    }

    val powertrainOptions = listOf(
        VehiclePowertrain.COMBUSTAO,
        VehiclePowertrain.HIBRIDO,
        VehiclePowertrain.HIBRIDO_PLUGIN,
        VehiclePowertrain.ELETRICO
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(BackgroundDark)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 24.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(EmeraldGreen.copy(alpha = 0.18f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.DirectionsCar,
                        contentDescription = null,
                        tint = EmeraldGreen,
                        modifier = Modifier.size(28.dp)
                    )
                }

                Column {
                    Text(
                        text = if (currentProfile.isProfileConfigured) "Editar Perfil & Veículo" else "Cadastro do Motorista",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextWhite
                    )
                    Text(
                        text = "Configuração do perfil e veículo",
                        fontSize = 13.sp,
                        color = TextSecondary
                    )
                }
            }

            if (onCancel != null) {
                IconButton(onClick = onCancel) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Fechar",
                        tint = TextSecondary
                    )
                }
            }
        }

        // Section 1: Dados do Motorista (Nome completo permitido)
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
                    text = "1. Identificação do Motorista",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = EmeraldGreen
                )

                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Nome Completo") },
                    placeholder = { Text("Ex: Carlos Eduardo da Silva") },
                    supportingText = {
                        Text(
                            text = "Digite seu nome completo. Na tela inicial exibiremos apenas o primeiro nome.",
                            fontSize = 11.sp,
                            color = TextSecondary
                        )
                    },
                    leadingIcon = {
                        Icon(Icons.Outlined.Person, contentDescription = null, tint = TextSecondary)
                    },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_driver_name"),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = EmeraldGreen,
                        unfocusedBorderColor = BorderDark
                    )
                )
            }
        }

        // Section 2: Dados do Veículo com Escolha da Marca e Modelo
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
                    text = "2. Dados do Veículo (Marca e Modelo)",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = EmeraldGreen
                )

                Text(
                    text = "Selecione a marca e escolha o modelo para carregar as configurações do carro:",
                    fontSize = 12.sp,
                    color = TextSecondary
                )

                // Brand and Model dropdown selector
                VehicleBrandModelSelector(
                    selectedBrandName = selectedBrandName,
                    onBrandSelected = { brand ->
                        selectedBrandName = brand
                        val bInfo = VehicleCatalog.brands.find { it.brandName == brand }
                        if (bInfo != null && bInfo.models.isNotEmpty()) {
                            val first = bInfo.models.first()
                            model = "${bInfo.brandName} ${first.name}"
                            selectedPowertrain = first.suggestedPowertrain
                            isElectric = (first.suggestedPowertrain == VehiclePowertrain.ELETRICO)
                            fuelType = first.suggestedPowertrain.displayName
                            consumptionText = first.averageConsumption.toString()
                            priceText = if (isElectric) "0.85" else "5.89"
                        }
                    },
                    selectedModelName = model,
                    onModelSelected = { brandInfo, modelInfo ->
                        model = "${brandInfo.brandName} ${modelInfo.name}"
                        selectedPowertrain = modelInfo.suggestedPowertrain
                        isElectric = (modelInfo.suggestedPowertrain == VehiclePowertrain.ELETRICO)
                        fuelType = modelInfo.suggestedPowertrain.displayName
                        consumptionText = modelInfo.averageConsumption.toString()
                        priceText = if (isElectric) "0.85" else "5.89"
                    },
                    onCustomModelChange = { model = it }
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedTextField(
                        value = year,
                        onValueChange = { year = it },
                        label = { Text("Ano de Fabricação") },
                        placeholder = { Text("Ex: 2022") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("input_vehicle_year"),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = EmeraldGreen,
                            unfocusedBorderColor = BorderDark
                        )
                    )

                    OutlinedTextField(
                        value = mileageText,
                        onValueChange = { mileageText = it },
                        label = { Text("Km Atual (Odômetro)") },
                        placeholder = { Text("Ex: 45000") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("input_vehicle_mileage"),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = EmeraldGreen,
                            unfocusedBorderColor = BorderDark
                        )
                    )
                }
            }
        }

        // Section 3: Tipo de Veículo e Propulsão (Combustão, Híbrido, Híbrido plug-in, Elétrico)
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
                    text = "3. Propulsão & Consumo (Opcional)",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = EmeraldGreen
                )

                // 4 powertrain buttons
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    powertrainOptions.chunked(2).forEach { chunk ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            chunk.forEach { pt ->
                                val isSelected = selectedPowertrain == pt
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(if (isSelected) EmeraldGreen else SurfaceDark)
                                        .clickable {
                                            selectedPowertrain = pt
                                            isElectric = (pt == VehiclePowertrain.ELETRICO)
                                            fuelType = pt.displayName
                                            if (consumptionText.isBlank() || consumptionText == "11.5" || consumptionText == "6.5" || consumptionText == "16.0") {
                                                consumptionText = VehicleCatalog.getDefaultConsumption(pt).toString()
                                            }
                                            if (pt == VehiclePowertrain.ELETRICO) {
                                                priceText = "0.85"
                                            } else {
                                                priceText = "5.89"
                                            }
                                        }
                                        .padding(vertical = 12.dp, horizontal = 8.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = pt.displayName,
                                        fontSize = 12.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                        color = if (isSelected) Color.White else TextSecondary,
                                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                    )
                                }
                            }
                        }
                    }
                }

                // Consumption and Price fields (Consumo NÃO é obrigatório!)
                OutlinedTextField(
                    value = consumptionText,
                    onValueChange = { consumptionText = it },
                    label = { Text(if (isElectric) "Consumo (km/kWh) • Opcional" else "Consumo (km/L) • Opcional") },
                    placeholder = { Text("Padrão médio: ${VehicleCatalog.getDefaultConsumption(selectedPowertrain)}") },
                    supportingText = {
                        Text(
                            text = "Não é obrigatório. Se deixar em branco, adotaremos a média de ${VehicleCatalog.getDefaultConsumption(selectedPowertrain)} ${if (isElectric) "km/kWh" else "km/L"} para este tipo de veículo.",
                            fontSize = 11.sp,
                            color = TextSecondary
                        )
                    },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_fuel_consumption"),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = EmeraldGreen,
                        unfocusedBorderColor = BorderDark
                    )
                )

                OutlinedTextField(
                    value = priceText,
                    onValueChange = { priceText = it },
                    label = { Text(if (isElectric) "Preço Energia (R$/kWh)" else "Preço Combustível (R$/L)") },
                    placeholder = { Text(if (isElectric) "Ex: 0.85" else "Ex: 5.89") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_fuel_price"),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = EmeraldGreen,
                        unfocusedBorderColor = BorderDark
                    )
                )
            }
        }

        // Section 4: Meta Financeira Mensal (Meta deve ser de LUCRO LÍQUIDO)
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
                    text = "4. Meta Financeira Mensal (Lucro Líquido)",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = EmeraldGreen
                )

                OutlinedTextField(
                    value = monthlyGoalText,
                    onValueChange = { monthlyGoalText = it },
                    label = { Text("Meta de Lucro Líquido Mensal (R$)") },
                    placeholder = { Text("Ex: 5000.00") },
                    leadingIcon = {
                        Icon(Icons.Outlined.TrackChanges, contentDescription = null, tint = EmeraldGreen)
                    },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_monthly_goal"),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = EmeraldGreen,
                        unfocusedBorderColor = BorderDark
                    )
                )

                Text(
                    text = "A sua meta é de LUCRO LÍQUIDO (o dinheiro que sobra livre no seu bolso após pagar o combustível e os custos do carro). As metas diárias e de tarifação mínima por km serão calculadas automaticamente para você alcançar esse lucro.",
                    fontSize = 12.sp,
                    color = TextSecondary
                )
            }
        }

        // Save Button
        Button(
            onClick = {
                val defaultConsumption = VehicleCatalog.getDefaultConsumption(selectedPowertrain)
                val parsedCons = consumptionText.replace(",", ".").toDoubleOrNull()
                val consumption = if (parsedCons != null && parsedCons > 0.0) parsedCons else defaultConsumption

                val defaultPrice = VehicleCatalog.getDefaultFuelPrice(selectedPowertrain)
                val parsedPrice = priceText.replace(",", ".").toDoubleOrNull()
                val price = if (parsedPrice != null && parsedPrice > 0.0) parsedPrice else defaultPrice

                val mileage = mileageText.replace(",", ".").toDoubleOrNull() ?: 0.0
                val goal = monthlyGoalText.replace(",", ".").toDoubleOrNull() ?: 5000.0

                onSaveProfile(
                    name.ifBlank { "Motorista" },
                    model.ifBlank { "Meu Veículo" },
                    year.ifBlank { "2022" },
                    selectedPowertrain.displayName,
                    (selectedPowertrain == VehiclePowertrain.ELETRICO),
                    consumption,
                    price,
                    mileage,
                    goal
                )
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(54.dp)
                .testTag("btn_save_new_user_profile"),
            colors = ButtonDefaults.buttonColors(containerColor = EmeraldGreen),
            shape = RoundedCornerShape(14.dp)
        ) {
            Icon(Icons.Default.Check, contentDescription = null, tint = Color.White)
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = if (currentProfile.isProfileConfigured) "Salvar Alterações" else "Iniciar com Valores Zerados",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}
