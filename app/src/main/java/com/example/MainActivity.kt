package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.components.*
import com.example.ui.screens.*
import com.example.ui.theme.*
import com.example.ui.viewmodel.MainViewModel

enum class AppScreen {
    HOME,
    REPORTS,
    TRIPS,
    FUEL,
    EXPENSES,
    MAINTENANCE,
    SETTINGS,
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
    var showAddTripDialog by remember { mutableStateOf(false) }
    var showAddExpenseDialog by remember { mutableStateOf(false) }
    var showAddFuelDialog by remember { mutableStateOf(false) }
    var showAddMaintenanceDialog by remember { mutableStateOf(false) }

    // Observe data from ViewModel
    val trips by viewModel.trips.collectAsStateWithLifecycle()
    val expenses by viewModel.expenses.collectAsStateWithLifecycle()
    val fuelLogs by viewModel.fuelLogs.collectAsStateWithLifecycle()
    val maintenance by viewModel.maintenance.collectAsStateWithLifecycle()
    val vehicleProfile by viewModel.vehicleProfile.collectAsStateWithLifecycle()

    val selectedPeriod by viewModel.selectedPeriod.collectAsStateWithLifecycle()
    val selectedTripTab by viewModel.selectedTripTab.collectAsStateWithLifecycle()
    val selectedFuelTab by viewModel.selectedFuelTab.collectAsStateWithLifecycle()
    val selectedMaintenanceTab by viewModel.selectedMaintenanceTab.collectAsStateWithLifecycle()
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
            onSaveProfile = { name, model, year, fuelType, isElectric, consumption, price, mileage, dailyGoal ->
                viewModel.saveUserProfile(
                    name = name,
                    model = model,
                    year = year,
                    fuelType = fuelType,
                    isElectric = isElectric,
                    avgConsumption = consumption,
                    fuelOrEnergyPrice = price,
                    currentMileage = mileage,
                    dailyGoal = dailyGoal
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

                // 2. Relatórios
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

                // 3. Corridas
                NavigationBarItem(
                    selected = currentScreen == AppScreen.TRIPS,
                    onClick = { currentScreen = AppScreen.TRIPS },
                    icon = {
                        Icon(
                            imageVector = if (currentScreen == AppScreen.TRIPS) Icons.Filled.DirectionsCar else Icons.Outlined.DirectionsCar,
                            contentDescription = "Corridas",
                            modifier = Modifier.size(22.dp)
                        )
                    },
                    label = {
                        Text(
                            text = "Corridas",
                            fontSize = 11.sp,
                            fontWeight = if (currentScreen == AppScreen.TRIPS) FontWeight.Bold else FontWeight.Normal
                        )
                    },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = EmeraldGreen,
                        selectedTextColor = EmeraldGreen,
                        indicatorColor = EmeraldGreen.copy(alpha = 0.15f),
                        unselectedIconColor = TextSecondary,
                        unselectedTextColor = TextSecondary
                    ),
                    modifier = Modifier.testTag("nav_trips")
                )

                // 4. Combustível / Energia
                NavigationBarItem(
                    selected = currentScreen == AppScreen.FUEL,
                    onClick = { currentScreen = AppScreen.FUEL },
                    icon = {
                        Icon(
                            imageVector = if (currentScreen == AppScreen.FUEL) {
                                if (vehicleProfile.isElectric) Icons.Filled.ElectricBolt else Icons.Filled.LocalGasStation
                            } else {
                                if (vehicleProfile.isElectric) Icons.Outlined.ElectricBolt else Icons.Outlined.LocalGasStation
                            },
                            contentDescription = if (vehicleProfile.isElectric) "Energia" else "Combustível",
                            modifier = Modifier.size(22.dp)
                        )
                    },
                    label = {
                        Text(
                            text = if (vehicleProfile.isElectric) "Energia" else "Combustível",
                            fontSize = 11.sp,
                            fontWeight = if (currentScreen == AppScreen.FUEL) FontWeight.Bold else FontWeight.Normal
                        )
                    },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = EmeraldGreen,
                        selectedTextColor = EmeraldGreen,
                        indicatorColor = EmeraldGreen.copy(alpha = 0.15f),
                        unselectedIconColor = TextSecondary,
                        unselectedTextColor = TextSecondary
                    ),
                    modifier = Modifier.testTag("nav_fuel")
                )

