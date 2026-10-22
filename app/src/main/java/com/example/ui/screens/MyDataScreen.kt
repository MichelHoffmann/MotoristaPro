package com.example.ui.screens

import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.*
import com.example.ui.theme.*
import com.example.ui.viewmodel.MainViewModel
import com.example.util.CsvExportHelper
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyDataScreen(
    vehicleProfile: VehicleProfile,
    dailyEarnings: List<DailyEarning>,
    expenses: List<Expense>,
    vehicleCostItems: List<VehicleCostItem>,
    workDays: List<WorkDay>,
    viabilityResult: ViabilityCalculationResult,
    onUpdateProfile: (VehicleProfile) -> Unit,
    onResetAllData: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    // Dialog state for editing personal details
    var showEditPersonalDialog by remember { mutableStateOf(false) }
    var editName by remember { mutableStateOf(vehicleProfile.driverName) }
    var editPlatform by remember { mutableStateOf(vehicleProfile.platform) }
    var editPhone by remember { mutableStateOf(vehicleProfile.phoneNumber) }

    // Dialog state for editing odometer / goal
    var showEditVehicleDialog by remember { mutableStateOf(false) }
    var editMileage by remember { mutableStateOf(vehicleProfile.currentMileage.toInt().toString()) }
    var editMonthlyGoal by remember { mutableStateOf(vehicleProfile.monthlyGoal.toInt().toString()) }
    var editAvgConsumption by remember { mutableStateOf(vehicleProfile.averageConsumption.toString()) }

    var showResetConfirmDialog by remember { mutableStateOf(false) }

    // CSV generator
    val fullCsvContent = remember(vehicleProfile, dailyEarnings, expenses, vehicleCostItems, workDays, viabilityResult) {
        CsvExportHelper.generateFullCsv(
            profile = vehicleProfile,
            dailyEarnings = dailyEarnings,
            expenses = expenses,
            vehicleCostItems = vehicleCostItems,
            workDays = workDays,
            viabilityResult = viabilityResult
        )
    }

    // Android Storage Access Framework launcher to save .CSV directly to Downloads / Files
    val saveCsvLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("text/csv")
    ) { uri ->
        if (uri != null) {
            val success = CsvExportHelper.writeCsvToUri(context, uri, fullCsvContent)
            if (success) {
                Toast.makeText(context, "Arquivo CSV salvo com sucesso!", Toast.LENGTH_LONG).show()
            } else {
                Toast.makeText(context, "Erro ao salvar arquivo CSV.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // Aggregates for display
    val totalEarnings = dailyEarnings.sumOf { it.totalAmount }
    val totalKm = dailyEarnings.sumOf { it.kmDriven }
    val totalTrips = dailyEarnings.sumOf { it.tripsCount }
    val totalFuelCost = dailyEarnings.sumOf { it.fuelOrChargeCost } + expenses.filter {
        it.category == "Combustível" || it.category == "Recarga Elétrica"
    }.sumOf { it.amount }
    val totalOtherExpenses = expenses.filter {
        it.category != "Combustível" && it.category != "Recarga Elétrica"
    }.sumOf { it.amount }
    val totalNetProfit = totalEarnings - (totalFuelCost + totalOtherExpenses)

    val driverInitial = vehicleProfile.driverName.trim().take(1).uppercase().ifBlank { "M" }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(BackgroundDark)
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // TOP HEADER / AVATAR ROW
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(54.dp)
                    .clip(CircleShape)
                    .background(EmeraldGreen.copy(alpha = 0.15f))
                    .border(2.dp, EmeraldGreen, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = driverInitial,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = EmeraldGreen
                )
            }

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = vehicleProfile.driverName.ifBlank { "Motorista Pro" },
                    fontSize = 20.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = TextWhite
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.DirectionsCar,
                        contentDescription = null,
                        tint = EmeraldGreen,
                        modifier = Modifier.size(14.dp)
                    )
                    Text(
                        text = vehicleProfile.platform.ifBlank { "Uber e 99" },
                        fontSize = 13.sp,
                        color = EmeraldGreen,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            IconButton(
                onClick = {
                    editName = vehicleProfile.driverName
                    editPlatform = vehicleProfile.platform
                    editPhone = vehicleProfile.phoneNumber
                    showEditPersonalDialog = true
                },
                modifier = Modifier.testTag("btn_edit_profile_header")
            ) {
                Icon(
                    imageVector = Icons.Outlined.Edit,
                    contentDescription = "Editar informações",
                    tint = TextSecondary
                )
            }
        }

        // ==========================================
        // 1. CARD PRINCIPAL: EXPORTAÇÃO CSV COMPLETA
        // ==========================================
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = CardDark),
            border = BorderStroke(1.5.dp, EmeraldGreen.copy(alpha = 0.6f)),
            modifier = Modifier
                .fillMaxWidth()
                .testTag("card_export_csv")
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(46.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(EmeraldGreen.copy(alpha = 0.15f))
                            .border(1.dp, EmeraldGreen, RoundedCornerShape(12.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.FileDownload,
                            contentDescription = null,
                            tint = EmeraldGreen,
                            modifier = Modifier.size(24.dp)
                        )
                    }

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Exportar Todos os Dados",
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextWhite
                        )
                        Text(
                            text = "Relatório completo em planilha .CSV",
                            fontSize = 12.sp,
                            color = TextSecondary
                        )
                    }
                }

                Text(
                    text = "Exporta todas as suas corridas, km rodados diários, faturamento, abastecimentos e recargas, custos do veículo, hodômetro, meta e planejamento em um só arquivo compatível com Excel e Google Planilhas.",
                    fontSize = 12.5.sp,
                    color = TextSecondary,
                    lineHeight = 17.sp
                )

                // Quick Summary chips
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = Color(0xFF0C2016),
                        border = BorderStroke(1.dp, EmeraldGreen.copy(alpha = 0.3f)),
                        modifier = Modifier.weight(1f)
                    ) {
                        Column(modifier = Modifier.padding(8.dp)) {
                            Text("Dias salvos", fontSize = 10.sp, color = TextSecondary)
                            Text("${dailyEarnings.size} dias", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = EmeraldGreen)
                        }
                    }

                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = Color(0xFF0C2016),
                        border = BorderStroke(1.dp, EmeraldGreen.copy(alpha = 0.3f)),
                        modifier = Modifier.weight(1f)
                    ) {
                        Column(modifier = Modifier.padding(8.dp)) {
                            Text("Km no histórico", fontSize = 10.sp, color = TextSecondary)
                            Text(String.format(Locale.GERMANY, "%.0f km", totalKm), fontSize = 13.sp, fontWeight = FontWeight.Bold, color = EmeraldGreen)
                        }
                    }

                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = Color(0xFF0C2016),
                        border = BorderStroke(1.dp, EmeraldGreen.copy(alpha = 0.3f)),
                        modifier = Modifier.weight(1f)
                    ) {
                        Column(modifier = Modifier.padding(8.dp)) {
                            Text("Total Ganho", fontSize = 10.sp, color = TextSecondary)
                            Text(String.format(Locale.GERMANY, "R$ %.0f", totalEarnings), fontSize = 13.sp, fontWeight = FontWeight.Bold, color = EmeraldGreen)
                        }
                    }
                }

                // Action buttons: Download & Share
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Save file (.csv)
                    Button(
                        onClick = {
                            val defaultFileName = "motorista_pro_relatorio_${System.currentTimeMillis()}.csv"
                            saveCsvLauncher.launch(defaultFileName)
                        },
                        modifier = Modifier
                            .weight(1.2f)
                            .height(48.dp)
                            .testTag("btn_save_csv_file"),
                        colors = ButtonDefaults.buttonColors(containerColor = EmeraldGreen),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.SaveAlt,
                                contentDescription = null,
                                tint = Color(0xFF070E18),
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Salvar CSV",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color(0xFF070E18)
                            )
                        }
                    }

                    // Share
                    OutlinedButton(
                        onClick = {
                            CsvExportHelper.shareCsvText(context, fullCsvContent)
                        },
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                            .testTag("btn_share_csv"),
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.5.dp, EmeraldGreen)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Share,
                                contentDescription = null,
                                tint = EmeraldGreen,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Enviar",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = EmeraldGreen
                            )
                        }
                    }
                }
            }
        }

        // ==========================================
        // 2. CARD: DADOS PESSOAIS
        // ==========================================
        DataSectionCard(
            title = "Dados Pessoais",
            icon = Icons.Default.Person,
            onEditClick = {
                editName = vehicleProfile.driverName
                editPlatform = vehicleProfile.platform
                editPhone = vehicleProfile.phoneNumber
                showEditPersonalDialog = true
            }
        ) {
            DataItemRow(label = "Nome Completo", value = vehicleProfile.driverName.ifBlank { "Não informado" })
            DataItemRow(label = "Plataformas de Trabalho", value = vehicleProfile.platform.ifBlank { "Uber e 99" })
            DataItemRow(label = "Telefone para Contato", value = vehicleProfile.phoneNumber.ifBlank { "Não informado" })
        }

        // ==========================================
        // 3. CARD: DADOS DO VEÍCULO
        // ==========================================
        DataSectionCard(
            title = "Dados do Veículo",
            icon = Icons.Default.DirectionsCar,
            onEditClick = {
                editMileage = vehicleProfile.currentMileage.toInt().toString()
                editMonthlyGoal = vehicleProfile.monthlyGoal.toInt().toString()
                editAvgConsumption = vehicleProfile.averageConsumption.toString()
                showEditVehicleDialog = true
            }
        ) {
            DataItemRow(
                label = "Modelo do Veículo",
                value = vehicleProfile.modelName.ifBlank { "BYD Dolphin Mini" }
            )
            DataItemRow(
                label = "Ano de Fabricação",
                value = vehicleProfile.manufacturingYear.ifBlank { "2025" }
            )
            DataItemRow(
                label = "Combustível / Energia",
                value = vehicleProfile.fuelType
            )
            DataItemRow(
                label = "Consumo Médio Registrado",
                value = "${vehicleProfile.averageConsumption} ${if (vehicleProfile.isElectric) "km/kWh" else "km/L"}"
            )
            DataItemRow(
                label = "Preço de Referência",
                value = String.format(Locale.GERMANY, "R$ %.2f / %s", vehicleProfile.fuelOrEnergyPrice, if (vehicleProfile.isElectric) "kWh" else "L")
            )
            DataItemRow(
                label = "Hodômetro Atual",
                value = String.format(Locale.GERMANY, "%.0f km", vehicleProfile.currentMileage),
                highlight = true
            )
        }

        // ==========================================
        // 4. CARD: METAS E PLANEJAMENTO
        // ==========================================
        DataSectionCard(
            title = "Metas e Planejamento Mensal",
            icon = Icons.Default.TrackChanges,
            onEditClick = {
                editMileage = vehicleProfile.currentMileage.toInt().toString()
                editMonthlyGoal = vehicleProfile.monthlyGoal.toInt().toString()
                editAvgConsumption = vehicleProfile.averageConsumption.toString()
                showEditVehicleDialog = true
            }
        ) {
            DataItemRow(
                label = "Meta Mensal de Lucro Líquido",
                value = String.format(Locale.GERMANY, "R$ %.2f", vehicleProfile.monthlyGoal),
                highlight = true
            )
            val plannedWorkDaysCount = workDays.count { it.isWorkDay }
            DataItemRow(
                label = "Dias de Trabalho no Mês",
                value = "$plannedWorkDaysCount dias planejados"
            )
            DataItemRow(
                label = "Média Diária de Km Estimada",
                value = String.format(Locale.GERMANY, "%.0f km / dia", vehicleProfile.avgDailyKm)
            )
            DataItemRow(
                label = "Jornada Diária Média",
                value = "${vehicleProfile.avgDailyHours.toInt()}h de trabalho"
            )
            if (viabilityResult.hasSufficientData) {
                DataItemRow(
                    label = "Custo Mínimo / Km (Viabilidade)",
                    value = String.format(Locale.GERMANY, "R$ %.2f / km", viabilityResult.recommendedMinRatePerKm)
                )
                DataItemRow(
                    label = "Custo Mínimo / Hora (Viabilidade)",
                    value = String.format(Locale.GERMANY, "R$ %.2f / h", viabilityResult.recommendedMinRatePerHour)
                )
            }
        }

        // ==========================================
        // 5. CARD: RESUMO TOTAL ACUMULADO
        // ==========================================
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = CardDark),
            border = BorderStroke(1.dp, BorderDark),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Assessment,
                        contentDescription = null,
                        tint = EmeraldGreen,
                        modifier = Modifier.size(20.dp)
                    )
                    Text(
                        text = "Histórico Acumulado no App",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextWhite
                    )
                }

                Divider(color = BorderDark, thickness = 0.8.dp)

                DataItemRow(label = "Faturamento Bruto Total", value = String.format(Locale.GERMANY, "R$ %.2f", totalEarnings))
                DataItemRow(label = "Total de Corridas Realizadas", value = "$totalTrips corridas")
                DataItemRow(label = "Quilometragem Total Rodada", value = String.format(Locale.GERMANY, "%.1f km", totalKm))
                DataItemRow(label = "Gasto em Combustível / Recargas", value = String.format(Locale.GERMANY, "R$ %.2f", totalFuelCost))
                DataItemRow(label = "Outras Despesas", value = String.format(Locale.GERMANY, "R$ %.2f", totalOtherExpenses))
                DataItemRow(
                    label = "Lucro Líquido Acumulado",
                    value = String.format(Locale.GERMANY, "R$ %.2f", totalNetProfit),
                    highlight = true
                )
            }
        }

        // ==========================================
        // 6. REDEFINIR DADOS DO APP
        // ==========================================
        TextButton(
            onClick = { showResetConfirmDialog = true },
            modifier = Modifier.align(Alignment.CenterHorizontally)
        ) {
            Icon(
                imageVector = Icons.Default.DeleteOutline,
                contentDescription = null,
                tint = CoralRed,
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = "Zerar todos os dados do aplicativo",
                color = CoralRed,
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold
            )
        }

        Spacer(modifier = Modifier.height(16.dp))
    }

    // DIALOG: Editar Dados Pessoais
    if (showEditPersonalDialog) {
        AlertDialog(
            onDismissRequest = { showEditPersonalDialog = false },
            title = {
                Text(
                    text = "Editar Dados Pessoais",
                    fontWeight = FontWeight.Bold,
                    color = TextWhite
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(
                        value = editName,
                        onValueChange = { editName = it },
                        label = { Text("Nome completo") },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = EmeraldGreen,
                            focusedLabelColor = EmeraldGreen,
                            unfocusedBorderColor = BorderDark
                        ),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = editPlatform,
                        onValueChange = { editPlatform = it },
                        label = { Text("Plataformas (ex: Uber, 99)") },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = EmeraldGreen,
                            focusedLabelColor = EmeraldGreen,
                            unfocusedBorderColor = BorderDark
                        ),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = editPhone,
                        onValueChange = { editPhone = it },
                        label = { Text("Telefone (opcional)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = EmeraldGreen,
                            focusedLabelColor = EmeraldGreen,
                            unfocusedBorderColor = BorderDark
                        ),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        onUpdateProfile(
                            vehicleProfile.copy(
                                driverName = editName,
                                platform = editPlatform,
                                phoneNumber = editPhone
                            )
                        )
                        showEditPersonalDialog = false
                        Toast.makeText(context, "Informações atualizadas!", Toast.LENGTH_SHORT).show()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = EmeraldGreen)
                ) {
                    Text("Salvar", color = Color(0xFF070E18), fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showEditPersonalDialog = false }) {
                    Text("Cancelar", color = TextSecondary)
                }
            },
            containerColor = CardDark,
            shape = RoundedCornerShape(16.dp)
        )
    }

    // DIALOG: Editar Hodômetro / Meta
    if (showEditVehicleDialog) {
        AlertDialog(
            onDismissRequest = { showEditVehicleDialog = false },
            title = {
                Text(
                    text = "Atualizar Hodômetro e Metas",
                    fontWeight = FontWeight.Bold,
                    color = TextWhite
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(
                        value = editMileage,
                        onValueChange = { editMileage = it },
                        label = { Text("Hodômetro atual (km)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = EmeraldGreen,
                            focusedLabelColor = EmeraldGreen,
                            unfocusedBorderColor = BorderDark
                        ),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = editMonthlyGoal,
                        onValueChange = { editMonthlyGoal = it },
                        label = { Text("Meta mensal de lucro (R$)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = EmeraldGreen,
                            focusedLabelColor = EmeraldGreen,
                            unfocusedBorderColor = BorderDark
                        ),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = editAvgConsumption,
                        onValueChange = { editAvgConsumption = it },
                        label = { Text("Consumo médio (${if (vehicleProfile.isElectric) "km/kWh" else "km/L"})") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = EmeraldGreen,
                            focusedLabelColor = EmeraldGreen,
                            unfocusedBorderColor = BorderDark
                        ),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val km = editMileage.replace(",", ".").toDoubleOrNull() ?: vehicleProfile.currentMileage
                        val goal = editMonthlyGoal.replace(",", ".").toDoubleOrNull() ?: vehicleProfile.monthlyGoal
                        val cons = editAvgConsumption.replace(",", ".").toDoubleOrNull() ?: vehicleProfile.averageConsumption

                        onUpdateProfile(
                            vehicleProfile.copy(
                                currentMileage = km,
                                monthlyGoal = goal,
                                averageConsumption = cons
                            )
                        )
                        showEditVehicleDialog = false
                        Toast.makeText(context, "Valores atualizados!", Toast.LENGTH_SHORT).show()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = EmeraldGreen)
                ) {
                    Text("Salvar", color = Color(0xFF070E18), fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showEditVehicleDialog = false }) {
                    Text("Cancelar", color = TextSecondary)
                }
            },
            containerColor = CardDark,
            shape = RoundedCornerShape(16.dp)
        )
    }

    // DIALOG: Confirmação de Zerar Dados
    if (showResetConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showResetConfirmDialog = false },
            title = {
                Text("Zerar todos os dados?", fontWeight = FontWeight.Bold, color = TextWhite)
            },
            text = {
                Text(
                    "Esta ação apagará todas as corridas, despesas, hodômetro e configurações salvas no aplicativo. Não é possível desfazer.",
                    color = TextSecondary,
                    lineHeight = 18.sp
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        onResetAllData()
                        showResetConfirmDialog = false
                        Toast.makeText(context, "Todos os dados foram redefinidos.", Toast.LENGTH_LONG).show()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = CoralRed)
                ) {
                    Text("Sim, zerar tudo", color = TextWhite, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showResetConfirmDialog = false }) {
                    Text("Cancelar", color = TextSecondary)
                }
            },
            containerColor = CardDark,
            shape = RoundedCornerShape(16.dp)
        )
    }
}

@Composable
private fun DataSectionCard(
    title: String,
    icon: ImageVector,
    onEditClick: () -> Unit,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = CardDark),
        border = BorderStroke(1.dp, BorderDark),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = EmeraldGreen,
                        modifier = Modifier.size(19.dp)
                    )
                    Text(
                        text = title,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextWhite
                    )
                }

                IconButton(
                    onClick = onEditClick,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Edit,
                        contentDescription = "Editar $title",
                        tint = TextSecondary,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            Divider(color = BorderDark, thickness = 0.8.dp)

            content()
        }
    }
}

@Composable
private fun DataItemRow(
    label: String,
    value: String,
    highlight: Boolean = false
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            fontSize = 13.sp,
            color = TextSecondary,
            fontWeight = FontWeight.Normal
        )
        Text(
            text = value,
            fontSize = 13.5.sp,
            color = if (highlight) EmeraldGreen else TextWhite,
            fontWeight = if (highlight) FontWeight.Bold else FontWeight.Medium
        )
    }
}
