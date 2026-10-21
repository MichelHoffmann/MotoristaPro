package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
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
    initialFuelCost: Double = 0.0,
    initialIsElectric: Boolean = false,
    initialOtherExpenses: Double = 0.0,
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
        endTime: String,
        fuelOrChargeCost: Double,
        isElectricCharge: Boolean,
        otherExpensesCost: Double
    ) -> Unit
) {
    val todayDateStr = remember { MainViewModel.getTodayDateString() }
    val yesterdayDateStr = remember {
        val cal = java.util.Calendar.getInstance()
        cal.add(java.util.Calendar.DAY_OF_YEAR, -1)
        val sdf = java.text.SimpleDateFormat("dd/MM/yyyy", java.util.Locale.getDefault())
        sdf.format(cal.time)
    }

    var dateText by remember { mutableStateOf(initialDate) }
    var amountText by remember {
        mutableStateOf(if (initialAmount > 0) String.format(java.util.Locale.US, "%.2f", initialAmount) else "")
    }
    var tripsCountText by remember {
        mutableStateOf(if (initialTrips > 0) initialTrips.toString() else "")
    }
    var kmDrivenText by remember {
        mutableStateOf(if (initialKm > 0) String.format(java.util.Locale.US, "%.1f", initialKm) else "")
    }
    var notesText by remember { mutableStateOf(initialNotes) }

    // Abastecimento ou Recarga
    var didFuelOrCharge by remember { mutableStateOf(initialFuelCost > 0) }
    var isElectricCharge by remember { mutableStateOf(initialIsElectric) }
    var fuelCostText by remember {
        mutableStateOf(if (initialFuelCost > 0) String.format(java.util.Locale.US, "%.2f", initialFuelCost) else "")
    }

    // Outros Gastos (alimentação, etc.)
    var otherExpensesText by remember {
        mutableStateOf(if (initialOtherExpenses > 0) String.format(java.util.Locale.US, "%.2f", initialOtherExpenses) else "")
    }

    // Etapas da Jornada de Trabalho (opcional / expansível)
    var showWorkStagesSection by remember { mutableStateOf(false) }
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
            didFuelOrCharge = found.fuelOrChargeCost > 0
            isElectricCharge = found.isElectricCharge
            fuelCostText = if (found.fuelOrChargeCost > 0) String.format(java.util.Locale.US, "%.2f", found.fuelOrChargeCost) else ""
        } else if (targetDate != initialDate) {
            amountText = ""
            tripsCountText = ""
            kmDrivenText = ""
            notesText = ""
            startTimeText = ""
            lunchStartTimeText = ""
            lunchEndTimeText = ""
            endTimeText = ""
            didFuelOrCharge = false
            fuelCostText = ""
            otherExpensesText = ""
        }
    }

    val totalGanho = amountText.replace(",", ".").toDoubleOrNull() ?: 0.0
    val gastoRecargaOuCombustivel = if (didFuelOrCharge) (fuelCostText.replace(",", ".").toDoubleOrNull() ?: 0.0) else 0.0
    val outrosGastos = otherExpensesText.replace(",", ".").toDoubleOrNull() ?: 0.0
    val lucroLiquido = totalGanho - gastoRecargaOuCombustivel - outrosGastos

    Dialog(
        onDismissRequest = onDismiss,
        properties = androidx.compose.ui.window.DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = Color(0xFF070E18),
            border = androidx.compose.foundation.BorderStroke(1.dp, BorderDark),
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .padding(vertical = 16.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // HEADER SECTION (Exact match to right screen in mockup)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Green checkered flag badge
                        Box(
                            modifier = Modifier
                                .size(42.dp)
                                .clip(CircleShape)
                                .background(EmeraldGreen.copy(alpha = 0.15f))
                                .border(1.5.dp, EmeraldGreen, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Flag,
                                contentDescription = null,
                                tint = EmeraldGreen,
                                modifier = Modifier.size(22.dp)
                            )
                        }

                        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                            Text(
                                text = "Encerrar expediente",
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextWhite
                            )
                            Text(
                                text = "Informe os dados do seu dia.",
                                fontSize = 12.5.sp,
                                color = TextSecondary
                            )
                        }
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Fechar",
                            tint = TextSecondary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                // Quick Date Switcher (Hoje / Ontem / Selecionar)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = if (dateText == todayDateStr) EmeraldGreen.copy(alpha = 0.2f) else DeepRowDark,
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            if (dateText == todayDateStr) EmeraldGreen else BorderDark
                        ),
                        modifier = Modifier
                            .clickable { loadDataForDate(todayDateStr) }
                            .padding(vertical = 2.dp)
                    ) {
                        Text(
                            text = "Hoje ($todayDateStr)",
                            fontSize = 11.sp,
                            fontWeight = if (dateText == todayDateStr) FontWeight.Bold else FontWeight.Normal,
                            color = if (dateText == todayDateStr) EmeraldGreen else TextSecondary,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                        )
                    }

                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = if (dateText == yesterdayDateStr) EmeraldGreen.copy(alpha = 0.2f) else DeepRowDark,
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            if (dateText == yesterdayDateStr) EmeraldGreen else BorderDark
                        ),
                        modifier = Modifier
                            .clickable { loadDataForDate(yesterdayDateStr) }
                            .padding(vertical = 2.dp)
                    ) {
                        Text(
                            text = "Ontem ($yesterdayDateStr)",
                            fontSize = 11.sp,
                            fontWeight = if (dateText == yesterdayDateStr) FontWeight.Bold else FontWeight.Normal,
                            color = if (dateText == yesterdayDateStr) EmeraldGreen else TextSecondary,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                        )
                    }
                }

                // 7 PILL CARDS (Exact match to right screen in mockup)

                // 1. Total de corridas
                DailyInputRow(
                    icon = Icons.Default.DirectionsCar,
                    title = "Total de corridas",
                    value = tripsCountText,
                    onValueChange = { tripsCountText = it },
                    placeholder = "0",
                    suffix = "",
                    keyboardType = KeyboardType.Number,
                    testTag = "input_total_corridas"
                )

                // 2. Km rodados
                DailyInputRow(
                    icon = Icons.Default.Navigation,
                    title = "Km rodados",
                    value = kmDrivenText,
                    onValueChange = { kmDrivenText = it },
                    placeholder = "0",
                    suffix = "km",
                    keyboardType = KeyboardType.Decimal,
                    testTag = "input_km_rodados"
                )

                // 3. Valor total ganho
                DailyInputRow(
                    icon = Icons.Default.AttachMoney,
                    title = "Valor total ganho",
                    value = amountText,
                    onValueChange = { amountText = it },
                    placeholder = "0,00",
                    prefix = "R$ ",
                    keyboardType = KeyboardType.Decimal,
                    testTag = "input_valor_total_ganho"
                )

                // 4. Recarregou / Abasteceu o carro? [ Sim ] [ Não ]
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = DeepRowDark,
                    border = androidx.compose.foundation.BorderStroke(1.dp, BorderDark),
                    modifier = Modifier.fillMaxWidth()
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
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(38.dp)
                                    .clip(CircleShape)
                                    .background(SurfaceDark),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = if (isElectricCharge) Icons.Default.ElectricBolt else Icons.Default.LocalGasStation,
                                    contentDescription = null,
                                    tint = if (didFuelOrCharge) EmeraldGreen else Color(0xFF94A3B8),
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Text(
                                text = if (isElectricCharge) "Recarregou o carro?" else "Abasteceu o carro?",
                                fontSize = 13.5.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = TextWhite
                            )
                        }

                        // Toggle pills: [ Sim ] [ Não ]
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            // Sim pill
                            Surface(
                                shape = RoundedCornerShape(16.dp),
                                color = if (didFuelOrCharge) EmeraldGreen else SurfaceDark,
                                border = androidx.compose.foundation.BorderStroke(
                                    1.dp,
                                    if (didFuelOrCharge) EmeraldGreen else BorderDark
                                ),
                                modifier = Modifier
                                    .clickable { didFuelOrCharge = true }
                                    .testTag("toggle_recarga_sim")
                            ) {
                                Text(
                                    text = "Sim",
                                    fontSize = 12.sp,
                                    fontWeight = if (didFuelOrCharge) FontWeight.Bold else FontWeight.Medium,
                                    color = if (didFuelOrCharge) Color.Black else TextSecondary,
                                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
                                )
                            }

                            // Não pill
                            Surface(
                                shape = RoundedCornerShape(16.dp),
                                color = if (!didFuelOrCharge) SurfaceDark else Color.Transparent,
                                border = androidx.compose.foundation.BorderStroke(
                                    1.dp,
                                    if (!didFuelOrCharge) EmeraldGreen.copy(alpha = 0.5f) else BorderDark
                                ),
                                modifier = Modifier
                                    .clickable {
                                        didFuelOrCharge = false
                                        fuelCostText = ""
                                    }
                                    .testTag("toggle_recarga_nao")
                            ) {
                                Text(
                                    text = "Não",
                                    fontSize = 12.sp,
                                    fontWeight = if (!didFuelOrCharge) FontWeight.Bold else FontWeight.Medium,
                                    color = if (!didFuelOrCharge) TextWhite else TextSecondary,
                                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
                                )
                            }
                        }
                    }
                }

                // 5. Gasto com recarga / combustível
                if (didFuelOrCharge) {
                    DailyInputRow(
                        icon = if (isElectricCharge) Icons.Default.ElectricBolt else Icons.Default.LocalGasStation,
                        title = if (isElectricCharge) "Gasto com recarga" else "Gasto com combustível",
                        value = fuelCostText,
                        onValueChange = { fuelCostText = it },
                        placeholder = "0,00",
                        prefix = "R$ ",
                        keyboardType = KeyboardType.Decimal,
                        testTag = "input_gasto_recarga"
                    )
                }

                // 6. Outros gastos (alimentação, etc.)
                DailyInputRow(
                    icon = Icons.Default.Restaurant,
                    title = "Outros gastos (alimentação, etc.)",
                    value = otherExpensesText,
                    onValueChange = { otherExpensesText = it },
                    placeholder = "0,00",
                    prefix = "R$ ",
                    keyboardType = KeyboardType.Decimal,
                    testTag = "input_outros_gastos"
                )

                // 7. Resumo do dia / Lucro líquido (Real-time dynamic calculated row)
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = DeepRowDark,
                    border = androidx.compose.foundation.BorderStroke(1.dp, EmeraldGreen.copy(alpha = 0.4f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 13.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(38.dp)
                                    .clip(CircleShape)
                                    .background(EmeraldGreen.copy(alpha = 0.15f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.ShowChart,
                                    contentDescription = null,
                                    tint = EmeraldGreen,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Column {
                                Text(
                                    text = "Resumo do dia",
                                    fontSize = 13.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TextWhite
                                )
                                Text(
                                    text = "Lucro líquido",
                                    fontSize = 11.5.sp,
                                    color = TextSecondary
                                )
                            }
                        }

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(
                                text = if (lucroLiquido >= 0) {
                                    "R$ ${String.format(java.util.Locale.US, "%.2f", lucroLiquido).replace(".", ",")}"
                                } else {
                                    "- R$ ${String.format(java.util.Locale.US, "%.2f", -lucroLiquido).replace(".", ",")}"
                                },
                                fontSize = 17.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = if (lucroLiquido >= 0) EmeraldGreen else ExpenseRed
                            )
                            Icon(
                                imageVector = Icons.Default.ChevronRight,
                                contentDescription = null,
                                tint = TextSecondary.copy(alpha = 0.6f),
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }

                // Opcional: Expandir Etapas da Jornada de Trabalho (Início, Pausa, Fim)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { showWorkStagesSection = !showWorkStagesSection }
                        .padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (showWorkStagesSection) "Ocultar horários da jornada ▲" else "Adicionar horários da jornada (opcional) ▼",
                        fontSize = 11.5.sp,
                        color = TextSecondary,
                        fontWeight = FontWeight.Medium
                    )
                }

                if (showWorkStagesSection) {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = SurfaceDark,
                        border = androidx.compose.foundation.BorderStroke(1.dp, BorderDark),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = "Horários de Trabalho (HH:mm)",
                                fontSize = 11.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = EmeraldGreen
                            )
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                OutlinedTextField(
                                    value = startTimeText,
                                    onValueChange = { startTimeText = it },
                                    label = { Text("1. Início", fontSize = 10.sp) },
                                    placeholder = { Text("08:00") },
                                    singleLine = true,
                                    modifier = Modifier.weight(1f),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = EmeraldGreen,
                                        unfocusedBorderColor = BorderDark
                                    )
                                )
                                OutlinedTextField(
                                    value = lunchStartTimeText,
                                    onValueChange = { lunchStartTimeText = it },
                                    label = { Text("2. Almoço", fontSize = 10.sp) },
                                    placeholder = { Text("12:00") },
                                    singleLine = true,
                                    modifier = Modifier.weight(1f),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = EmeraldGreen,
                                        unfocusedBorderColor = BorderDark
                                    )
                                )
                            }
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                OutlinedTextField(
                                    value = lunchEndTimeText,
                                    onValueChange = { lunchEndTimeText = it },
                                    label = { Text("3. Retorno", fontSize = 10.sp) },
                                    placeholder = { Text("13:00") },
                                    singleLine = true,
                                    modifier = Modifier.weight(1f),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = EmeraldGreen,
                                        unfocusedBorderColor = BorderDark
                                    )
                                )
                                OutlinedTextField(
                                    value = endTimeText,
                                    onValueChange = { endTimeText = it },
                                    label = { Text("4. Fim", fontSize = 10.sp) },
                                    placeholder = { Text("18:00") },
                                    singleLine = true,
                                    modifier = Modifier.weight(1f),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = EmeraldGreen,
                                        unfocusedBorderColor = BorderDark
                                    )
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                // ACTION BUTTON: "Finalizar dia" (Big glowing green pill matching mockup)
                Button(
                    onClick = {
                        val amt = amountText.replace(",", ".").toDoubleOrNull() ?: 0.0
                        val trips = tripsCountText.toIntOrNull() ?: 0
                        val km = kmDrivenText.replace(",", ".").toDoubleOrNull() ?: 0.0
                        val fuelCost = if (didFuelOrCharge) fuelCostText.replace(",", ".").toDoubleOrNull() ?: 0.0 else 0.0
                        val otherExp = otherExpensesText.replace(",", ".").toDoubleOrNull() ?: 0.0

                        onConfirm(
                            dateText,
                            amt,
                            trips,
                            km,
                            notesText,
                            startTimeText,
                            lunchStartTimeText,
                            lunchEndTimeText,
                            endTimeText,
                            fuelCost,
                            isElectricCharge,
                            otherExp
                        )
                        onDismiss()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .testTag("save_daily_work_button"),
                    colors = ButtonDefaults.buttonColors(containerColor = EmeraldGreen),
                    shape = RoundedCornerShape(26.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = null,
                            tint = Color.Black,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Finalizar dia",
                            color = Color.Black,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 16.sp
                        )
                    }
                }

                // SECONDARY ACTION BUTTON: "Cancelar"
                TextButton(
                    onClick = onDismiss,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("cancel_daily_work_button")
                ) {
                    Text(
                        text = "Cancelar",
                        color = TextSecondary,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}

@Composable
private fun DailyInputRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String = "0",
    prefix: String = "",
    suffix: String = "",
    keyboardType: KeyboardType = KeyboardType.Text,
    testTag: String = ""
) {
    Surface(
        shape = RoundedCornerShape(14.dp),
        color = DeepRowDark,
        border = androidx.compose.foundation.BorderStroke(1.dp, BorderDark),
        modifier = Modifier.fillMaxWidth()
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
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.weight(1f)
            ) {
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(SurfaceDark),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = Color(0xFF94A3B8),
                        modifier = Modifier.size(20.dp)
                    )
                }
                Text(
                    text = title,
                    fontSize = 13.5.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = TextWhite
                )
            }

            // Editable value input aligned to the right with chevron
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                if (prefix.isNotEmpty()) {
                    Text(
                        text = prefix,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (value.isNotEmpty()) TextWhite else TextSecondary.copy(alpha = 0.5f)
                    )
                }

                androidx.compose.foundation.text.BasicTextField(
                    value = value,
                    onValueChange = onValueChange,
                    textStyle = androidx.compose.ui.text.TextStyle(
                        color = TextWhite,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = androidx.compose.ui.text.style.TextAlign.End
                    ),
                    keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
                    singleLine = true,
                    modifier = Modifier
                        .widthIn(min = 40.dp, max = 100.dp)
                        .testTag(testTag),
                    decorationBox = { innerTextField ->
                        Box(contentAlignment = Alignment.CenterEnd) {
                            if (value.isEmpty()) {
                                Text(
                                    text = placeholder,
                                    color = TextSecondary.copy(alpha = 0.5f),
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Normal,
                                    textAlign = androidx.compose.ui.text.style.TextAlign.End
                                )
                            }
                            innerTextField()
                        }
                    }
                )

                if (suffix.isNotEmpty()) {
                    Spacer(modifier = Modifier.width(3.dp))
                    Text(
                        text = suffix,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        color = TextSecondary
                    )
                }

                Spacer(modifier = Modifier.width(4.dp))
                Icon(
                    imageVector = Icons.Default.ChevronRight,
                    contentDescription = null,
                    tint = TextSecondary.copy(alpha = 0.6f),
                    modifier = Modifier.size(18.dp)
                )
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
