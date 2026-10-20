package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.AppDatabase
import com.example.data.AppRepository
import com.example.data.AuthManager
import com.example.data.SupabaseClient
import com.example.model.AuthUser
import com.example.model.Dado
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

data class SupabaseSyncState(
    val isLoading: Boolean = false,
    val isError: Boolean = false,
    val message: String? = null
)

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

    private val database = AppDatabase.getInstance(application)
    private val repository = AppRepository(database.appDao())
    val supabaseClient = SupabaseClient(application)
    val authManager = AuthManager(application)

    val authUser: StateFlow<AuthUser> = authManager.currentUser
    val dados: StateFlow<List<Dado>> = repository.dados
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _supabaseSyncState = MutableStateFlow(SupabaseSyncState())
    val supabaseSyncState: StateFlow<SupabaseSyncState> = _supabaseSyncState.asStateFlow()

    init {
        viewModelScope.launch {
            repository.seedInitialDataIfEmpty()
            if (supabaseClient.isConfigured()) {
                syncSupabaseDados()
            }
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

    // Register daily earnings, total trip count, km, work stages, and fuel/recharge
    fun recordDailyWork(
        date: String,
        totalAmount: Double,
        tripsCount: Int,
        kmDriven: Double,
        notes: String,
        startTime: String = "",
        lunchStartTime: String = "",
        lunchEndTime: String = "",
        endTime: String = "",
        fuelOrChargeCost: Double = 0.0,
        isElectricCharge: Boolean = false
    ) {
        viewModelScope.launch {
            val effectiveDate = if (date.isBlank()) getTodayDateString() else date
            // Automatically mark this day as a work day
            repository.setWorkDay(effectiveDate, true)

            val existing = repository.getDailyEarningByDate(effectiveDate)
            val updated = if (existing != null) {
                existing.copy(
                    totalAmount = totalAmount,
                    tripsCount = tripsCount,
                    kmDriven = kmDriven,
                    notes = notes,
                    startTime = if (startTime.isNotBlank()) startTime else existing.startTime,
                    lunchStartTime = if (lunchStartTime.isNotBlank()) lunchStartTime else existing.lunchStartTime,
                    lunchEndTime = if (lunchEndTime.isNotBlank()) lunchEndTime else existing.lunchEndTime,
                    endTime = if (endTime.isNotBlank()) endTime else existing.endTime,
                    fuelOrChargeCost = if (fuelOrChargeCost > 0) fuelOrChargeCost else existing.fuelOrChargeCost,
                    isElectricCharge = isElectricCharge,
                    timestamp = System.currentTimeMillis()
                )
            } else {
                DailyEarning(
                    dateStr = effectiveDate,
                    totalAmount = totalAmount,
                    tripsCount = tripsCount,
                    kmDriven = kmDriven,
                    notes = notes,
                    startTime = startTime,
                    lunchStartTime = lunchStartTime,
                    lunchEndTime = lunchEndTime,
                    endTime = endTime,
                    fuelOrChargeCost = fuelOrChargeCost,
                    isElectricCharge = isElectricCharge
                )
            }

            repository.addDailyEarning(updated)

            // If fuel/recharge was recorded, also log as FuelChargeRecord & Expense
            if (fuelOrChargeCost > 0.0) {
                val catName = if (isElectricCharge) "Recarga Elétrica" else "Combustível"
                val desc = if (isElectricCharge) "Recarga do dia $effectiveDate" else "Abastecimento do dia $effectiveDate"
                repository.addFuelChargeRecord(
                    FuelChargeRecord(
                        dateStr = effectiveDate,
                        isElectricCharge = isElectricCharge,
                        quantity = 0.0,
                        unitPrice = 0.0,
                        totalCost = fuelOrChargeCost,
                        notes = desc
                    )
                )
                repository.addExpense(
                    Expense(
                        category = catName,
                        amount = fuelOrChargeCost,
                        description = desc,
                        dateStr = effectiveDate
                    )
                )
            }

            // Update odometer if km is greater
            val currentProf = vehicleProfile.value
            if (kmDriven > 0) {
                val diffKm = if (existing != null) (kmDriven - existing.kmDriven).coerceAtLeast(0.0) else kmDriven
                if (diffKm > 0) {
                    repository.updateVehicleProfile(
                        currentProf.copy(currentMileage = currentProf.currentMileage + diffKm)
                    )
                }
            }
        }
    }

    // Set or adjust work stages for a day
    fun updateWorkStages(
        date: String,
        startTime: String,
        lunchStartTime: String,
        lunchEndTime: String,
        endTime: String
    ) {
        viewModelScope.launch {
            val effectiveDate = if (date.isBlank()) getTodayDateString() else date
            repository.setWorkDay(effectiveDate, true)

            val existing = repository.getDailyEarningByDate(effectiveDate)
            val updated = if (existing != null) {
                existing.copy(
                    startTime = startTime,
                    lunchStartTime = lunchStartTime,
                    lunchEndTime = lunchEndTime,
                    endTime = endTime,
                    timestamp = System.currentTimeMillis()
                )
            } else {
                DailyEarning(
                    dateStr = effectiveDate,
                    totalAmount = 0.0,
                    tripsCount = 0,
                    kmDriven = 0.0,
                    startTime = startTime,
                    lunchStartTime = lunchStartTime,
                    lunchEndTime = lunchEndTime,
                    endTime = endTime
                )
            }
            repository.addDailyEarning(updated)
        }
    }

    // Quick action: Advance to the next work stage with current time
    fun recordWorkStageAction(stage: String, date: String = getTodayDateString(), time: String = getCurrentTimeString()) {
        viewModelScope.launch {
            val effectiveDate = if (date.isBlank()) getTodayDateString() else date
            repository.setWorkDay(effectiveDate, true)

            val existing = repository.getDailyEarningByDate(effectiveDate)
            val updated = if (existing != null) {
                when (stage) {
                    "START" -> existing.copy(startTime = time)
                    "LUNCH_START" -> existing.copy(lunchStartTime = time)
                    "LUNCH_END" -> existing.copy(lunchEndTime = time)
                    "END" -> existing.copy(endTime = time)
                    else -> existing
                }
            } else {
                when (stage) {
                    "START" -> DailyEarning(dateStr = effectiveDate, totalAmount = 0.0, tripsCount = 0, kmDriven = 0.0, startTime = time)
                    "LUNCH_START" -> DailyEarning(dateStr = effectiveDate, totalAmount = 0.0, tripsCount = 0, kmDriven = 0.0, lunchStartTime = time)
                    "LUNCH_END" -> DailyEarning(dateStr = effectiveDate, totalAmount = 0.0, tripsCount = 0, kmDriven = 0.0, lunchEndTime = time)
                    "END" -> DailyEarning(dateStr = effectiveDate, totalAmount = 0.0, tripsCount = 0, kmDriven = 0.0, endTime = time)
                    else -> DailyEarning(dateStr = effectiveDate, totalAmount = 0.0, tripsCount = 0, kmDriven = 0.0)
                }
            }
            repository.addDailyEarning(updated)
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

    // Supabase Operations for table 'dados' (colunas: id, usuario, conteudo)
    fun syncSupabaseDados() {
        viewModelScope.launch {
            _supabaseSyncState.value = SupabaseSyncState(isLoading = true, message = "Sincronizando com Supabase...")
            val result = supabaseClient.fetchDados()
            result.onSuccess { remoteDados ->
                repository.replaceAllDados(remoteDados)
                _supabaseSyncState.value = SupabaseSyncState(
                    isLoading = false,
                    isError = false,
                    message = "Sincronizado! ${remoteDados.size} registro(s) obtidos."
                )
            }.onFailure { err ->
                _supabaseSyncState.value = SupabaseSyncState(
                    isLoading = false,
                    isError = true,
                    message = err.message ?: "Erro ao sincronizar com Supabase"
                )
            }
        }
    }

    fun insertDado(usuario: String, conteudo: String, onComplete: ((Boolean, String?) -> Unit)? = null) {
        viewModelScope.launch {
            _supabaseSyncState.value = SupabaseSyncState(isLoading = true, message = "Salvando registro...")
            val userToSave = usuario.trim().ifBlank {
                val current = authUser.value
                if (current.isLoggedIn) current.name.ifBlank { current.email } else "Motorista"
            }

            val timestampStr = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())

            if (supabaseClient.isConfigured()) {
                val result = supabaseClient.insertDado(userToSave, conteudo)
                result.onSuccess { remoteDado ->
                    repository.addDado(remoteDado)
                    _supabaseSyncState.value = SupabaseSyncState(
                        isLoading = false,
                        isError = false,
                        message = "Registro salvo no Supabase com sucesso!"
                    )
                    onComplete?.invoke(true, null)
                }.onFailure { err ->
                    // Fallback local
                    val local = Dado(
                        id = System.currentTimeMillis(),
                        usuario = userToSave,
                        conteudo = conteudo,
                        createdAt = timestampStr,
                        isSyncedWithSupabase = false
                    )
                    repository.addDado(local)
                    _supabaseSyncState.value = SupabaseSyncState(
                        isLoading = false,
                        isError = true,
                        message = "Salvo localmente (Supabase indisponível: ${err.message})"
                    )
                    onComplete?.invoke(false, err.message)
                }
            } else {
                val local = Dado(
                    id = System.currentTimeMillis(),
                    usuario = userToSave,
                    conteudo = conteudo,
                    createdAt = timestampStr,
                    isSyncedWithSupabase = false
                )
                repository.addDado(local)
                _supabaseSyncState.value = SupabaseSyncState(
                    isLoading = false,
                    isError = false,
                    message = "Salvo na tabela dados local! Configure Supabase para nuvem."
                )
                onComplete?.invoke(true, null)
            }
        }
    }

    fun deleteDado(dado: Dado) {
        viewModelScope.launch {
            repository.deleteDado(dado)
            if (supabaseClient.isConfigured() && dado.id > 0) {
                supabaseClient.deleteDado(dado.id)
            }
        }
    }

    fun testSupabaseConnection(onResult: (Boolean, String) -> Unit) {
        viewModelScope.launch {
            _supabaseSyncState.value = SupabaseSyncState(isLoading = true, message = "Testando conexão com Supabase...")
            val res = supabaseClient.testConnection()
            res.onSuccess { msg ->
                _supabaseSyncState.value = SupabaseSyncState(isLoading = false, isError = false, message = msg)
                onResult(true, msg)
            }.onFailure { err ->
                val msg = err.message ?: "Falha ao conectar ao Supabase."
                _supabaseSyncState.value = SupabaseSyncState(isLoading = false, isError = true, message = msg)
                onResult(false, msg)
            }
        }
    }

    fun saveSupabaseConfig(url: String, anonKey: String) {
        supabaseClient.saveCustomConfig(url, anonKey)
        syncSupabaseDados()
    }

    fun clearSupabaseConfig() {
        supabaseClient.clearCustomConfig()
        _supabaseSyncState.value = SupabaseSyncState(message = "Configuração do Supabase redefinida.")
    }

    // Google Sign-In & Authentication
    fun signInWithGoogle(context: android.content.Context, onResult: (Boolean, String?) -> Unit) {
        viewModelScope.launch {
            val res = authManager.signInWithGoogleCredentialManager(context)
            res.onSuccess { user ->
                onResult(true, null)
            }.onFailure { err ->
                onResult(false, err.message)
            }
        }
    }

    fun loginWithSimulatedGoogle(name: String, email: String, photoUrl: String = "") {
        authManager.loginWithSimulatedGoogle(name, email, photoUrl)
    }

    fun loginAsGuest() {
        authManager.loginAsGuest()
    }

    fun logout() {
        authManager.logout()
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

        fun getCurrentTimeString(): String {
            val sdf = SimpleDateFormat("HH:mm", Locale.getDefault())
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
