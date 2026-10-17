package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.AppDatabase
import com.example.data.AppRepository
import com.example.model.DailyEarning
import com.example.model.Expense
import com.example.model.FuelChargeRecord
import com.example.model.VehicleCostItem
import com.example.model.VehiclePowertrain
import com.example.model.VehicleProfile
import com.example.model.ViabilityCalculationResult
import com.example.model.ViabilityCalculator
import com.example.model.WorkDay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

enum class PeriodFilter { DIARIO, SEMANAL, MENSAL, ANUAL }
enum class ExpenseTabFilter { TODAS, CATEGORIAS, COMPROVANTES }

data class ChartBarPoint(
    val label: String,
    val earnings: Float,
    val expenses: Float
)

data class CalendarDay(
    val dayNumber: Int,
    val dateStr: String, // "dd/MM/yyyy"
    val dayOfWeek: Int, // Calendar.SUNDAY ... Calendar.SATURDAY
    val isCurrentMonth: Boolean,
    val isToday: Boolean,
    val isPast: Boolean,
    val isWorkDay: Boolean,
    val earnedAmount: Double = 0.0
)

enum class QuickWorkSchedule {
    MON_TO_FRI,
    MON_TO_SAT,
    ALL_DAYS,
    CLEAR
}

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: AppRepository

    init {
        val database = AppDatabase.getInstance(application)
        repository = AppRepository(database.appDao())
        viewModelScope.launch {
            repository.seedInitialDataIfEmpty()
        }
    }

    val dailyEarnings: StateFlow<List<DailyEarning>> = repository.dailyEarnings
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val workDays: StateFlow<List<WorkDay>> = repository.workDays
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val expenses: StateFlow<List<Expense>> = repository.expenses
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val vehicleProfile: StateFlow<VehicleProfile> = repository.vehicleProfile
        .map { it ?: VehicleProfile() }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), VehicleProfile())

    val vehicleCostItems: StateFlow<List<VehicleCostItem>> = repository.vehicleCostItems
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val fuelChargeRecords: StateFlow<List<FuelChargeRecord>> = repository.fuelChargeRecords
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _selectedPeriod = MutableStateFlow(PeriodFilter.DIARIO)
    val selectedPeriod: StateFlow<PeriodFilter> = _selectedPeriod.asStateFlow()

    private val _selectedExpenseTab = MutableStateFlow(ExpenseTabFilter.TODAS)
    val selectedExpenseTab: StateFlow<ExpenseTabFilter> = _selectedExpenseTab.asStateFlow()

    fun setPeriodFilter(filter: PeriodFilter) { _selectedPeriod.value = filter }
    fun setExpenseTabFilter(filter: ExpenseTabFilter) { _selectedExpenseTab.value = filter }

    // Calendar state (Selected Year & Month for planning)
    private val calendar = Calendar.getInstance()
    private val _currentYearMonth = MutableStateFlow(
        Pair(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH))
    )
    val currentYearMonth: StateFlow<Pair<Int, Int>> = _currentYearMonth.asStateFlow()

    fun nextMonth() {
        val (year, month) = _currentYearMonth.value
        if (month == Calendar.DECEMBER) {
            _currentYearMonth.value = Pair(year + 1, Calendar.JANUARY)
        } else {
            _currentYearMonth.value = Pair(year, month + 1)
        }
    }

    fun previousMonth() {
        val (year, month) = _currentYearMonth.value
        if (month == Calendar.JANUARY) {
            _currentYearMonth.value = Pair(year - 1, Calendar.DECEMBER)
        } else {
            _currentYearMonth.value = Pair(year, month - 1)
        }
    }

    // Toggle work day
    fun toggleWorkDay(dateStr: String) {
        viewModelScope.launch {
            repository.toggleWorkDay(dateStr)
        }
    }

    // Apply bulk work schedule for the selected month
    fun applyQuickSchedule(schedule: QuickWorkSchedule) {
        viewModelScope.launch {
            val (year, month) = _currentYearMonth.value
            val cal = Calendar.getInstance()
            cal.set(year, month, 1)
            val maxDay = cal.getActualMaximum(Calendar.DAY_OF_MONTH)

            for (day in 1..maxDay) {
                cal.set(year, month, day)
                val dateStr = String.format(Locale.getDefault(), "%02d/%02d/%04d", day, month + 1, year)
                val dayOfWeek = cal.get(Calendar.DAY_OF_WEEK)

                val shouldWork = when (schedule) {
                    QuickWorkSchedule.MON_TO_FRI -> dayOfWeek in Calendar.MONDAY..Calendar.FRIDAY
                    QuickWorkSchedule.MON_TO_SAT -> dayOfWeek in Calendar.MONDAY..Calendar.SATURDAY
                    QuickWorkSchedule.ALL_DAYS -> true
                    QuickWorkSchedule.CLEAR -> false
                }
                repository.setWorkDay(dateStr, shouldWork)
            }
        }
    }

    // Register daily earnings & total trip count
    fun recordDailyWork(
        date: String,
        totalAmount: Double,
        tripsCount: Int,
        kmDriven: Double,
        notes: String
    ) {
        viewModelScope.launch {
            val effectiveDate = if (date.isBlank()) getTodayDateString() else date
            // Automatically mark this day as a work day
            repository.setWorkDay(effectiveDate, true)

            repository.addDailyEarning(
                DailyEarning(
                    dateStr = effectiveDate,
                    totalAmount = totalAmount,
                    tripsCount = tripsCount,
                    kmDriven = kmDriven,
                    notes = notes
                )
            )

            // Update odometer if km is greater
            val currentProf = vehicleProfile.value
            if (kmDriven > 0) {
                repository.updateVehicleProfile(
                    currentProf.copy(currentMileage = currentProf.currentMileage + kmDriven)
                )
            }
        }
    }

    fun deleteDailyEarning(earning: DailyEarning) {
        viewModelScope.launch { repository.deleteDailyEarning(earning) }
    }

    fun updateMonthlyGoal(goal: Double) {
        viewModelScope.launch {
            val prof = vehicleProfile.value
            repository.updateVehicleProfile(prof.copy(monthlyGoal = goal))
        }
    }

    // Expenses
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

    // Vehicle Cost Items (Fixed & Recurring Costs)
    fun addVehicleCostItem(item: VehicleCostItem) {
        viewModelScope.launch {
            repository.addVehicleCostItem(item)
        }
    }

    fun deleteVehicleCostItem(item: VehicleCostItem) {
        viewModelScope.launch {
            repository.deleteVehicleCostItem(item)
        }
    }

    // Fuel & Electric Charge Records
    fun addFuelChargeRecord(record: FuelChargeRecord) {
        viewModelScope.launch {
            repository.addFuelChargeRecord(record)
            // Also add as expense to keep historical financial sync
            val catName = if (record.isElectricCharge) "Recarga Elétrica" else "Combustível"
            val desc = if (record.isElectricCharge) {
                "${String.format(Locale.GERMANY, "%.1f", record.quantity)} kWh (${record.chargeLocation.displayName})"
            } else {
                "${String.format(Locale.GERMANY, "%.1f", record.quantity)} L"
            }
            repository.addExpense(
                Expense(
                    category = catName,
                    amount = record.totalCost,
                    description = desc,
                    dateStr = record.dateStr
                )
            )
        }
    }

    fun deleteFuelChargeRecord(record: FuelChargeRecord) {
        viewModelScope.launch {
            repository.deleteFuelChargeRecord(record)
        }
    }

    // Save full vehicle & calculation parameters profile
    fun updateFullVehicleProfile(profile: VehicleProfile) {
        viewModelScope.launch {
            repository.updateVehicleProfile(profile)
        }
    }

    // Viability Calculation State Flow
    val viabilityResult: StateFlow<ViabilityCalculationResult> = combine(
        vehicleProfile,
        vehicleCostItems,
        workDays,
        currentYearMonth
    ) { profile, costItems, wDays, yearMonth ->
        val (year, month) = yearMonth
        val plannedThisMonth = wDays.count {
            val parts = it.dateStr.split("/")
            if (parts.size == 3) {
                val dMonth = parts[1].toIntOrNull()?.minus(1)
                val dYear = parts[2].toIntOrNull()
                dMonth == month && dYear == year && it.isWorkDay
            } else false
        }
        val daysCount = if (plannedThisMonth > 0) plannedThisMonth else 22
        ViabilityCalculator.calculate(profile, costItems, daysCount)
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        ViabilityCalculationResult(hasSufficientData = false)
    )

    // Save user profile
    fun saveUserProfile(
        name: String,
        model: String,
        year: String,
        fuelType: String,
        isElectric: Boolean,
        avgConsumption: Double,
        fuelOrEnergyPrice: Double,
        currentMileage: Double,
        monthlyGoal: Double
    ) {
        viewModelScope.launch {
            val current = vehicleProfile.value
            val inferredPowertrain = when {
                isElectric || fuelType.equals("Elétrico", ignoreCase = true) -> VehiclePowertrain.ELETRICO
                fuelType.contains("Plug-in", ignoreCase = true) -> VehiclePowertrain.HIBRIDO_PLUGIN
                fuelType.equals("Híbrido", ignoreCase = true) -> VehiclePowertrain.HIBRIDO
                else -> VehiclePowertrain.COMBUSTAO
            }
            repository.updateVehicleProfile(
                current.copy(
                    id = 1,
                    driverName = name,
                    modelName = model,
                    manufacturingYear = year,
                    fuelType = fuelType,
                    isElectric = isElectric,
                    vehiclePowertrain = inferredPowertrain,
                    averageConsumption = avgConsumption,
                    fuelOrEnergyPrice = fuelOrEnergyPrice,
                    currentMileage = currentMileage,
                    monthlyGoal = if (monthlyGoal > 0) monthlyGoal else 5000.0,
                    isGoalNetProfit = true,
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

    // Dynamic Chart Data
    fun getDynamicChartData(earningsList: List<DailyEarning>, expensesList: List<Expense>): List<ChartBarPoint> {
        if (earningsList.isEmpty() && expensesList.isEmpty()) {
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

        val dates = (earningsList.map { it.dateStr } + expensesList.map { it.dateStr }).distinct().takeLast(7)
        return dates.map { date ->
            val earn = earningsList.filter { it.dateStr == date }.sumOf { it.totalAmount }.toFloat()
            val exp = expensesList.filter { it.dateStr == date }.sumOf { it.amount }.toFloat()
            val shortDate = if (date.length >= 5) date.substring(0, 5) else date
            ChartBarPoint(shortDate, earn, exp)
        }
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

        fun getCurrentMonthName(month: Int): String {
            val months = listOf(
                "Janeiro", "Fevereiro", "Março", "Abril", "Maio", "Junho",
                "Julho", "Agosto", "Setembro", "Outubro", "Novembro", "Dezembro"
            )
            return months.getOrElse(month) { "" }
        }
    }
}
