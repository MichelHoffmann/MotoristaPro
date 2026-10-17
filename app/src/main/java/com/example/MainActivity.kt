package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
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
    HOME,
    PLANNING,
    REPORTS,
    EXPENSES,
    SETTINGS,
    VEHICLE,
    NEW_USER
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
    var currentScreen by remember { mutableStateOf(AppScreen.HOME) }
    var showMoreSheet by remember { mutableStateOf(false) }

    // Dialog visibility states
    var showRecordDailyWorkDialog by remember { mutableStateOf(false) }
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

    // Handle back button on sub-screens
    if (currentScreen != AppScreen.HOME) {
        BackHandler {
            currentScreen = AppScreen.HOME
        }
    }

    // If profile is not configured yet, show New User Menu on start
    if (!vehicleProfile.isProfileConfigured || currentScreen == AppScreen.NEW_USER) {
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
            onCancel = if (vehicleProfile.isProfileConfigured) {
                { currentScreen = AppScreen.SETTINGS }
            } else null
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

                // 4. Mais / Meu Veículo / Configurações
                val isMoreScreen = currentScreen == AppScreen.SETTINGS || currentScreen == AppScreen.VEHICLE
                NavigationBarItem(
                    selected = isMoreScreen,
                    onClick = { showMoreSheet = true },
                    icon = {
                        Icon(
                            imageVector = Icons.Filled.MoreHoriz,
                            contentDescription = "Mais",
                            modifier = Modifier.size(22.dp)
                        )
                    },
                    label = {
                        Text(
                            text = "Mais",
                            fontSize = 11.sp,
                            fontWeight = if (isMoreScreen) FontWeight.Bold else FontWeight.Normal
                        )
                    },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = EmeraldGreen,
                        selectedTextColor = EmeraldGreen,
                        indicatorColor = EmeraldGreen.copy(alpha = 0.15f),
                        unselectedIconColor = TextSecondary,
                        unselectedTextColor = TextSecondary
                    ),
                    modifier = Modifier.testTag("nav_more")
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
                    onAddDailyWorkClick = { showRecordDailyWorkDialog = true },
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
                    onUpdateMonthlyGoal = { viewModel.updateMonthlyGoal(it) }
                )
                AppScreen.REPORTS -> ReportsScreen(
                    dailyEarnings = dailyEarnings,
                    expenses = expenses,
                    selectedPeriod = selectedPeriod,
                    onPeriodSelected = { viewModel.setPeriodFilter(it) },
                    chartData = viewModel.getDynamicChartData(dailyEarnings, expenses)
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
                    onResetAllData = { viewModel.resetAllDataToZero() }
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

    // Dialog: Registrar faturamento do dia (valor total ganho, total de corridas, km rodados e etapas de trabalho)
    if (showRecordDailyWorkDialog) {
        RecordDailyWorkDialog(
            initialDate = todayDateStr,
            initialAmount = todayEarningRecord?.totalAmount ?: 0.0,
            initialTrips = todayEarningRecord?.tripsCount ?: 0,
            initialKm = todayEarningRecord?.kmDriven ?: 0.0,
            initialNotes = todayEarningRecord?.notes.orEmpty(),
            initialStartTime = todayEarningRecord?.startTime.orEmpty(),
            initialLunchStartTime = todayEarningRecord?.lunchStartTime.orEmpty(),
            initialLunchEndTime = todayEarningRecord?.lunchEndTime.orEmpty(),
            initialEndTime = todayEarningRecord?.endTime.orEmpty(),
            onDismiss = { showRecordDailyWorkDialog = false },
            onConfirm = { date, amount, trips, km, notes, start, lunchStart, lunchEnd, end ->
                viewModel.recordDailyWork(date, amount, trips, km, notes, start, lunchStart, lunchEnd, end)
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