                // 5. Mais
                val isMoreScreen = currentScreen in listOf(AppScreen.EXPENSES, AppScreen.MAINTENANCE, AppScreen.SETTINGS)
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
                    trips = trips,
                    expenses = expenses,
                    vehicleProfile = vehicleProfile,
                    onAddTripClick = { showAddTripDialog = true },
                    onAddExpenseClick = { showAddExpenseDialog = true },
                    onNavigateToTrips = { currentScreen = AppScreen.TRIPS },
                    onNavigateToExpenses = { currentScreen = AppScreen.EXPENSES },
                    onOpenProfileMenu = { currentScreen = AppScreen.NEW_USER }
                )
                AppScreen.REPORTS -> ReportsScreen(
                    trips = trips,
                    expenses = expenses,
                    selectedPeriod = selectedPeriod,
                    onPeriodSelected = { viewModel.setPeriodFilter(it) },
                    chartData = viewModel.getDynamicChartData(trips, expenses)
                )
                AppScreen.TRIPS -> TripsScreen(
                    trips = trips,
                    selectedFilter = selectedTripTab,
                    onFilterSelected = { viewModel.setTripTabFilter(it) },
                    onAddTripClick = { showAddTripDialog = true },
                    onDeleteTrip = { viewModel.deleteTrip(it) }
                )
                AppScreen.FUEL -> FuelScreen(
                    fuelLogs = fuelLogs,
                    vehicleProfile = vehicleProfile,
                    selectedFilter = selectedFuelTab,
                    onFilterSelected = { viewModel.setFuelTabFilter(it) },
                    onAddFuelClick = { showAddFuelDialog = true },
                    onDeleteFuelLog = { viewModel.deleteFuelLog(it) }
                )
                AppScreen.EXPENSES -> ExpensesScreen(
                    expenses = expenses,
                    selectedFilter = selectedExpenseTab,
                    onFilterSelected = { viewModel.setExpenseTabFilter(it) },
                    onAddExpenseClick = { showAddExpenseDialog = true },
                    onDeleteExpense = { viewModel.deleteExpense(it) }
                )
                AppScreen.MAINTENANCE -> MaintenanceScreen(
                    items = maintenance,
                    selectedFilter = selectedMaintenanceTab,
                    onFilterSelected = { viewModel.setMaintenanceTabFilter(it) },
                    onAddMaintenanceClick = { showAddMaintenanceDialog = true },
                    onToggleCompleted = { viewModel.toggleMaintenanceStatus(it) }
                )
                AppScreen.SETTINGS -> SettingsScreen(
                    vehicleProfile = vehicleProfile,
                    onOpenNewUserMenu = { currentScreen = AppScreen.NEW_USER },
                    onResetAllData = { viewModel.resetAllDataToZero() }
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
            onNavigateToExpenses = { currentScreen = AppScreen.EXPENSES },
            onNavigateToMaintenance = { currentScreen = AppScreen.MAINTENANCE },
            onNavigateToSettings = { currentScreen = AppScreen.SETTINGS }
        )
    }

    // Dialogs
    if (showAddTripDialog) {
        AddTripDialog(
            onDismiss = { showAddTripDialog = false },
            onConfirm = { platform, amt, dist, dur, time, date ->
                viewModel.addTrip(platform, amt, dist, dur, time, date)
            }
        )
    }

    if (showAddExpenseDialog) {
        AddExpenseDialog(
            onDismiss = { showAddExpenseDialog = false },
            onConfirm = { category, amount, desc, date ->
                viewModel.addExpense(category, amount, desc, date)
            }
        )
    }

    if (showAddFuelDialog) {
        AddFuelDialog(
            onDismiss = { showAddFuelDialog = false },
            onConfirm = { date, liters, total, type, odo, station ->
                viewModel.addFuelLog(date, liters, total, type, odo, station)
            }
        )
    }

    if (showAddMaintenanceDialog) {
        AddExpenseDialog(
            onDismiss = { showAddMaintenanceDialog = false },
            onConfirm = { category, amount, desc, date ->
                viewModel.addMaintenance(
                    title = desc,
                    cost = amount,
                    date = date,
                    alertKm = 5000,
                    alertDays = 30,
                    isCompleted = true,
                    notes = category
                )
            }
        )
    }
}
