package com.example

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.ui.platform.LocalContext
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.components.AddExpenseDialog
import com.example.ui.components.AdjustWorkStagesDialog
import com.example.ui.components.MoreSheet
import com.example.ui.components.RecordDailyWorkDialog
import com.example.ui.screens.*
import com.example.ui.theme.*
import com.example.ui.viewmodel.MainViewModel

enum class AppScreen {
    WELCOME,
    IMPORT_DATA,
    REGISTRATION,
    HOME,
    PLANNING,
    REPORTS,
    EXPENSES,
    SETTINGS,
    VEHICLE,
    NEW_USER,
    MY_DATA
}

class MainActivity : ComponentActivity() {

    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                MainAppScaffold(viewModel = viewModel)
            }
        }
    }
}

@Composable
fun MainAppScaffold(viewModel: MainViewModel) {
    val context = LocalContext.current
    val prefs = remember { context.getSharedPreferences("motorista_pro_prefs", Context.MODE_PRIVATE) }
    val hasLoggedInOnce = remember { prefs.getBoolean("user_has_logged_in_once", false) }

    // Start directly on HOME if user already logged in or profile configured; otherwise start on WELCOME
    var currentScreen by remember {
        mutableStateOf(if (hasLoggedInOnce) AppScreen.HOME else AppScreen.WELCOME)
    }
    var showMoreSheet by remember { mutableStateOf(false) }

    // Dialog visibility states
    var showRecordDailyWorkDialog by remember { mutableStateOf(false) }
    var recordDialogTargetDate by remember { mutableStateOf<String?>(null) }
    var showAdjustWorkStagesDialog by remember { mutableStateOf(false) }
    var showAddExpenseDialog by remember { mutableStateOf(false) }

    // Observe data from ViewModel
    val dailyEarnings by viewModel.dailyEarnings.collectAsStateWithLifecycle()
    val workDays by viewModel.workDays.collectAsStateWithLifecycle()
    val expenses by viewModel.expenses.collectAsStateWithLifecycle()
    val vehicleProfile by viewModel.vehicleProfile.collectAsStateWithLifecycle()
    val vehicleCostItems by viewModel.vehicleCostItems.collectAsStateWithLifecycle()
    val fuelChargeRecords by viewModel.fuelChargeRecords.collectAsStateWithLifecycle()
    val viabilityResult by viewModel.viabilityResult.collectAsStateWithLifecycle()
    val currentYearMonth by viewModel.currentYearMonth.collectAsStateWithLifecycle()

    val selectedPeriod by viewModel.selectedPeriod.collectAsStateWithLifecycle()
    val selectedExpenseTab by viewModel.selectedExpenseTab.collectAsStateWithLifecycle()

    // Shift Stopwatch State
    val shiftState by viewModel.shiftState.collectAsStateWithLifecycle()
    val shiftStartTimeStr by viewModel.shiftStartTimeStr.collectAsStateWithLifecycle()
    val shiftStartTimeMillis by viewModel.shiftStartTimeMillis.collectAsStateWithLifecycle()
    val accumulatedWorkSeconds by viewModel.accumulatedWorkSeconds.collectAsStateWithLifecycle()
    val breakStartTimeMillis by viewModel.breakStartTimeMillis.collectAsStateWithLifecycle()
    val accumulatedBreakSeconds by viewModel.accumulatedBreakSeconds.collectAsStateWithLifecycle()

    // Automatic redirection if user already logged in, has configured profile or has records
    LaunchedEffect(hasLoggedInOnce, vehicleProfile.isProfileConfigured, dailyEarnings.size) {
        if (hasLoggedInOnce || vehicleProfile.isProfileConfigured || dailyEarnings.isNotEmpty()) {
            prefs.edit().putBoolean("user_has_logged_in_once", true).apply()
            if (currentScreen == AppScreen.WELCOME) {
                currentScreen = AppScreen.HOME
            }
        }
    }

    // Handle back button on sub-screens (never return to Welcome if user already logged in once)
    if (currentScreen != AppScreen.HOME) {
        BackHandler {
            currentScreen = when (currentScreen) {
                AppScreen.IMPORT_DATA, AppScreen.REGISTRATION -> {
                    if (hasLoggedInOnce || vehicleProfile.isProfileConfigured || dailyEarnings.isNotEmpty()) {
                        AppScreen.HOME
                    } else {
                        AppScreen.WELCOME
                    }
                }
                else -> AppScreen.HOME
            }
        }
    }

    // 1. Initial Screen: Welcome Screen (Only accessible before first login)
    if (currentScreen == AppScreen.WELCOME) {
        if (hasLoggedInOnce || vehicleProfile.isProfileConfigured || dailyEarnings.isNotEmpty()) {
            currentScreen = AppScreen.HOME
            return
        }
        WelcomeScreen(
            appName = "Motorista Pro",
            onExistingUserClick = {
                currentScreen = AppScreen.IMPORT_DATA
            },
            onNewUserClick = {
                currentScreen = AppScreen.REGISTRATION
            }
        )
        return
    }

    // 2. Import Data Screen ("Já tem cadastro? Importar arquivo CSV")
    if (currentScreen == AppScreen.IMPORT_DATA) {
        ImportDataScreen(
            appName = "Motorista Pro",
            onNavigateBack = {
                currentScreen = if (hasLoggedInOnce || vehicleProfile.isProfileConfigured || dailyEarnings.isNotEmpty()) AppScreen.HOME else AppScreen.WELCOME
            },
            onImportSuccess = {
                prefs.edit().putBoolean("user_has_logged_in_once", true).apply()
                currentScreen = AppScreen.HOME
            },
            onContinueWithoutImport = {
                prefs.edit().putBoolean("user_has_logged_in_once", true).apply()
                if (!vehicleProfile.isProfileConfigured) {
                    currentScreen = AppScreen.REGISTRATION
                } else {
                    currentScreen = AppScreen.HOME
                }
            },
            onImportCsvData = { items ->
                items.forEach { (date, amt) ->
                    viewModel.recordDailyWork(
                        date = date,
                        totalAmount = amt,
                        tripsCount = (amt / 18.0).toInt().coerceAtLeast(1),
                        kmDriven = (amt * 0.45),
                        notes = "Importado via CSV",
                        startTime = "08:00",
                        lunchStartTime = "12:00",
                        lunchEndTime = "13:00",
                        endTime = "18:00",
                        fuelOrChargeCost = 0.0,
                        isElectricCharge = vehicleProfile.isElectric
                    )
                }
            }
        )
        return
    }

    // 3. New User Registration Wizard ("Vamos começar!" -> "Dados do veículo")
    if (currentScreen == AppScreen.REGISTRATION) {
        RegistrationWizardScreen(
            appName = "Motorista Pro",
            currentProfile = vehicleProfile,
            onNavigateBack = {
                currentScreen = if (hasLoggedInOnce || vehicleProfile.isProfileConfigured || dailyEarnings.isNotEmpty()) AppScreen.HOME else AppScreen.WELCOME
            },
            onComplete = { name, model, year, fuelType, isElectric, consumption, price, mileage, monthlyGoal, platform, phone, brand, fullRange ->
                prefs.edit().putBoolean("user_has_logged_in_once", true).apply()
                viewModel.saveUserProfile(
                    name = name,
                    model = model,
                    year = year,
                    fuelType = fuelType,
                    isElectric = isElectric,
                    avgConsumption = consumption,
                    fuelOrEnergyPrice = price,
                    currentMileage = mileage,
                    monthlyGoal = monthlyGoal,
                    platform = platform,
                    phoneNumber = phone,
                    vehicleBrand = brand,
                    fullRangeKm = fullRange
                )
                currentScreen = AppScreen.HOME
            }
        )
        return
    }

    // 4. Vehicle Registration Screen (from Settings/Profile)
    if (currentScreen == AppScreen.NEW_USER) {
        NewUserScreen(
            currentProfile = vehicleProfile,
            onSaveProfile = { name, model, year, fuelType, isElectric, consumption, price, mileage, monthlyGoal ->
                viewModel.saveUserProfile(
                    name = name,
                    model = model,
                    year = year,
                    fuelType = fuelType,
                    isElectric = isElectric,
                    avgConsumption = consumption,
                    fuelOrEnergyPrice = price,
                    currentMileage = mileage,
                    monthlyGoal = monthlyGoal
                )
                currentScreen = AppScreen.HOME
            },
            onCancel = {
                currentScreen = if (vehicleProfile.isProfileConfigured) AppScreen.SETTINGS else AppScreen.WELCOME
            }
        )
        return
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        contentWindowInsets = WindowInsets.safeDrawing,
        containerColor = BackgroundDark,
        bottomBar = {
            NavigationBar(
                containerColor = SurfaceDark,
                contentColor = TextSecondary,
                tonalElevation = 8.dp
            ) {
                // 1. Início
                NavigationBarItem(
                    selected = currentScreen == AppScreen.HOME,
                    onClick = { currentScreen = AppScreen.HOME },
                    icon = {
                        Icon(
                            imageVector = if (currentScreen == AppScreen.HOME) Icons.Filled.Home else Icons.Outlined.Home,
                            contentDescription = "Início",
                            modifier = Modifier.size(22.dp)
                        )
                    },
                    label = {
                        Text(
                            text = "Início",
                            fontSize = 11.sp,
                            fontWeight = if (currentScreen == AppScreen.HOME) FontWeight.Bold else FontWeight.Normal
                        )
                    },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = EmeraldGreen,
                        selectedTextColor = EmeraldGreen,
                        indicatorColor = EmeraldGreen.copy(alpha = 0.15f),
                        unselectedIconColor = TextSecondary,
                        unselectedTextColor = TextSecondary
                    ),
                    modifier = Modifier.testTag("nav_home")
                )

                // 2. Planejamento (Calendário, Metas & Feriados)
                NavigationBarItem(
                    selected = currentScreen == AppScreen.PLANNING,
                    onClick = { currentScreen = AppScreen.PLANNING },
                    icon = {
                        Icon(
                            imageVector = if (currentScreen == AppScreen.PLANNING) Icons.Filled.CalendarMonth else Icons.Outlined.CalendarMonth,
                            contentDescription = "Planejamento",
                            modifier = Modifier.size(22.dp)
                        )
                    },
                    label = {
                        Text(
                            text = "Planejamento",
                            fontSize = 11.sp,
                            fontWeight = if (currentScreen == AppScreen.PLANNING) FontWeight.Bold else FontWeight.Normal
                        )
                    },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = EmeraldGreen,
                        selectedTextColor = EmeraldGreen,
                        indicatorColor = EmeraldGreen.copy(alpha = 0.15f),
                        unselectedIconColor = TextSecondary,
                        unselectedTextColor = TextSecondary
                    ),
                    modifier = Modifier.testTag("nav_planning")
                )

                // 3. Relatórios
                NavigationBarItem(
                    selected = currentScreen == AppScreen.REPORTS,
                    onClick = { currentScreen = AppScreen.REPORTS },
                    icon = {
                        Icon(
                            imageVector = if (currentScreen == AppScreen.REPORTS) Icons.Filled.Analytics else Icons.Outlined.Analytics,
                            contentDescription = "Relatórios",
                            modifier = Modifier.size(22.dp)
                        )
                    },
                    label = {
                        Text(
                            text = "Relatórios",
                            fontSize = 11.sp,
                            fontWeight = if (currentScreen == AppScreen.REPORTS) FontWeight.Bold else FontWeight.Normal
                        )
                    },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = EmeraldGreen,
                        selectedTextColor = EmeraldGreen,
                        indicatorColor = EmeraldGreen.copy(alpha = 0.15f),
                        unselectedIconColor = TextSecondary,
                        unselectedTextColor = TextSecondary
                    ),
                    modifier = Modifier.testTag("nav_reports")
                )

                // 4. Meus Dados (Informações cadastradas & Exportar CSV)
                NavigationBarItem(
                    selected = currentScreen == AppScreen.MY_DATA,
                    onClick = { currentScreen = AppScreen.MY_DATA },
                    icon = {
                        Icon(
                            imageVector = if (currentScreen == AppScreen.MY_DATA) Icons.Filled.Person else Icons.Outlined.Person,
                            contentDescription = "Meus Dados",
                            modifier = Modifier.size(22.dp)
                        )
                    },
                    label = {
                        Text(
                            text = "Meus Dados",
                            fontSize = 11.sp,
                            fontWeight = if (currentScreen == AppScreen.MY_DATA) FontWeight.Bold else FontWeight.Normal
                        )
                    },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = EmeraldGreen,
                        selectedTextColor = EmeraldGreen,
                        indicatorColor = EmeraldGreen.copy(alpha = 0.15f),
                        unselectedIconColor = TextSecondary,
                        unselectedTextColor = TextSecondary
                    ),
                    modifier = Modifier.testTag("nav_my_data")
                )
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (currentScreen) {
                AppScreen.HOME -> HomeScreen(
                    dailyEarnings = dailyEarnings,
                    expenses = expenses,
                    fuelChargeRecords = fuelChargeRecords,
                    workDays = workDays,
                    vehicleProfile = vehicleProfile,
                    viabilityResult = viabilityResult,
                    shiftState = shiftState,
                    shiftStartTimeStr = shiftStartTimeStr,
                    shiftStartTimeMillis = shiftStartTimeMillis,
                    accumulatedWorkSeconds = accumulatedWorkSeconds,
                    breakStartTimeMillis = breakStartTimeMillis,
                    accumulatedBreakSeconds = accumulatedBreakSeconds,
                    onStartShift = { viewModel.startWorkShift() },
                    onStartBreak = { viewModel.startBreak() },
                    onResumeShift = { viewModel.resumeWorkShift() },
                    onFinishShift = {
                        viewModel.finishWorkShift()
                        recordDialogTargetDate = MainViewModel.getTodayDateString()
                        showRecordDailyWorkDialog = true
                    },
                    onAddDailyWorkClick = {
                        recordDialogTargetDate = MainViewModel.getTodayDateString()
                        showRecordDailyWorkDialog = true
                    },
                    onAddPastDailyWorkClick = {
                        val cal = java.util.Calendar.getInstance()
                        cal.add(java.util.Calendar.DAY_OF_YEAR, -1)
                        val sdf = java.text.SimpleDateFormat("dd/MM/yyyy", java.util.Locale.getDefault())
                        recordDialogTargetDate = sdf.format(cal.time)
                        showRecordDailyWorkDialog = true
                    },
                    onNavigateToPlanning = { currentScreen = AppScreen.PLANNING },
                    onNavigateToVehicle = { currentScreen = AppScreen.VEHICLE },
                    onOpenProfileMenu = { currentScreen = AppScreen.NEW_USER },
                    onRecordStageAction = { viewModel.recordWorkStageAction(it) },
                    onAdjustStagesClick = { showAdjustWorkStagesDialog = true }
                )
                AppScreen.PLANNING -> PlanningScreen(
                    currentYearMonth = currentYearMonth,
                    workDays = workDays,
                    dailyEarnings = dailyEarnings,
                    expenses = expenses,
                    vehicleProfile = vehicleProfile,
                    onNextMonth = { viewModel.nextMonth() },
                    onPreviousMonth = { viewModel.previousMonth() },
                    onToggleWorkDay = { viewModel.toggleWorkDay(it) },
                    onApplySchedule = { viewModel.applyQuickSchedule(it) },
                    onUpdateMonthlyGoal = { viewModel.updateMonthlyGoal(it) },
                    onAddEarningsForDate = { date ->
                        recordDialogTargetDate = date.ifBlank { null }
                        showRecordDailyWorkDialog = true
                    }
                )
                AppScreen.REPORTS -> ReportsScreen(
                    dailyEarnings = dailyEarnings,
                    expenses = expenses,
                    selectedPeriod = selectedPeriod,
                    onPeriodSelected = { viewModel.setPeriodFilter(it) },
                    chartData = viewModel.getDynamicChartData(dailyEarnings, expenses),
                    onAddEarningsForDate = { date ->
                        recordDialogTargetDate = date.ifBlank { null }
                        showRecordDailyWorkDialog = true
                    }
                )
                AppScreen.EXPENSES -> ExpensesScreen(
                    expenses = expenses,
                    selectedFilter = selectedExpenseTab,
                    onFilterSelected = { viewModel.setExpenseTabFilter(it) },
                    onAddExpenseClick = { showAddExpenseDialog = true },
                    onDeleteExpense = { viewModel.deleteExpense(it) }
                )
                AppScreen.SETTINGS -> SettingsScreen(
                    vehicleProfile = vehicleProfile,
                    onOpenNewUserMenu = { currentScreen = AppScreen.NEW_USER },
                    onNavigateToVehicle = { currentScreen = AppScreen.VEHICLE },
                    onResetAllData = {
                        prefs.edit().putBoolean("user_has_logged_in_once", false).apply()
                        viewModel.resetAllDataToZero()
                        currentScreen = AppScreen.WELCOME
                    }
                )
                AppScreen.VEHICLE -> VehicleSectionScreen(
                    vehicleProfile = vehicleProfile,
                    vehicleCostItems = vehicleCostItems,
                    fuelChargeRecords = fuelChargeRecords,
                    viabilityResult = viabilityResult,
                    onUpdateProfile = { viewModel.updateFullVehicleProfile(it) },
                    onAddCostItem = { viewModel.addVehicleCostItem(it) },
                    onDeleteCostItem = { viewModel.deleteVehicleCostItem(it) },
                    onAddFuelChargeRecord = { viewModel.addFuelChargeRecord(it) },
                    onDeleteFuelChargeRecord = { viewModel.deleteFuelChargeRecord(it) },
                    onNavigateBack = { currentScreen = AppScreen.HOME }
                )
                AppScreen.MY_DATA -> MyDataScreen(
                    vehicleProfile = vehicleProfile,
                    dailyEarnings = dailyEarnings,
                    expenses = expenses,
                    vehicleCostItems = vehicleCostItems,
                    workDays = workDays,
                    viabilityResult = viabilityResult,
                    shiftState = shiftState,
                    shiftStartTimeStr = shiftStartTimeStr,
                    onGenerateFreshCsv = { viewModel.generateLatestCsv() },
                    onUpdateProfile = { viewModel.updateFullVehicleProfile(it) },
                    onResetAllData = {
                        prefs.edit().putBoolean("user_has_logged_in_once", false).apply()
                        viewModel.resetAllDataToZero()
                        currentScreen = AppScreen.WELCOME
                    }
                )
                AppScreen.WELCOME -> {
                    // Handled above before Scaffold
                }
                AppScreen.IMPORT_DATA -> {
                    // Handled above before Scaffold
                }
                AppScreen.REGISTRATION -> {
                    // Handled above before Scaffold
                }
                AppScreen.NEW_USER -> {
                    // Handled above before Scaffold
                }
            }
        }
    }

    // Modal Bottom Sheet: Mais
    if (showMoreSheet) {
        MoreSheet(
            onDismiss = { showMoreSheet = false },
            onNavigateToVehicle = { currentScreen = AppScreen.VEHICLE },
            onNavigateToPlanning = { currentScreen = AppScreen.PLANNING },
            onNavigateToExpenses = { currentScreen = AppScreen.EXPENSES },
            onNavigateToSettings = { currentScreen = AppScreen.SETTINGS }
        )
    }

    val todayDateStr = MainViewModel.getTodayDateString()
    val todayEarningRecord = dailyEarnings.find { it.dateStr == todayDateStr }
    val targetRecordDate = recordDialogTargetDate ?: todayDateStr
    val targetEarningRecord = dailyEarnings.find { it.dateStr == targetRecordDate }

    val targetRecordOtherExpenses = expenses.filter {
        it.dateStr == targetRecordDate && it.category != "Combustível" && it.category != "Recarga Elétrica"
    }.sumOf { it.amount }

    // Dialog: Registrar faturamento do dia (valor total ganho, total de corridas, km rodados, combustível/recarga, outros gastos e etapas)
    if (showRecordDailyWorkDialog) {
        RecordDailyWorkDialog(
            initialDate = targetRecordDate,
            initialAmount = targetEarningRecord?.totalAmount ?: 0.0,
            initialTrips = targetEarningRecord?.tripsCount ?: 0,
            initialKm = targetEarningRecord?.kmDriven ?: 0.0,
            initialNotes = targetEarningRecord?.notes.orEmpty(),
            initialStartTime = targetEarningRecord?.startTime.orEmpty(),
            initialLunchStartTime = targetEarningRecord?.lunchStartTime.orEmpty(),
            initialLunchEndTime = targetEarningRecord?.lunchEndTime.orEmpty(),
            initialEndTime = targetEarningRecord?.endTime.orEmpty(),
            initialFuelCost = targetEarningRecord?.fuelOrChargeCost ?: 0.0,
            initialIsElectric = targetEarningRecord?.isElectricCharge ?: vehicleProfile.isElectric,
            initialOtherExpenses = targetRecordOtherExpenses,
            existingEarnings = dailyEarnings,
            onDismiss = { showRecordDailyWorkDialog = false },
            onConfirm = { date, amount, trips, km, notes, start, lunchStart, lunchEnd, end, fuelCost, isElectric, otherExp ->
                viewModel.recordDailyWork(date, amount, trips, km, notes, start, lunchStart, lunchEnd, end, fuelCost, isElectric)
                if (otherExp > 0 && targetRecordOtherExpenses == 0.0) {
                    viewModel.addExpense(
                        category = "Outros / Alimentação",
                        amount = otherExp,
                        description = "Alimentação / gastos do dia",
                        date = date
                    )
                }
                showRecordDailyWorkDialog = false
            }
        )
    }

    // Dialog: Ajustar etapas da jornada de trabalho (mínimo de duas etapas para cálculo de horas)
    if (showAdjustWorkStagesDialog) {
        AdjustWorkStagesDialog(
            initialDate = todayDateStr,
            initialStartTime = todayEarningRecord?.startTime.orEmpty(),
            initialLunchStartTime = todayEarningRecord?.lunchStartTime.orEmpty(),
            initialLunchEndTime = todayEarningRecord?.lunchEndTime.orEmpty(),
            initialEndTime = todayEarningRecord?.endTime.orEmpty(),
            onDismiss = { showAdjustWorkStagesDialog = false },
            onConfirm = { date, start, lunchStart, lunchEnd, end ->
                viewModel.updateWorkStages(date, start, lunchStart, lunchEnd, end)
            }
        )
    }

    // Dialog: Registrar Despesa
    if (showAddExpenseDialog) {
        AddExpenseDialog(
            onDismiss = { showAddExpenseDialog = false },
            onConfirm = { category, amount, desc, date ->
                viewModel.addExpense(category, amount, desc, date)
            }
        )
    }
}
