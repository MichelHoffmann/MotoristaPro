package com.example.data

import com.example.model.DailyEarning
import com.example.model.Expense
import com.example.model.FuelChargeRecord
import com.example.model.VehicleCostItem
import com.example.model.VehicleProfile
import com.example.model.WorkDay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull

class AppRepository(private val dao: AppDao) {

    val dailyEarnings: Flow<List<DailyEarning>> = dao.getAllDailyEarnings()
    val workDays: Flow<List<WorkDay>> = dao.getAllWorkDays()
    val expenses: Flow<List<Expense>> = dao.getAllExpenses()
    val vehicleProfile: Flow<VehicleProfile?> = dao.getVehicleProfile()
    val vehicleCostItems: Flow<List<VehicleCostItem>> = dao.getAllVehicleCostItems()
    val fuelChargeRecords: Flow<List<FuelChargeRecord>> = dao.getAllFuelChargeRecords()
    val dados: Flow<List<com.example.model.Dado>> = dao.getAllDados()

    suspend fun addDailyEarning(earning: DailyEarning) = dao.insertDailyEarning(earning)
    suspend fun deleteDailyEarning(earning: DailyEarning) = dao.deleteDailyEarning(earning)
    suspend fun getDailyEarningByDate(dateStr: String): DailyEarning? = dao.getDailyEarningByDate(dateStr)

    // Tabela dados
    suspend fun addDado(dado: com.example.model.Dado) = dao.insertDado(dado)
    suspend fun deleteDado(dado: com.example.model.Dado) = dao.deleteDado(dado)
    suspend fun clearDados() = dao.clearAllDados()
    suspend fun replaceAllDados(list: List<com.example.model.Dado>) {
        dao.clearAllDados()
        dao.insertAllDados(list)
    }

    suspend fun toggleWorkDay(dateStr: String) {
        val currentDays = workDays.firstOrNull() ?: emptyList()
        val exists = currentDays.any { it.dateStr == dateStr && it.isWorkDay }
        if (exists) {
            dao.deleteWorkDayByDate(dateStr)
        } else {
            dao.insertWorkDay(WorkDay(dateStr = dateStr, isWorkDay = true))
        }
    }

    suspend fun setWorkDay(dateStr: String, isWork: Boolean) {
        if (isWork) {
            dao.insertWorkDay(WorkDay(dateStr = dateStr, isWorkDay = true))
        } else {
            dao.deleteWorkDayByDate(dateStr)
        }
    }

    suspend fun addExpense(expense: Expense) = dao.insertExpense(expense)
    suspend fun deleteExpense(expense: Expense) = dao.deleteExpense(expense)

    // Vehicle Costs
    suspend fun addVehicleCostItem(item: VehicleCostItem) = dao.insertVehicleCostItem(item)
    suspend fun deleteVehicleCostItem(item: VehicleCostItem) = dao.deleteVehicleCostItem(item)

    // Fuel and Energy records
    suspend fun addFuelChargeRecord(record: FuelChargeRecord) = dao.insertFuelChargeRecord(record)
    suspend fun deleteFuelChargeRecord(record: FuelChargeRecord) = dao.deleteFuelChargeRecord(record)

    suspend fun updateFuelExpenseForDate(dateStr: String, fuelCost: Double, isElectric: Boolean) {
        val currentExpenses = expenses.firstOrNull() ?: emptyList()
        currentExpenses.filter {
            it.dateStr == dateStr && (it.description.startsWith("Abastecimento do dia") || it.description.startsWith("Recarga do dia") || it.category == "Combustível" || it.category == "Recarga Elétrica")
        }.forEach { dao.deleteExpense(it) }

        val currentFuelRecords = fuelChargeRecords.firstOrNull() ?: emptyList()
        currentFuelRecords.filter { it.dateStr == dateStr }.forEach { dao.deleteFuelChargeRecord(it) }

        if (fuelCost > 0.0) {
            val catName = if (isElectric) "Recarga Elétrica" else "Combustível"
            val desc = if (isElectric) "Recarga do dia $dateStr" else "Abastecimento do dia $dateStr"
            dao.insertFuelChargeRecord(
                FuelChargeRecord(
                    dateStr = dateStr,
                    isElectricCharge = isElectric,
                    quantity = 0.0,
                    unitPrice = 0.0,
                    totalCost = fuelCost,
                    notes = desc
                )
            )
            dao.insertExpense(
                Expense(
                    category = catName,
                    amount = fuelCost,
                    description = desc,
                    dateStr = dateStr
                )
            )
        }
    }

    suspend fun updateVehicleProfile(profile: VehicleProfile) = dao.saveVehicleProfile(profile)

    suspend fun resetAllDataToZero() {
        dao.clearAllDailyEarnings()
        dao.clearAllWorkDays()
        dao.clearAllExpenses()
        dao.clearAllVehicleCostItems()
        dao.clearAllFuelChargeRecords()
        dao.clearVehicleProfile()
        dao.saveVehicleProfile(
            VehicleProfile(
                id = 1,
                driverName = "",
                modelName = "",
                manufacturingYear = "",
                fuelType = "Flex",
                isElectric = false,
                averageConsumption = 0.0,
                fuelOrEnergyPrice = 0.0,
                currentMileage = 0.0,
                monthlyGoal = 6000.0,
                isProfileConfigured = false
            )
        )
    }

    suspend fun seedInitialDataIfEmpty() {
        val existingProfile = vehicleProfile.firstOrNull()
        if (existingProfile == null) {
            dao.saveVehicleProfile(
                VehicleProfile(
                    id = 1,
                    driverName = "",
                    modelName = "",
                    manufacturingYear = "",
                    fuelType = "Flex",
                    isElectric = false,
                    averageConsumption = 0.0,
                    fuelOrEnergyPrice = 0.0,
                    currentMileage = 0.0,
                    monthlyGoal = 6000.0,
                    isProfileConfigured = false
                )
            )
        }
    }
}
