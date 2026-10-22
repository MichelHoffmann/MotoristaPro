package com.example.ui.screens

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
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
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.VehicleCatalog
import com.example.model.VehiclePowertrain
import com.example.model.VehicleProfile
import com.example.ui.theme.*

@Composable
fun RegistrationWizardScreen(
    appName: String = "Motorista Pro",
    currentProfile: VehicleProfile = VehicleProfile(),
    onNavigateBack: () -> Unit,
    onComplete: (
        name: String,
        model: String,
        year: String,
        fuelType: String,
        isElectric: Boolean,
        consumption: Double,
        price: Double,
        mileage: Double,
        monthlyGoal: Double,
        platform: String,
        phoneNumber: String,
        vehicleBrand: String
    ) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var currentStep by remember { mutableStateOf(1) } // 1: Dados pessoais, 2: Dados do veículo

    // STEP 1 STATE: Dados pessoais
    var fullName by remember { mutableStateOf(currentProfile.driverName) }
    var selectedPlatform by remember { mutableStateOf("Uber, 99 e InDrive") }
    var phoneNumber by remember { mutableStateOf("") }
    var showPlatformDialog by remember { mutableStateOf(false) }

    // STEP 2 STATE: Dados do veículo
    val initialBrand = remember(currentProfile.modelName) {
        val parts = currentProfile.modelName.trim().split(" ")
        val matched = VehicleCatalog.brands.find { it.brandName.equals(parts.firstOrNull(), ignoreCase = true) }
        matched?.brandName ?: "BYD"
    }
    var selectedBrand by remember { mutableStateOf(initialBrand) }
    var selectedModel by remember {
        mutableStateOf(if (currentProfile.modelName.isNotBlank()) currentProfile.modelName.removePrefix("$selectedBrand ").trim() else "Dolphin Mini")
    }
    var selectedYear by remember {
        mutableStateOf(if (currentProfile.manufacturingYear.isNotBlank()) currentProfile.manufacturingYear else "2025")
    }
    var isElectric by remember { mutableStateOf(if (currentProfile.isProfileConfigured) currentProfile.isElectric else true) }
    var consumptionText by remember {
        mutableStateOf(
            if (currentProfile.averageConsumption > 0) {
                currentProfile.averageConsumption.toString()
            } else {
                if (isElectric) "7.2" else "12.5"
            }
        )
    }
    var odometerText by remember {
        mutableStateOf(if (currentProfile.currentMileage > 0) currentProfile.currentMileage.toInt().toString() else "0")
    }

    // Dialog state for pickers
    var showBrandDialog by remember { mutableStateOf(false) }
    var showModelDialog by remember { mutableStateOf(false) }
    var showYearDialog by remember { mutableStateOf(false) }
    var showConsumptionInfoDialog by remember { mutableStateOf(false) }

    val platformOptions = listOf(
        "Uber, 99 e InDrive",
        "Uber e 99",
        "Apenas Uber",
        "Apenas 99",
        "Apenas InDrive",
        "Particular / Corridas Privadas"
    )

    val yearOptions = (2026 downTo 2012).map { it.toString() }

    // Platform Selector Dialog
    if (showPlatformDialog) {
        OptionSelectorDialog(
            title = "Plataforma que utiliza",
            options = platformOptions,
            selected = selectedPlatform,
            onSelect = {
                selectedPlatform = it
                showPlatformDialog = false
            },
            onDismiss = { showPlatformDialog = false }
        )
    }

    // Brand Selector Dialog
    if (showBrandDialog) {
        OptionSelectorDialog(
            title = "Selecione a marca",
            options = VehicleCatalog.brands.map { it.brandName },
            selected = selectedBrand,
            onSelect = { brand ->
                selectedBrand = brand
                val models = VehicleCatalog.brands.find { it.brandName == brand }?.models.orEmpty()
                selectedModel = models.firstOrNull()?.name ?: "Modelo Padrão"
                val defaultPowertrain = models.firstOrNull()?.suggestedPowertrain ?: VehiclePowertrain.COMBUSTAO
                isElectric = defaultPowertrain == VehiclePowertrain.ELETRICO
                consumptionText = if (isElectric) "7.2" else "12.5"
                showBrandDialog = false
            },
            onDismiss = { showBrandDialog = false }
        )
    }

    // Model Selector Dialog
    if (showModelDialog) {
        val availableModels = VehicleCatalog.brands.find { it.brandName == selectedBrand }?.models.orEmpty()
        val modelNames = if (availableModels.isNotEmpty()) availableModels.map { it.name } else listOf("Outro")
        OptionSelectorDialog(
            title = "Selecione o modelo",
            options = modelNames,
            selected = selectedModel,
            onSelect = { model ->
                selectedModel = model
                val found = availableModels.find { it.name == model }
                if (found != null) {
                    isElectric = found.suggestedPowertrain == VehiclePowertrain.ELETRICO
                    consumptionText = if (isElectric) "7.2" else found.averageConsumption.toString()
                }
                showModelDialog = false
            },
            onDismiss = { showModelDialog = false }
        )
    }

    // Year Selector Dialog
    if (showYearDialog) {
        OptionSelectorDialog(
            title = "Selecione o ano de fabricação",
            options = yearOptions,
            selected = selectedYear,
            onSelect = {
                selectedYear = it
                showYearDialog = false
            },
            onDismiss = { showYearDialog = false }
        )
    }

    // Consumption Info Dialog
    if (showConsumptionInfoDialog) {
        AlertDialog(
            onDismissRequest = { showConsumptionInfoDialog = false },
            title = { Text("Consumo do Veículo", color = TextWhite, fontWeight = FontWeight.Bold) },
            text = {
                Text(
                    text = if (isElectric) {
                        "Para veículos 100% elétricos, o consumo pode ser informado em km/kWh (ex: 7.2 km por kWh) ou kWh/100km.\n\nIsso permite calcular com exatidão o custo por km de cada recarga."
                    } else {
                        "Para veículos a combustão ou híbridos flex, informe quantos km seu carro faz em média por litro de gasolina ou etanol na cidade (ex: 12.5 km/L)."
                    },
                    color = TextSecondary,
                    fontSize = 13.5.sp,
                    lineHeight = 20.sp
                )
            },
            confirmButton = {
                Button(
                    onClick = { showConsumptionInfoDialog = false },
                    colors = ButtonDefaults.buttonColors(containerColor = EmeraldGreen)
                ) {
                    Text("OK", color = Color.Black, fontWeight = FontWeight.Bold)
                }
            },
            containerColor = CardDark,
            shape = RoundedCornerShape(18.dp)
        )
    }

    Surface(
        modifier = modifier.fillMaxSize(),
        color = BackgroundDark
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .padding(horizontal = 20.dp, vertical = 14.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                // TOP BAR: Back Arrow + Logo
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    IconButton(
                        onClick = {
                            if (currentStep == 2) {
                                currentStep = 1
                            } else {
                                onNavigateBack()
                            }
                        },
                        modifier = Modifier.size(38.dp)
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Voltar",
                            tint = TextWhite,
                            modifier = Modifier.size(22.dp)
                        )
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(28.dp)
                                .clip(CircleShape)
                                .background(EmeraldGreen.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.ElectricBolt,
                                contentDescription = null,
                                tint = EmeraldGreen,
                                modifier = Modifier.size(18.dp)
                            )
                        }

                        Text(
                            text = appName,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextWhite
                        )
                    }
                }

                // SCREEN HEADER: Title & Subtitle based on Step
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text = if (currentStep == 1) "Vamos começar!" else "Dados do veículo",
                        fontSize = 30.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = TextWhite,
                        letterSpacing = (-0.5).sp
                    )
                    Text(
                        text = if (currentStep == 1) {
                            "Para te ajudar da melhor forma, precisamos de algumas informações sobre você e seu veículo."
                        } else {
                            "Agora, informe os dados do seu veículo para que possamos calcular seus resultados."
                        },
                        fontSize = 14.sp,
                        color = TextSecondary,
                        lineHeight = 20.sp
                    )
                }

                // STEP INDICATOR (Exact match to screenshots 2 & 3)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Step 1 Chip
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.clickable { currentStep = 1 }
                    ) {
                        Box(
                            modifier = Modifier
                                .size(22.dp)
                                .clip(CircleShape)
                                .background(EmeraldGreen),
                            contentAlignment = Alignment.Center
                        ) {
                            if (currentStep == 2) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = null,
                                    tint = Color.Black,
                                    modifier = Modifier.size(14.dp)
                                )
                            } else {
                                Text(
                                    text = "1",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.Black
                                )
                            }
                        }
                        Text(
                            text = "Dados pessoais",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = EmeraldGreen
                        )
                    }

                    // Connector line
                    HorizontalDivider(
                        modifier = Modifier.weight(1f),
                        color = if (currentStep == 2) EmeraldGreen else BorderDark,
                        thickness = 1.5.dp
                    )

                    // Step 2 Chip
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(22.dp)
                                .clip(CircleShape)
                                .background(if (currentStep == 2) EmeraldGreen else SurfaceDark)
                                .border(1.dp, if (currentStep == 2) EmeraldGreen else BorderDark, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "2",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (currentStep == 2) Color.Black else TextSecondary
                            )
                        }
                        Text(
                            text = "Dados do veículo",
                            fontSize = 12.sp,
                            fontWeight = if (currentStep == 2) FontWeight.SemiBold else FontWeight.Normal,
                            color = if (currentStep == 2) EmeraldGreen else TextSecondary
                        )
                    }
                }

                Spacer(modifier = Modifier.height(2.dp))

                // STEP 1 CONTENT: DADOS PESSOAIS
                if (currentStep == 1) {
                    Text(
                        text = "Dados pessoais",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextWhite
                    )

                    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                        // 1. Nome completo
                        WizardInputField(
                            icon = Icons.Outlined.Person,
                            label = "Nome completo",
                            value = fullName,
                            onValueChange = { fullName = it },
                            placeholder = "Ex: João da Silva",
                            capitalization = KeyboardCapitalization.Words,
                            testTag = "input_wizard_driver_name"
                        )

                        // 2. Plataforma que utiliza
                        WizardSelectField(
                            icon = Icons.Outlined.DirectionsCar,
                            label = "Plataforma que utiliza",
                            value = selectedPlatform,
                            onClick = { showPlatformDialog = true },
                            testTag = "select_wizard_platform"
                        )

                        // 3. Telefone (opcional)
                        WizardInputField(
                            icon = Icons.Outlined.Phone,
                            label = "Telefone (opcional)",
                            value = phoneNumber,
                            onValueChange = { phoneNumber = it },
                            placeholder = "(XX) XXXXX-XXXX",
                            keyboardType = KeyboardType.Phone,
                            testTag = "input_wizard_phone"
                        )
                    }
                }

                // STEP 2 CONTENT: DADOS DO VEÍCULO
                if (currentStep == 2) {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        // 1. Marca
                        WizardSelectField(
                            icon = Icons.Outlined.DirectionsCar,
                            label = "Marca",
                            value = selectedBrand,
                            onClick = { showBrandDialog = true },
                            testTag = "select_wizard_brand"
                        )

                        // 2. Modelo
                        WizardSelectField(
                            icon = Icons.Outlined.DirectionsCar,
                            label = "Modelo",
                            value = selectedModel,
                            onClick = { showModelDialog = true },
                            testTag = "select_wizard_model"
                        )

                        // 3. Ano de fabricação
                        WizardSelectField(
                            icon = Icons.Outlined.CalendarMonth,
                            label = "Ano de fabricação",
                            value = selectedYear,
                            onClick = { showYearDialog = true },
                            testTag = "select_wizard_year"
                        )

                        // 4. Tipo de combustível (Radio group)
                        Surface(
                            shape = RoundedCornerShape(14.dp),
                            color = DeepRowDark,
                            border = BorderStroke(1.dp, BorderDark),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 14.dp, vertical = 14.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Outlined.LocalGasStation,
                                        contentDescription = null,
                                        tint = TextSecondary,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Text(
                                        text = "Tipo de combustível",
                                        fontSize = 13.5.sp,
                                        color = TextWhite,
                                        fontWeight = FontWeight.Medium
                                    )
                                }

                                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                    // Gasolina / Etanol
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.clickable {
                                            isElectric = false
                                            consumptionText = "12.5"
                                        }
                                    ) {
                                        RadioButton(
                                            selected = !isElectric,
                                            onClick = {
                                                isElectric = false
                                                consumptionText = "12.5"
                                            },
                                            colors = RadioButtonDefaults.colors(selectedColor = EmeraldGreen)
                                        )
                                        Text("Gasolina / Etanol", fontSize = 11.5.sp, color = TextWhite)
                                    }

                                    // Elétrico
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.clickable {
                                            isElectric = true
                                            consumptionText = "7.2"
                                        }
                                    ) {
                                        RadioButton(
                                            selected = isElectric,
                                            onClick = {
                                                isElectric = true
                                                consumptionText = "7.2"
                                            },
                                            colors = RadioButtonDefaults.colors(selectedColor = EmeraldGreen)
                                        )
                                        Text("Elétrico", fontSize = 11.5.sp, color = TextWhite)
                                    }
                                }
                            }
                        }

                        // 5. Consumo Header with Info icon
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = "Consumo",
                                fontSize = 13.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextWhite
                            )
                            Icon(
                                imageVector = Icons.Outlined.Info,
                                contentDescription = "Informações sobre consumo",
                                tint = TextSecondary,
                                modifier = Modifier
                                    .size(16.dp)
                                    .clickable { showConsumptionInfoDialog = true }
                            )
                        }

                        // Consumo Tabs: [ km/L (combustível) ] [ kWh/100km (elétrico) ]
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .background(DeepRowDark)
                                .border(1.dp, BorderDark, RoundedCornerShape(10.dp))
                                .padding(4.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (!isElectric) EmeraldGreen else Color.Transparent)
                                    .clickable {
                                        isElectric = false
                                        consumptionText = "12.5"
                                    }
                                    .padding(vertical = 8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "km/L (combustível)",
                                    fontSize = 11.5.sp,
                                    fontWeight = if (!isElectric) FontWeight.Bold else FontWeight.Normal,
                                    color = if (!isElectric) Color.Black else TextSecondary
                                )
                            }

                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (isElectric) EmeraldGreen else Color.Transparent)
                                    .clickable {
                                        isElectric = true
                                        consumptionText = "7.2"
                                    }
                                    .padding(vertical = 8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "kWh/100km (elétrico)",
                                    fontSize = 11.5.sp,
                                    fontWeight = if (isElectric) FontWeight.Bold else FontWeight.Normal,
                                    color = if (isElectric) Color.Black else TextSecondary
                                )
                            }
                        }

                        // Consumo Input Field
                        WizardInputField(
                            icon = if (isElectric) Icons.Default.ElectricBolt else Icons.Default.LocalGasStation,
                            label = if (isElectric) "Consumo por kWh" else "Consumo médio (km/L)",
                            value = consumptionText,
                            onValueChange = { consumptionText = it },
                            placeholder = if (isElectric) "Ex: 7,2" else "Ex: 12,5",
                            keyboardType = KeyboardType.Decimal,
                            testTag = "input_wizard_consumption"
                        )

                        // 6. Hodômetro atual (km)
                        WizardInputField(
                            icon = Icons.Outlined.Speed,
                            label = "Hodômetro atual (km)",
                            value = odometerText,
                            onValueChange = { odometerText = it },
                            placeholder = "Ex: 0",
                            keyboardType = KeyboardType.Number,
                            testTag = "input_wizard_odometer"
                        )
                    }
                }
            }

            // BOTTOM ACTION BUTTON: "Próximo →" or "Finalizar cadastro"
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp, bottom = 8.dp)
            ) {
                if (currentStep == 1) {
                    Button(
                        onClick = {
                            if (fullName.isBlank()) {
                                Toast.makeText(context, "Por favor, digite seu nome", Toast.LENGTH_SHORT).show()
                            } else {
                                currentStep = 2
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                            .testTag("btn_wizard_next"),
                        colors = ButtonDefaults.buttonColors(containerColor = EmeraldGreen),
                        shape = RoundedCornerShape(26.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = "Próximo",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.Black
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                contentDescription = null,
                                tint = Color.Black,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                } else {
                    Button(
                        onClick = {
                            val finalModel = "$selectedBrand $selectedModel".trim()
                            val consumption = consumptionText.replace(",", ".").toDoubleOrNull() ?: if (isElectric) 7.2 else 12.5
                            val mileage = odometerText.replace(",", ".").toDoubleOrNull() ?: 0.0
                            val fuelPrice = if (isElectric) 0.85 else 5.89
                            val fuelTypeName = if (isElectric) "Elétrico" else "Flex (Gasolina/Etanol)"

                            onComplete(
                                fullName.ifBlank { "Michel" },
                                finalModel,
                                selectedYear,
                                fuelTypeName,
                                isElectric,
                                consumption,
                                fuelPrice,
                                mileage,
                                6000.0,
                                selectedPlatform,
                                phoneNumber,
                                selectedBrand
                            )
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                            .testTag("btn_wizard_finish"),
                        colors = ButtonDefaults.buttonColors(containerColor = EmeraldGreen),
                        shape = RoundedCornerShape(26.dp)
                    ) {
                        Text(
                            text = "Finalizar cadastro",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.Black
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun WizardInputField(
    icon: ImageVector,
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    keyboardType: KeyboardType = KeyboardType.Text,
    capitalization: KeyboardCapitalization = KeyboardCapitalization.None,
    testTag: String = ""
) {
    Surface(
        shape = RoundedCornerShape(14.dp),
        color = DeepRowDark,
        border = BorderStroke(1.dp, BorderDark),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = TextSecondary,
                modifier = Modifier.size(20.dp)
            )

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = label,
                    fontSize = 11.5.sp,
                    color = TextSecondary,
                    fontWeight = FontWeight.Medium
                )
                BasicTextField(
                    value = value,
                    onValueChange = onValueChange,
                    textStyle = TextStyle(
                        color = TextWhite,
                        fontSize = 14.5.sp,
                        fontWeight = FontWeight.SemiBold
                    ),
                    keyboardOptions = KeyboardOptions(
                        keyboardType = keyboardType,
                        capitalization = capitalization
                    ),
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag(testTag),
                    decorationBox = { innerTextField ->
                        if (value.isEmpty()) {
                            Text(
                                text = placeholder,
                                color = TextSecondary.copy(alpha = 0.5f),
                                fontSize = 14.sp
                            )
                        }
                        innerTextField()
                    }
                )
            }
        }
    }
}

@Composable
private fun WizardSelectField(
    icon: ImageVector,
    label: String,
    value: String,
    onClick: () -> Unit,
    testTag: String = ""
) {
    Surface(
        shape = RoundedCornerShape(14.dp),
        color = DeepRowDark,
        border = BorderStroke(1.dp, BorderDark),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .testTag(testTag)
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
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.weight(1f)
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = TextSecondary,
                    modifier = Modifier.size(20.dp)
                )

                Column {
                    Text(
                        text = label,
                        fontSize = 11.5.sp,
                        color = TextSecondary,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = value.ifBlank { "Selecione" },
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = if (value.isNotBlank()) TextWhite else TextSecondary
                    )
                }
            }

            Icon(
                imageVector = Icons.Default.KeyboardArrowDown,
                contentDescription = "Abrir opções",
                tint = TextSecondary,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

@Composable
private fun OptionSelectorDialog(
    title: String,
    options: List<String>,
    selected: String,
    onSelect: (String) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title, fontWeight = FontWeight.Bold, color = TextWhite) },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                options.forEach { opt ->
                    val isSelected = opt == selected
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = if (isSelected) EmeraldGreen.copy(alpha = 0.2f) else Color.Transparent,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onSelect(opt) }
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 12.dp, vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = opt,
                                fontSize = 14.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                color = if (isSelected) EmeraldGreen else TextWhite
                            )
                            if (isSelected) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = null,
                                    tint = EmeraldGreen,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Fechar", color = TextSecondary)
            }
        },
        containerColor = CardDark,
        shape = RoundedCornerShape(20.dp)
    )
}
