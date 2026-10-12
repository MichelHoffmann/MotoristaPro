package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.ui.theme.*

@Composable
fun AddTripDialog(
    onDismiss: () -> Unit,
    onConfirm: (platform: String, amount: Double, distanceKm: Double, durationMin: Int, time: String, date: String) -> Unit
) {
    var platform by remember { mutableStateOf("99") }
    var amountText by remember { mutableStateOf("25.50") }
    var distanceText by remember { mutableStateOf("7.2") }
    var durationText by remember { mutableStateOf("18") }
    var timeText by remember { mutableStateOf("14:30") }
    var dateText by remember { mutableStateOf("26/05/2025") }

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
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Registrar Corrida",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextWhite
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Fechar", tint = TextSecondary)
                    }
                }

                // Platform Selector
                Text("Plataforma", fontSize = 13.sp, color = TextSecondary)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf("99", "Uber", "InDrive", "Particular").forEach { plat ->
                        val isSel = platform == plat
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isSel) EmeraldGreen else SurfaceDark)
                                .clickable { platform = plat }
                                .padding(vertical = 10.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = plat,
                                color = if (isSel) Color.White else TextSecondary,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                OutlinedTextField(
                    value = amountText,
                    onValueChange = { amountText = it },
                    label = { Text("Valor Líquido (R$)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth().testTag("trip_amount_input"),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = EmeraldGreen,
                        unfocusedBorderColor = BorderDark
                    )
                )

                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(
                        value = distanceText,
                        onValueChange = { distanceText = it },
                        label = { Text("Distância (km)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.weight(1f),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = EmeraldGreen,
                            unfocusedBorderColor = BorderDark
                        )
                    )
                    OutlinedTextField(
                        value = durationText,
                        onValueChange = { durationText = it },
                        label = { Text("Duração (min)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = EmeraldGreen,
                            unfocusedBorderColor = BorderDark
                        )
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(
                        value = timeText,
                        onValueChange = { timeText = it },
                        label = { Text("Horário (HH:mm)") },
                        modifier = Modifier.weight(1f),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = EmeraldGreen,
                            unfocusedBorderColor = BorderDark
                        )
                    )
                    OutlinedTextField(
                        value = dateText,
                        onValueChange = { dateText = it },
                        label = { Text("Data (DD/MM/AAAA)") },
                        modifier = Modifier.weight(1f),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = EmeraldGreen,
                            unfocusedBorderColor = BorderDark
                        )
                    )
                }

                Button(
                    onClick = {
                        val amt = amountText.replace(",", ".").toDoubleOrNull() ?: 0.0
                        val dist = distanceText.replace(",", ".").toDoubleOrNull() ?: 0.0
                        val dur = durationText.toIntOrNull() ?: 0
                        onConfirm(platform, amt, dist, dur, timeText, dateText)
                        onDismiss()
                    },
                    modifier = Modifier.fillMaxWidth().testTag("save_trip_button"),
                    colors = ButtonDefaults.buttonColors(containerColor = EmeraldGreen),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Salvar Corrida", color = Color.White, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun AddExpenseDialog(
    onDismiss: () -> Unit,
    onConfirm: (category: String, amount: Double, description: String, date: String) -> Unit
) {
    var category by remember { mutableStateOf("Combustível") }
    var amountText by remember { mutableStateOf("50.00") }
    var descText by remember { mutableStateOf("") }
    var dateText by remember { mutableStateOf("26/05/2025") }

    val categories = listOf("Combustível", "Alimentação", "Manutenção", "Estacionamento", "Lavagem", "Outros")

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
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Registrar Despesa",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextWhite
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Fechar", tint = TextSecondary)
                    }
                }

                Text("Categoria", fontSize = 13.sp, color = TextSecondary)
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    categories.chunked(3).forEach { rowCats ->
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                            rowCats.forEach { cat ->
                                val isSel = category == cat
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(if (isSel) ExpenseRed else SurfaceDark)
                                        .clickable { category = cat }
                                        .padding(vertical = 8.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = cat,
                                        color = if (isSel) Color.White else TextSecondary,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }
                        }
                    }
                }

                OutlinedTextField(
                    value = amountText,
                    onValueChange = { amountText = it },
                    label = { Text("Valor (R$)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth().testTag("expense_amount_input"),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = ExpenseRed,
                        unfocusedBorderColor = BorderDark
                    )
                )

                OutlinedTextField(
                    value = descText,
                    onValueChange = { descText = it },
                    label = { Text("Descrição / Observação") },
                    placeholder = { Text("Ex: Almoço no centro") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = ExpenseRed,
                        unfocusedBorderColor = BorderDark
                    )
                )

                OutlinedTextField(
                    value = dateText,
                    onValueChange = { dateText = it },
                    label = { Text("Data (DD/MM/AAAA)") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = ExpenseRed,
                        unfocusedBorderColor = BorderDark
                    )
                )

                Button(
                    onClick = {
                        val amt = amountText.replace(",", ".").toDoubleOrNull() ?: 0.0
                        onConfirm(category, amt, descText.ifBlank { category }, dateText)
                        onDismiss()
                    },
                    modifier = Modifier.fillMaxWidth().testTag("save_expense_button"),
                    colors = ButtonDefaults.buttonColors(containerColor = ExpenseRed),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Salvar Despesa", color = Color.White, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun AddFuelDialog(
    onDismiss: () -> Unit,
    onConfirm: (date: String, liters: Double, total: Double, type: String, odo: Double, station: String) -> Unit
) {
    var dateText by remember { mutableStateOf("26/05/2025") }
    var litersText by remember { mutableStateOf("30.0") }
    var totalText by remember { mutableStateOf("204.00") }
    var fuelType by remember { mutableStateOf("Gasolina") }
    var odoText by remember { mutableStateOf("142650") }
    var stationText by remember { mutableStateOf("Posto Ipiranga") }

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
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Registrar Abastecimento",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextWhite
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Fechar", tint = TextSecondary)
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf("Gasolina", "Etanol", "GNV", "Diesel").forEach { type ->
                        val isSel = fuelType == type
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isSel) FuelOrange else SurfaceDark)
                                .clickable { fuelType = type }
                                .padding(vertical = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = type,
                                color = if (isSel) Color.White else TextSecondary,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(
                        value = totalText,
                        onValueChange = { totalText = it },
                        label = { Text("Valor Total (R$)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.weight(1f),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = FuelOrange,
                            unfocusedBorderColor = BorderDark
                        )
                    )
                    OutlinedTextField(
                        value = litersText,
                        onValueChange = { litersText = it },
                        label = { Text("Litros") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.weight(1f),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = FuelOrange,
                            unfocusedBorderColor = BorderDark
                        )
                    )
                }

                OutlinedTextField(
                    value = odoText,
                    onValueChange = { odoText = it },
                    label = { Text("Odômetro atual (km)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = FuelOrange,
                        unfocusedBorderColor = BorderDark
                    )
                )

                OutlinedTextField(
                    value = stationText,
                    onValueChange = { stationText = it },
                    label = { Text("Nome do Posto") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = FuelOrange,
                        unfocusedBorderColor = BorderDark
                    )
                )

                Button(
                    onClick = {
                        val lit = litersText.replace(",", ".").toDoubleOrNull() ?: 0.0
                        val tot = totalText.replace(",", ".").toDoubleOrNull() ?: 0.0
                        val odo = odoText.replace(",", ".").toDoubleOrNull() ?: 0.0
                        onConfirm(dateText, lit, tot, fuelType, odo, stationText)
                        onDismiss()
                    },
                    modifier = Modifier.fillMaxWidth().testTag("save_fuel_button"),
                    colors = ButtonDefaults.buttonColors(containerColor = FuelOrange),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Salvar Abastecimento", color = Color.White, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun EditVehicleDialog(
    currentModel: String,
    currentVersion: String,
    currentMileage: Double,
    currentGoal: Double,
    onDismiss: () -> Unit,
    onConfirm: (model: String, version: String, mileage: Double, dailyGoal: Double) -> Unit
) {
    var model by remember { mutableStateOf(currentModel) }
    var version by remember { mutableStateOf(currentVersion) }
    var mileageText by remember { mutableStateOf(currentMileage.toInt().toString()) }
    var goalText by remember { mutableStateOf(currentGoal.toInt().toString()) }

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
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Dados do Veículo & Metas", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = TextWhite)
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Fechar", tint = TextSecondary)
                    }
                }

                OutlinedTextField(
                    value = model,
                    onValueChange = { model = it },
                    label = { Text("Modelo do Veículo") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = EmeraldGreen,
                        unfocusedBorderColor = BorderDark
                    )
                )

                OutlinedTextField(
                    value = version,
                    onValueChange = { version = it },
                    label = { Text("Versão / Motor") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = EmeraldGreen,
                        unfocusedBorderColor = BorderDark
                    )
                )

                OutlinedTextField(
                    value = mileageText,
                    onValueChange = { mileageText = it },
                    label = { Text("Quilometragem Atual (km)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = EmeraldGreen,
                        unfocusedBorderColor = BorderDark
                    )
                )

                OutlinedTextField(
                    value = goalText,
                    onValueChange = { goalText = it },
                    label = { Text("Meta Diária (R$)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = EmeraldGreen,
                        unfocusedBorderColor = BorderDark
                    )
                )

                Button(
                    onClick = {
                        val mil = mileageText.toDoubleOrNull() ?: currentMileage
                        val gol = goalText.toDoubleOrNull() ?: currentGoal
                        onConfirm(model, version, mil, gol)
                        onDismiss()
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = EmeraldGreen),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Salvar Alterações", color = Color.White, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
