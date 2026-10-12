package com.example.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.model.Expense
import com.example.model.FuelLog
import com.example.model.MaintenanceItem
import com.example.model.Trip
import com.example.model.VehicleProfile
import kotlinx.coroutines.flow.Flow

@Dao
interface AppDao {
    // Trips
    @Query("SELECT * FROM trips ORDER BY id DESC")
    fun getAllTrips(): Flow<List<Trip>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTrip(trip: Trip): Long

    @Delete
    suspend fun deleteTrip(trip: Trip)

    // Expenses
    @Query("SELECT * FROM expenses ORDER BY id DESC")
    fun getAllExpenses(): Flow<List<Expense>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertExpense(expense: Expense): Long

    @Delete
    suspend fun deleteExpense(expense: Expense)

    // Fuel Logs
    @Query("SELECT * FROM fuel_logs ORDER BY id DESC")
    fun getAllFuelLogs(): Flow<List<FuelLog>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFuelLog(fuelLog: FuelLog): Long

    @Delete
    suspend fun deleteFuelLog(fuelLog: FuelLog)

    // Maintenance
    @Query("SELECT * FROM maintenance_items ORDER BY isCompleted ASC, id DESC")
    fun getAllMaintenance(): Flow<List<MaintenanceItem>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMaintenance(item: MaintenanceItem): Long

    @Update
    suspend fun updateMaintenance(item: MaintenanceItem)

    @Delete
    suspend fun deleteMaintenance(item: MaintenanceItem)

    // Vehicle Profile
    @Query("SELECT * FROM vehicle_profile WHERE id = 1 LIMIT 1")
    fun getVehicleProfile(): Flow<VehicleProfile?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveVehicleProfile(profile: VehicleProfile)

    @Query("DELETE FROM trips")
    suspend fun clearAllTrips()

    @Query("DELETE FROM expenses")
    suspend fun clearAllExpenses()

    @Query("DELETE FROM fuel_logs")
    suspend fun clearAllFuelLogs()

    @Query("DELETE FROM maintenance_items")
    suspend fun clearAllMaintenance()

    @Query("DELETE FROM vehicle_profile")
    suspend fun clearVehicleProfile()
}
