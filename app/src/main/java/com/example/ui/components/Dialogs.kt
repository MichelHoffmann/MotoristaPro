package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.model.DailyEarning
import com.example.model.calculateWorkStageMinutes
import com.example.ui.theme.*
import com.example.ui.viewmodel.MainViewModel

@Composable
fun RecordDailyWorkDialog(
    initialDate: String = MainViewModel.getTodayDateString(),
    initialAmount: Double = 0.0,
    initialTrips: Int = 0,
    initialKm: Double = 0.0,
    initialNotes: String = "",
    initialStartTime: String = "",
    initialLunchStartTime: String = "",
    initialLunchEndTime: String = "",
    initialEndTime: String = "",
    existingEarnings: List<DailyEarning> = emptyList(),
    onDismiss: () -> Unit,
    onConfirm: (
        date: String,
        totalAmount: Double,
        tripsCount: Int,
        kmDriven: Double,
        notes: String,
        startTime: String,
        lunchStartTime: String,
        lunchEndTime: String,
        endTime: String
    ) -> Unit
) {
    val todayDateStr = remember { MainViewModel.getTodayDateString() }
    val yesterdayDateStr = remember {
        val cal = java.util.Calendar.getInstance()
        cal.add(java.util.Calendar.DAY_OF_YEAR, -1)
        val sdf = java.text.SimpleDateFormat("dd/MM/yyyy", java.util.Locale.getDefault())
        sdf.format(cal.time)
    }
    val dayBeforeYesterdayDateStr = remember {
        val cal = java.util.Calendar.getInstance()
        cal.add(java.util.Calendar.DAY_OF_YEAR, -2)
        val sdf = java.text.SimpleDateFormat("dd/MM/yyyy", java.util.Locale.getDefault())
        sdf.format(cal.time)
    }

    var dateText by remember { mutableStateOf(initialDate) }
    var amountText by remember { mutableStateOf(if (initialAmount > 0) String.format(java.util.Locale.US, "%.2f", initialAmount) else "") }
    var tripsCountText by remember { mutableStateOf(if (initialTrips > 0) initialTrips.toString() else "") }
    var kmDrivenText by remember { mutableStateOf(if (initialKm > 0) String.format(java.util.Locale.US, "%.1f", initialKm) else "") }
    var notesText by remember { mutableStateOf(initialNotes) }

    // Etapas da Jornada de Trabalho
    var startTimeText by remember { mutableStateOf(initialStartTime) }
    var lunchStartTimeText by remember { mutableStateOf(initialLunchStartTime) }
    var lunchEndTimeText by remember { mutableStateOf(initialLunchEndTime) }
    var endTimeText by remember { mutableStateOf(initialEndTime) }

    fun loadDataForDate(targetDate: String) {
        dateText = targetDate
        val found = existingEarnings.find { it.dateStr == targetDate }
        if (found != null) {
            amountText = if (found.totalAmount > 0) String.format(java.util.Locale.US, "%.2f", found.totalAmount) else ""
            tripsCountText = if (found.tripsCount > 0) found.tripsCount.toString() else ""
            kmDrivenText = if (found.kmDriven > 0) String.format(java.util.Locale.US, "%.1f", found.kmDriven) else ""
            notesText = found.notes
            startTimeText = found.startTime
            lunchStartTimeText = found.lunchStartTime
            lunchEndTimeText = found.lunchEndTime
            endTimeText = found.endTime
        } else if (targetDate != initialDate) {
            amountText = ""
            tripsCountText = ""
            kmDrivenText = ""
            notesText = ""
            startTimeText = ""
            lunchStartTimeText = ""
            lunchEndTimeText = ""
            endTimeText = ""
        }
    }

    val calculatedMins = calculateWorkStageMinutes(
        startTime = startTimeText,
        lunchStartTime = lunchStartTimeText,
        lunchEndTime = lunchEndTimeText,
        endTime = endTimeText
    )
    val calculatedHours = calculatedMins / 60.0
    val formattedTime = if (calculatedMins <= 0) "0h" else {
        val h = calculatedMins / 60
        val m = calculatedMins % 60
        if (m == 0) "${h}h" else "${h}h ${m}min"
    }

    val currentAmount = amountText.replace(",", ".").toDoubleOrNull() ?: 0.0
    val hourlyEarnings = if (calculatedHours > 0 && currentAmount > 0) currentAmount / calculatedHours else 0.0
    val existingForCurrentDate = existingEarnings.find { it.dateStr == dateText }

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
                    Column {
                        Text(
                            text = "Lançar Faturamento & Jornada",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextWhite
                        )
                        Text(
                            text = "Ganhos, km e etapas de qualquer dia",
                            fontSize = 12.sp,
                            color = TextSecondary
                        )
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Fechar", tint = TextSecondary)
                    }
                }

                // Quick Date selection chips (Hoje, Ontem, Outro Dia)
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text = "Selecione a Data do Lançamento:",
                        fontSize = 11.sp,
                        color = TextSecondary,
                        fontWeight = FontWeight.SemiBold
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        FilterChip(
                            selected = dateText == todayDateStr,
                            onClick = { loadDataForDate(todayDateStr) },
                            label = { Text("Hoje", fontSize = 11.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = EmeraldGreen,
                                selectedLabelColor = Color.White
                            )
                        )
                        FilterChip(
                            selected = dateText == yesterdayDateStr,
                            onClick = { loadDataForDate(yesterdayDateStr) },
                            label = { Text("Ontem", fontSize = 11.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = EmeraldGreen,
                                selectedLabelColor = Color.White
                            )
                        )
                        FilterChip(
                            selected = dateText == dayBeforeYesterdayDateStr,
                            onClick = { loadDataForDate(dayBeforeYesterdayDateStr) },
                            label = { Text("Anteontem", fontSize = 11.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = EmeraldGreen,
                                selectedLabelColor = Color.White
                            )
                        )
                    }
                }

                OutlinedTextField(
                    value = dateText,
                    onValueChange = { newDate ->
                        dateText = newDate
                        val found = existingEarnings.find { it.dateStr == newDate }
                        if (found != null) {
                            amountText = if (found.totalAmount > 0) String.format(java.util.Locale.US, "%.2f", found.totalAmount) else ""
                            tripsCountText = if (found.tripsCount > 0) found.tripsCount.toString() else ""
                            kmDrivenText = if (found.kmDriven > 0) String.format(java.util.Locale.US, "%.1f", found.kmDriven) else ""
                            notesText = found.notes
                            startTimeText = found.startTime
                            lunchStartTimeText = found.lunchStartTime
                            lunchEndTimeText = found.lunchEndTime
                            endTimeText = found.endTime
                        }
                    },
                    label = { Text("Data (DD/MM/AAAA)") },
                    placeholder = { Text("Ex: 15/10/2026") },
                    leadingIcon = {
                        Icon(Icons.Default.CalendarToday, contentDescription = null, tint = EmeraldGreen, modifier = Modifier.size(18.dp))
                    },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = EmeraldGreen,
                        unfocusedBorderColor = BorderDark
                    )
                )

                if (existingForCurrentDate != null) {
                    Surface(
                        color = EmeraldGreen.copy(alpha = 0.15f),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(Icons.Default.CheckCircle, contentDescription = null, tint = EmeraldGreen, modifier = Modifier.size(16.dp))
                            Text(
                                text = "Dados salvos deste dia carregados para edição",
                                fontSize = 11.sp,
                                color = EmeraldGreen,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }

                // Section: Etapas do Dia
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = SurfaceDark)
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Etapas da Jornada de Trabalho",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = EmeraldGreen
                            )
                            if (calculatedMins > 0) {
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = EmeraldGreen.copy(alpha = 0.2f)
                                ) {
                                    Text(
                                        text = formattedTime,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = EmeraldGreen,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                    )
                                }
                            }
                        }

                        Text(
                            text = "Preencha ao menos 2 etapas (ex: Início e Fim, ou com Almoço):",
                            fontSize = 11.sp,
                            color = TextSecondary
                        )

                        // 1ª Etapa & 2ª Etapa
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedTextField(
                                value = startTimeText,
                                onValueChange = { startTimeText = it },
                                label = { Text("1. Início (HH:mm)") },
                                placeholder = { Text("08:00") },
                                singleLine = true,
                                modifier = Modifier.weight(1f).testTag("input_start_time"),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = EmeraldGreen,
                                    unfocusedBorderColor = BorderDark
                                )
                            )

                            OutlinedTextField(
                                value = lunchStartTimeText,
                                onValueChange = { lunchStartTimeText = it },
                                label = { Text("2. Pausa Almoço") },
                                placeholder = { Text("12:00") },
                                singleLine = true,
                                modifier = Modifier.weight(1f).testTag("input_lunch_start_time"),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = EmeraldGreen,
                                    unfocusedBorderColor = BorderDark
                                )
                            )
                        }

                        // 3ª Etapa & 4ª Etapa
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedTextField(
                                value = lunchEndTimeText,
                                onValueChange = { lunchEndTimeText = it },
                                label = { Text("3. Retorno Almoço") },
                                placeholder = { Text("13:00") },
                                singleLine = true,
                                modifier = Modifier.weight(1f).testTag("input_lunch_end_time"),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = EmeraldGreen,
                                    unfocusedBorderColor = BorderDark
                                )
                            )

                            OutlinedTextField(
                                value = endTimeText,
                                onValueChange = { endTimeText = it },
                                label = { Text("4. Fim Jornada") },
                                placeholder = { Text("18:00") },
                                singleLine = true,
                                modifier = Modifier.weight(1f).testTag("input_end_time"),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = EmeraldGreen,
                                    unfocusedBorderColor = BorderDark
                                )
                            )
                        }

                        if (hourlyEarnings > 0) {
                            Text(
                                text = "Média horária calculada: R$ ${String.format(java.util.Locale.US, "%.2f", hourlyEarnings).replace(".", ",")}/hora",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = ProfitGreen
                            )
                        }
                    }
                }

                // Section: Financeiro
                OutlinedTextField(
                    value = amountText,
                    onValueChange = { amountText = it },
                    label = { Text("Valor Total Ganho no Dia (R$)") },
                    placeholder = { Text("Ex: 312.50") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("daily_amount_input"),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = EmeraldGreen,
                        unfocusedBorderColor = BorderDark
                    )
                )

                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = tripsCountText,
                        onValueChange = { tripsCountText = it },
                        label = { Text("Total de Corridas") },
                        placeholder = { Text("Ex: 18") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.weight(1f).testTag("daily_trips_input"),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = EmeraldGreen,
                            unfocusedBorderColor = BorderDark
                        )
                    )

                    OutlinedTextField(
                        value = kmDrivenText,
                        onValueChange = { kmDrivenText = it },
                        label = { Text("Km Rodados") },
                        placeholder = { Text("Ex: 142.6") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true,
                        modifier = Modifier.weight(1f).testTag("daily_km_input"),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = EmeraldGreen,
                            unfocusedBorderColor = BorderDark
                        )
                    )
                }

                OutlinedTextField(
                    value = notesText,
                    onValueChange = { notesText = it },
                    label = { Text("Observação (Opcional)") },
                    placeholder = { Text("Ex: Uber e 99 - Chuva à tarde") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = EmeraldGreen,
                        unfocusedBorderColor = BorderDark
                    )
                )

                Button(
                    onClick = {
                        val amt = amountText.replace(",", ".").toDoubleOrNull() ?: 0.0
                        val trips = tripsCountText.toIntOrNull() ?: 0
                        val km = kmDrivenText.replace(",", ".").toDoubleOrNull() ?: 0.0
                        onConfirm(
                            dateText,
                            amt,
                            trips,
                            km,
                            notesText,
                            startTimeText,
                            lunchStartTimeText,
                            lunchEndTimeText,
                            endTimeText
                        )
                        onDismiss()
                    },
                    modifier = Modifier.fillMaxWidth().testTag("save_daily_work_button"),
                    colors = ButtonDefaults.buttonColors(containerColor = EmeraldGreen),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Salvar Informações do Dia", color = Color.White, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun AdjustWorkStagesDialog(
    initialDate: String = MainViewModel.getTodayDateString(),
    initialStartTime: String = "",
    initialLunchStartTime: String = "",
    initialLunchEndTime: String = "",
    initialEndTime: String = "",
    onDismiss: () -> Unit,
    onConfirm: (
        date: String,
        startTime: String,
        lunchStartTime: String,
        lunchEndTime: String,
        endTime: String
    ) -> Unit
) {
    var startTimeText by remember { mutableStateOf(initialStartTime) }
    var lunchStartTimeText by remember { mutableStateOf(initialLunchStartTime) }
    var lunchEndTimeText by remember { mutableStateOf(initialLunchEndTime) }
    var endTimeText by remember { mutableStateOf(initialEndTime) }

    val calculatedMins = calculateWorkStageMinutes(
        startTime = startTimeText,
        lunchStartTime = lunchStartTimeText,
        lunchEndTime = lunchEndTimeText,
        endTime = endTimeText
    )
    val formattedTime = if (calculatedMins <= 0) "0h" else {
        val h = calculatedMins / 60
        val m = calculatedMins % 60
        if (m == 0) "${h}h" else "${h}h ${m}min"
    }

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
                    Column {
                        Text(
                            text = "Etapas de Trabalho",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextWhite
                        )
                        Text(
                            text = "Data: $initialDate",
                            fontSize = 12.sp,
                            color = TextSecondary
                        )
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Fechar", tint = TextSecondary)
                    }
                }

                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = SurfaceDark,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("Tempo Total Calculado", fontSize = 11.sp, color = TextSecondary)
                            Text(formattedTime, fontSize = 20.sp, fontWeight = FontWeight.Bold, color = EmeraldGreen)
                        }
                        Button(
                            onClick = {
                                val now = MainViewModel.getCurrentTimeString()
                                when {
                                    startTimeText.isBlank() -> startTimeText = now
                                    lunchStartTimeText.isBlank() -> lunchStartTimeText = now
                                    lunchEndTimeText.isBlank() -> lunchEndTimeText = now
                                    endTimeText.isBlank() -> endTimeText = now
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = EmeraldGreen.copy(alpha = 0.2f)),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Text("Agora (${MainViewModel.getCurrentTimeString()})", color = EmeraldGreen, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                Text(
                    text = "Ajuste os horários das etapas (mínimo de 2 etapas):",
                    fontSize = 12.sp,
                    color = TextSecondary
                )

                // Etapa 1: Início da Jornada
                OutlinedTextField(
                    value = startTimeText,
                    onValueChange = { startTimeText = it },
                    label = { Text("1. Início da Jornada (HH:mm)") },
                    placeholder = { Text("Ex: 08:00") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("adjust_start_time"),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = EmeraldGreen,
                        unfocusedBorderColor = BorderDark
                    )
                )

                // Etapa 2: Pausa para Almoço
                OutlinedTextField(
                    value = lunchStartTimeText,
                    onValueChange = { lunchStartTimeText = it },
                    label = { Text("2. Pausa para Almoço (HH:mm)") },
                    placeholder = { Text("Ex: 12:00 (Opcional)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("adjust_lunch_start_time"),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = EmeraldGreen,
                        unfocusedBorderColor = BorderDark
                    )
                )

                // Etapa 3: Retorno do Almoço
                OutlinedTextField(
                    value = lunchEndTimeText,
                    onValueChange = { lunchEndTimeText = it },
                    label = { Text("3. Retorno do Almoço (HH:mm)") },
                    placeholder = { Text("Ex: 13:00 (Opcional)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("adjust_lunch_end_time"),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = EmeraldGreen,
                        unfocusedBorderColor = BorderDark
                    )
                )

                // Etapa 4: Fim da Jornada
                OutlinedTextField(
                    value = endTimeText,
                    onValueChange = { endTimeText = it },
                    label = { Text("4. Fim da Jornada (HH:mm)") },
                    placeholder = { Text("Ex: 18:00") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("adjust_end_time"),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = EmeraldGreen,
                        unfocusedBorderColor = BorderDark
                    )
                )

                Button(
                    onClick = {
                        onConfirm(
                            initialDate,
                            startTimeText,
                            lunchStartTimeText,
                            lunchEndTimeText,
                            endTimeText
                        )
                        onDismiss()
                    },
                    modifier = Modifier.fillMaxWidth().testTag("save_adjust_stages_button"),
                    colors = ButtonDefaults.buttonColors(containerColor = EmeraldGreen),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Salvar Horários da Jornada", color = Color.White, fontWeight = FontWeight.Bold)
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
    var amountText by remember { mutableStateOf("") }
    var descText by remember { mutableStateOf("") }
    var dateText by remember { mutableStateOf(MainViewModel.getTodayDateString()) }

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
                    placeholder = { Text("Ex: 50.00") },
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
                    placeholder = { Text("Ex: Abastecimento ou Almoço") },
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
