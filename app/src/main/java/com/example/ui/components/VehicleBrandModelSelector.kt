package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.VehicleBrandInfo
import com.example.model.VehicleCatalog
import com.example.model.VehicleModelInfo
import com.example.model.VehiclePowertrain
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VehicleBrandModelSelector(
    selectedBrandName: String,
    onBrandSelected: (String) -> Unit,
    selectedModelName: String,
    onModelSelected: (VehicleBrandInfo, VehicleModelInfo) -> Unit,
    onCustomModelChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var brandExpanded by remember { mutableStateOf(false) }
    var modelExpanded by remember { mutableStateOf(false) }

    val currentBrandInfo = VehicleCatalog.brands.find {
        it.brandName.equals(selectedBrandName, ignoreCase = true)
    } ?: VehicleCatalog.brands.first()

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // 1. Marca Dropdown
        ExposedDropdownMenuBox(
            expanded = brandExpanded,
            onExpandedChange = { brandExpanded = !brandExpanded },
            modifier = Modifier.fillMaxWidth()
        ) {
            OutlinedTextField(
                value = if (selectedBrandName.isNotBlank()) selectedBrandName else "Selecione a Marca",
                onValueChange = {},
                readOnly = true,
                label = { Text("Marca do Veículo") },
                leadingIcon = {
                    Icon(Icons.Default.DirectionsCar, contentDescription = null, tint = EmeraldGreen)
                },
                trailingIcon = {
                    ExposedDropdownMenuDefaults.TrailingIcon(expanded = brandExpanded)
                },
                modifier = Modifier
                    .menuAnchor(MenuAnchorType.PrimaryNotEditable, true)
                    .fillMaxWidth()
                    .testTag("dropdown_vehicle_brand"),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = EmeraldGreen,
                    unfocusedBorderColor = BorderDark,
                    focusedTextColor = TextWhite,
                    unfocusedTextColor = TextWhite
                )
            )

            ExposedDropdownMenu(
                expanded = brandExpanded,
                onDismissRequest = { brandExpanded = false },
                modifier = Modifier
                    .background(CardDark)
                    .heightIn(max = 280.dp)
            ) {
                VehicleCatalog.brands.forEach { brand ->
                    DropdownMenuItem(
                        text = {
                            Text(
                                text = brand.brandName,
                                color = if (brand.brandName == selectedBrandName) EmeraldGreen else TextWhite,
                                fontWeight = if (brand.brandName == selectedBrandName) FontWeight.Bold else FontWeight.Normal
                            )
                        },
                        onClick = {
                            onBrandSelected(brand.brandName)
                            brandExpanded = false
                            modelExpanded = true // Automatically open models after choosing brand!
                        },
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
                    )
                }
            }
        }

        // 2. Modelo Dropdown (options populated based on selected brand)
        val availableModels = currentBrandInfo.models

        ExposedDropdownMenuBox(
            expanded = modelExpanded,
            onExpandedChange = { modelExpanded = !modelExpanded },
            modifier = Modifier.fillMaxWidth()
        ) {
            OutlinedTextField(
                value = if (selectedModelName.isNotBlank()) selectedModelName else "Selecione o Modelo",
                onValueChange = onCustomModelChange,
                label = { Text("Modelo da ${currentBrandInfo.brandName}") },
                placeholder = { Text("Escolha da lista ou digite") },
                trailingIcon = {
                    ExposedDropdownMenuDefaults.TrailingIcon(expanded = modelExpanded)
                },
                modifier = Modifier
                    .menuAnchor(MenuAnchorType.PrimaryEditable, true)
                    .fillMaxWidth()
                    .testTag("dropdown_vehicle_model"),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = EmeraldGreen,
                    unfocusedBorderColor = BorderDark,
                    focusedTextColor = TextWhite,
                    unfocusedTextColor = TextWhite
                )
            )

            ExposedDropdownMenu(
                expanded = modelExpanded,
                onDismissRequest = { modelExpanded = false },
                modifier = Modifier
                    .background(CardDark)
                    .heightIn(max = 300.dp)
            ) {
                availableModels.forEach { modelInfo ->
                    DropdownMenuItem(
                        text = {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(
                                        text = modelInfo.name,
                                        color = TextWhite,
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 14.sp
                                    )
                                    Text(
                                        text = "${modelInfo.suggestedPowertrain.displayName} • Méd: ${modelInfo.averageConsumption} ${if (modelInfo.suggestedPowertrain == VehiclePowertrain.ELETRICO) "km/kWh" else "km/L"}",
                                        color = TextSecondary,
                                        fontSize = 11.sp
                                    )
                                }
                            }
                        },
                        onClick = {
                            onModelSelected(currentBrandInfo, modelInfo)
                            modelExpanded = false
                        },
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 10.dp)
                    )
                }
            }
        }
    }
}
