package com.example.ui.screens

import androidx.compose.foundation.background
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
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.VehicleProfile
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
        dailyGoal: Double
    ) -> Unit,
    onCancel: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    var name by remember { mutableStateOf(currentProfile.driverName) }
    var model by remember { mutableStateOf(currentProfile.modelName) }
    var year by remember { mutableStateOf(currentProfile.manufacturingYear) }
    var fuelType by remember { mutableStateOf(if (currentProfile.fuelType.isNotBlank()) currentProfile.fuelType else "Flex") }
    var isElectric by remember { mutableStateOf(currentProfile.isElectric) }

    var consumptionText by remember {
        mutableStateOf(if (currentProfile.averageConsumption > 0) currentProfile.averageConsumption.toString() else "11.5")
    }
    var priceText by remember {
        mutableStateOf(if (currentProfile.fuelOrEnergyPrice > 0) currentProfile.fuelOrEnergyPrice.toString() else "5.89")
    }
    var mileageText by remember {
        mutableStateOf(if (currentProfile.currentMileage > 0) currentProfile.currentMileage.toInt().toString() else "0")
    }
    var dailyGoalText by remember {
        mutableStateOf(if (currentProfile.dailyGoal > 0) currentProfile.dailyGoal.toInt().toString() else "250")
    }

    val fuelOptions = listOf(
        "Flex",
        "Gasolina",
        "Etanol",
        "GNV",
        "Diesel",
        "Elétrico",
        "Híbrido"
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
                        text = if (currentProfile.isProfileConfigured) "Editar Perfil & Veículo" else "Novo Usuário",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextWhite
                    )
                    Text(
                        text = "Configuração do motorista",
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

        // Section 1: Dados do Motorista
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
                    text = "1. Identificação do Motorista",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = EmeraldGreen
                )

                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Seu Nome") },
                    placeholder = { Text("Ex: Carlos Silva") },
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

        // Section 2: Dados do Veículo
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
                    text = "2. Dados do Veículo",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = EmeraldGreen
                )

                OutlinedTextField(
                    value = model,
                    onValueChange = { model = it },
                    label = { Text("Marca e Modelo") },
                    placeholder = { Text("Ex: Fiat Uno Way, Onix, etc.") },
                    leadingIcon = {
                        Icon(Icons.Outlined.DirectionsCar, contentDescription = null, tint = TextSecondary)
                    },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_vehicle_model"),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = EmeraldGreen,
                        unfocusedBorderColor = BorderDark
                    )
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

        // Section 3: Combustível / Energia
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
                    text = "3. Tipo de Combustível / Propulsão",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = EmeraldGreen
                )

                // Chips for fuel type
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    fuelOptions.chunked(3).forEach { chunk ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            chunk.forEach { option ->
                                val isSelected = fuelType.equals(option, ignoreCase = true)
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(if (isSelected) EmeraldGreen else SurfaceDark)
                                        .clickable {
                                            fuelType = option
                                            isElectric = (option == "Elétrico")
                                            if (isElectric) {
                                                consumptionText = "6.5"
                                                priceText = "0.85"
                                            } else {
                                                if (consumptionText == "6.5") consumptionText = "11.5"
                                                if (priceText == "0.85") priceText = "5.89"
                                            }
                                        }
                                        .padding(vertical = 10.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = option,
                                        fontSize = 12.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                        color = if (isSelected) Color.White else TextSecondary
                                    )
                                }
                            }
                        }
                    }
                }

                // Consumption and Price fields
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedTextField(
                        value = consumptionText,
                        onValueChange = { consumptionText = it },
                        label = { Text(if (isElectric) "Consumo (km/kWh)" else "Consumo (km/L)") },
                        placeholder = { Text(if (isElectric) "Ex: 6.5" else "Ex: 11.5") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("input_fuel_consumption"),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = EmeraldGreen,
                            unfocusedBorderColor = BorderDark
                        )
                    )

                    OutlinedTextField(
                        value = priceText,
                        onValueChange = { priceText = it },
                        label = { Text(if (isElectric) "Energia (R$/kWh)" else "Preço Médio (R$/L)") },
                        placeholder = { Text(if (isElectric) "Ex: 0.85" else "Ex: 5.89") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("input_fuel_price"),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = EmeraldGreen,
                            unfocusedBorderColor = BorderDark
                        )
                    )
                }
            }
        }

        // Section 4: Metas Financeiras
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
                    text = "4. Meta Financeira Diária",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = EmeraldGreen
                )

                OutlinedTextField(
                    value = dailyGoalText,
                    onValueChange = { dailyGoalText = it },
                    label = { Text("Meta de Ganhos por Dia (R$)") },
                    placeholder = { Text("Ex: 250.00") },
                    leadingIcon = {
                        Icon(Icons.Outlined.TrackChanges, contentDescription = null, tint = EmeraldGreen)
                    },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_daily_goal"),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = EmeraldGreen,
                        unfocusedBorderColor = BorderDark
                    )
                )

                Text(
                    text = "O app iniciará com todos os ganhos e despesas zerados para você começar seu controle do zero.",
                    fontSize = 12.sp,
                    color = TextSecondary
                )
            }
        }

        // Save Button
        Button(
            onClick = {
                val consumption = consumptionText.replace(",", ".").toDoubleOrNull() ?: 11.5
                val price = priceText.replace(",", ".").toDoubleOrNull() ?: 5.89
                val mileage = mileageText.replace(",", ".").toDoubleOrNull() ?: 0.0
                val goal = dailyGoalText.replace(",", ".").toDoubleOrNull() ?: 250.0

                onSaveProfile(
                    name.ifBlank { "Motorista" },
                    model.ifBlank { "Meu Veículo" },
                    year.ifBlank { "2022" },
                    fuelType,
                    isElectric,
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
