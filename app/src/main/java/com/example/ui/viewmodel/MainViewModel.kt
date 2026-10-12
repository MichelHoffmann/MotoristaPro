package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.AppDatabase
import com.example.data.AppRepository
import com.example.model.Expense
import com.example.model.FuelLog
import com.example.model.MaintenanceItem
import com.example.model.Trip
import com.example.model.VehicleProfile
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

enum class PeriodFilter { DIARIO, SEMANAL, MENSAL, ANUAL }
enum class TripTabFilter { HISTORICO, POR_APP, POR_PERIODO }
enum class FuelTabFilter { REGISTROS, CONSUMO, ESTATISTICAS }
enum class MaintenanceTabFilter { SERVICOS, LEMBRETES, HISTORICO }
enum class ExpenseTabFilter { TODAS, CATEGORIAS, COMPROVANTES }

data class ChartBarPoint(
    val label: String,
    val earnings: Float,
    val expenses: Float
)

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: AppRepository

    init {
        val database = AppDatabase.getInstance(application)
        repository = AppRepository(database.appDao())
        viewModelScope.launch {
            repository.seedInitialDataIfEmpty()
        }
    }

    val trips: StateFlow<List<Trip>> = repository.trips
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val expenses: StateFlow<List<Expense>> = repository.expenses
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val fuelLogs: StateFlow<List<FuelLog>> = repository.fuelLogs
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val maintenance: StateFlow<List<MaintenanceItem>> = repository.maintenance
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val vehicleProfile: StateFlow<VehicleProfile> = repository.vehicleProfile
        .map { it ?: VehicleProfile() }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), VehicleProfile())

    // UI Tab Filters
    private val _selectedPeriod = MutableStateFlow(PeriodFilter.DIARIO)
    val selectedPeriod: StateFlow<PeriodFilter> = _selectedPeriod.asStateFlow()

    private val _selectedTripTab = MutableStateFlow(TripTabFilter.HISTORICO)
    val selectedTripTab: StateFlow<TripTabFilter> = _selectedTripTab.asStateFlow()

    private val _selectedFuelTab = MutableStateFlow(FuelTabFilter.REGISTROS)
    val selectedFuelTab: StateFlow<FuelTabFilter> = _selectedFuelTab.asStateFlow()

    private val _selectedMaintenanceTab = MutableStateFlow(MaintenanceTabFilter.SERVICOS)
    val selectedMaintenanceTab: StateFlow<MaintenanceTabFilter> = _selectedMaintenanceTab.asStateFlow()

    private val _selectedExpenseTab = MutableStateFlow(ExpenseTabFilter.TODAS)
    val selectedExpenseTab: StateFlow<ExpenseTabFilter> = _selectedExpenseTab.asStateFlow()

    fun setPeriodFilter(filter: PeriodFilter) { _selectedPeriod.value = filter }
    fun setTripTabFilter(filter: TripTabFilter) { _selectedTripTab.value = filter }
    fun setFuelTabFilter(filter: FuelTabFilter) { _selectedFuelTab.value = filter }
    fun setMaintenanceTabFilter(filter: MaintenanceTabFilter) { _selectedMaintenanceTab.value = filter }
    fun setExpenseTabFilter(filter: ExpenseTabFilter) { _selectedExpenseTab.value = filter }

    // Dynamic Chart Data based on actual data
    fun getDynamicChartData(tripsList: List<Trip>, expensesList: List<Expense>): List<ChartBarPoint> {
        if (tripsList.isEmpty() && expensesList.isEmpty()) {
            return listOf(
                ChartBarPoint("Seg", 0f, 0f),
                ChartBarPoint("Ter", 0f, 0f),
                ChartBarPoint("Qua", 0f, 0f),
                ChartBarPoint("Qui", 0f, 0f),
                ChartBarPoint("Sex", 0f, 0f),
                ChartBarPoint("Sáb", 0f, 0f),
                ChartBarPoint("Dom", 0f, 0f)
            )
        }

        // Group by date or day of week
        val dates = (tripsList.map { it.dateStr } + expensesList.map { it.dateStr }).distinct().takeLast(6)
        if (dates.isEmpty()) {
            return emptyList()
        }
        return dates.map { date ->
            val earn = tripsList.filter { it.dateStr == date }.sumOf { it.netAmount }.toFloat()
            val exp = expensesList.filter { it.dateStr == date }.sumOf { it.amount }.toFloat()
            val shortDate = if (date.length >= 5) date.substring(0, 5) else date
            ChartBarPoint(shortDate, earn, exp)
        }
    }

    // Save initial or updated profile from New User Menu
    fun saveUserProfile(
        name: String,
        model: String,
        year: String,
        fuelType: String,
        isElectric: Boolean,
        avgConsumption: Double,
        fuelOrEnergyPrice: Double,
        currentMileage: Double,
        dailyGoal: Double
    ) {
        viewModelScope.launch {
            repository.updateVehicleProfile(
                VehicleProfile(
                    id = 1,
                    driverName = name,
                    modelName = model,
                    manufacturingYear = year,
                    fuelType = fuelType,
                    isElectric = isElectric,
                    averageConsumption = avgConsumption,
                    fuelOrEnergyPrice = fuelOrEnergyPrice,
                    currentMileage = currentMileage,
                    dailyGoal = if (dailyGoal > 0) dailyGoal else 250.0,
                    weeklyGoal = (if (dailyGoal > 0) dailyGoal else 250.0) * 6,
                    isProfileConfigured = true
                )
            )
        }
    }

    fun resetAllDataToZero() {
        viewModelScope.launch {
            repository.resetAllDataToZero()
        }
    }

    // Actions
    fun addTrip(
        platform: String,
        amount: Double,
        distanceKm: Double,
        durationMin: Int,
        time: String,
        date: String
    ) {
        viewModelScope.launch {
            val fee = amount * 0.20 // Taxa estimada
            val effectiveDate = if (date.isBlank()) getTodayDateString() else date
            val effectiveTime = if (time.isBlank()) getCurrentTimeString() else time
            repository.addTrip(
                Trip(
                    platform = platform,
                    netAmount = amount,
                    grossAmount = amount + fee,
                    fee = fee,
                    distanceKm = distanceKm,
                    durationMinutes = durationMin,
                    timeStr = effectiveTime,
                    dateStr = effectiveDate
                )
            )
            // Update odometer
            val currentProf = vehicleProfile.value
            if (distanceKm > 0) {
                repository.updateVehicleProfile(
                    currentProf.copy(currentMileage = currentProf.currentMileage + distanceKm)
                )
            }
        }
    }

    fun deleteTrip(trip: Trip) {
        viewModelScope.launch { repository.deleteTrip(trip) }
    }

    fun addExpense(category: String, amount: Double, description: String, date: String) {
        viewModelScope.launch {
            val effectiveDate = if (date.isBlank()) getTodayDateString() else date
            repository.addExpense(
                Expense(
                    category = category,
                    amount = amount,
                    description = description,
                    dateStr = effectiveDate
                )
            )
        }
    }

    fun deleteExpense(expense: Expense) {
        viewModelScope.launch { repository.deleteExpense(expense) }
    }

    fun addFuelLog(
        date: String,
        litersOrKwh: Double,
        totalAmount: Double,
        fuelType: String,
        odometer: Double,
        station: String
    ) {
        viewModelScope.launch {
            val pricePerUnit = if (litersOrKwh > 0) totalAmount / litersOrKwh else 0.0
            val effectiveDate = if (date.isBlank()) getTodayDateString() else date
            val currentProf = vehicleProfile.value
            val avg = if (currentProf.averageConsumption > 0) currentProf.averageConsumption else 10.0

            repository.addFuelLog(
                FuelLog(
                    dateStr = effectiveDate,
                    liters = litersOrKwh,
                    totalAmount = totalAmount,
                    pricePerLiter = pricePerUnit,
                    fuelType = fuelType,
                    odometerKm = odometer,
                    averageKmPerLiter = avg,
                    stationName = station.ifBlank { if (fuelType == "Elétrico") "Ponto de Recarga" else "Posto de Combustível" }
                )
            )
            // Also register as expense
            repository.addExpense(
                Expense(
                    category = "Combustível",
                    amount = totalAmount,
                    description = "$fuelType em $station ($litersOrKwh ${if (fuelType == "Elétrico") "kWh" else "L"})",
                    dateStr = effectiveDate
                )
            )
            if (odometer > currentProf.currentMileage) {
                repository.updateVehicleProfile(currentProf.copy(currentMileage = odometer))
            }
        }
    }

    fun deleteFuelLog(fuelLog: FuelLog) {
        viewModelScope.launch { repository.deleteFuelLog(fuelLog) }
    }

    fun addMaintenance(
        title: String,
        cost: Double,
        date: String,
        alertKm: Int,
        alertDays: Int,
        isCompleted: Boolean,
        notes: String
    ) {
        viewModelScope.launch {
            val effectiveDate = if (date.isBlank()) getTodayDateString() else date
            repository.addMaintenance(
                MaintenanceItem(
                    title = title,
                    cost = cost,
                    dateStr = effectiveDate,
                    alertKmRemaining = alertKm,
                    alertDaysRemaining = alertDays,
                    isCompleted = isCompleted,
                    notes = notes
                )
            )
            if (isCompleted && cost > 0) {
                repository.addExpense(
                    Expense(
                        category = "Manutenção",
                        amount = cost,
                        description = title,
                        dateStr = effectiveDate
                    )
                )
            }
        }
    }

    fun toggleMaintenanceStatus(item: MaintenanceItem) {
        viewModelScope.launch {
            repository.updateMaintenance(item.copy(isCompleted = !item.isCompleted))
        }
    }

    fun deleteMaintenance(item: MaintenanceItem) {
        viewModelScope.launch { repository.deleteMaintenance(item) }
    }

    companion object {
        fun formatCurrency(amount: Double): String {
            return String.format(Locale.GERMANY, "R$ %.2f", amount).replace(".", ",")
        }

        fun formatKm(km: Double): String {
            return String.format(Locale.GERMANY, "%.1f km", km).replace(".", ",")
        }

        fun getTodayDateString(): String {
            val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
            return sdf.format(Date())
        }

        fun getTodayHeaderString(): String {
            val sdf = SimpleDateFormat("EEE, d 'de' MMM 'de' yyyy", Locale("pt", "BR"))
            return sdf.format(Date()).replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale("pt", "BR")) else it.toString() }
        }

        fun getCurrentTimeString(): String {
            val sdf = SimpleDateFormat("HH:mm", Locale.getDefault())
            return sdf.format(Date())
        }
    }
}
