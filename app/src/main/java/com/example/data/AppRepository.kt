package com.example.data

import com.example.model.Expense
import com.example.model.FuelLog
import com.example.model.MaintenanceItem
import com.example.model.Trip
import com.example.model.VehicleProfile
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull

class AppRepository(private val dao: AppDao) {

    val trips: Flow<List<Trip>> = dao.getAllTrips()
    val expenses: Flow<List<Expense>> = dao.getAllExpenses()
    val fuelLogs: Flow<List<FuelLog>> = dao.getAllFuelLogs()
    val maintenance: Flow<List<MaintenanceItem>> = dao.getAllMaintenance()
    val vehicleProfile: Flow<VehicleProfile?> = dao.getVehicleProfile()

    suspend fun addTrip(trip: Trip) = dao.insertTrip(trip)
    suspend fun deleteTrip(trip: Trip) = dao.deleteTrip(trip)

    suspend fun addExpense(expense: Expense) = dao.insertExpense(expense)
    suspend fun deleteExpense(expense: Expense) = dao.deleteExpense(expense)

    suspend fun addFuelLog(fuelLog: FuelLog) = dao.insertFuelLog(fuelLog)
    suspend fun deleteFuelLog(fuelLog: FuelLog) = dao.deleteFuelLog(fuelLog)

    suspend fun addMaintenance(item: MaintenanceItem) = dao.insertMaintenance(item)
    suspend fun updateMaintenance(item: MaintenanceItem) = dao.updateMaintenance(item)
    suspend fun deleteMaintenance(item: MaintenanceItem) = dao.deleteMaintenance(item)

    suspend fun updateVehicleProfile(profile: VehicleProfile) = dao.saveVehicleProfile(profile)

    suspend fun clearAllFinancialData() {
        dao.clearAllTrips()
        dao.clearAllExpenses()
        dao.clearAllFuelLogs()
        dao.clearAllMaintenance()
    }

    suspend fun resetAllDataToZero() {
        clearAllFinancialData()
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
                dailyGoal = 250.0,
                weeklyGoal = 1500.0,
                isProfileConfigured = false
            )
        )
    }

    suspend fun seedInitialDataIfEmpty() {
        // App starts with zeroed data as requested.
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
                    dailyGoal = 250.0,
                    weeklyGoal = 1500.0,
                    isProfileConfigured = false
                )
            )
        }
    }
}
