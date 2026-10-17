package com.example.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.model.DailyEarning
import com.example.model.Expense
import com.example.model.FuelChargeRecord
import com.example.model.VehicleCostItem
import com.example.model.VehicleProfile
import com.example.model.WorkDay
import kotlinx.coroutines.flow.Flow

@Dao
interface AppDao {
    // Daily Earnings
    @Query("SELECT * FROM daily_earnings ORDER BY id DESC")
    fun getAllDailyEarnings(): Flow<List<DailyEarning>>

    @Query("SELECT * FROM daily_earnings WHERE dateStr = :dateStr LIMIT 1")
    suspend fun getDailyEarningByDate(dateStr: String): DailyEarning?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDailyEarning(earning: DailyEarning): Long

    @Delete
    suspend fun deleteDailyEarning(earning: DailyEarning)

    // Planned Work Days
    @Query("SELECT * FROM work_days")
    fun getAllWorkDays(): Flow<List<WorkDay>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWorkDay(workDay: WorkDay): Long

    @Query("DELETE FROM work_days WHERE dateStr = :dateStr")
    suspend fun deleteWorkDayByDate(dateStr: String)

    // Expenses (Gerais)
    @Query("SELECT * FROM expenses ORDER BY id DESC")
    fun getAllExpenses(): Flow<List<Expense>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertExpense(expense: Expense): Long

    @Delete
    suspend fun deleteExpense(expense: Expense)

    // Vehicle Fixed/Maintenance Cost Items
    @Query("SELECT * FROM vehicle_fixed_expenses ORDER BY id DESC")
    fun getAllVehicleCostItems(): Flow<List<VehicleCostItem>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertVehicleCostItem(item: VehicleCostItem): Long

    @Delete
    suspend fun deleteVehicleCostItem(item: VehicleCostItem)

    // Fuel and Electric Charge Records
    @Query("SELECT * FROM energy_fuel_records ORDER BY id DESC")
    fun getAllFuelChargeRecords(): Flow<List<FuelChargeRecord>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFuelChargeRecord(record: FuelChargeRecord): Long

    @Delete
    suspend fun deleteFuelChargeRecord(record: FuelChargeRecord)

    // Vehicle Profile
    @Query("SELECT * FROM vehicle_profile WHERE id = 1 LIMIT 1")
    fun getVehicleProfile(): Flow<VehicleProfile?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveVehicleProfile(profile: VehicleProfile)

    // Reset All Data
    @Query("DELETE FROM daily_earnings")
    suspend fun clearAllDailyEarnings()

    @Query("DELETE FROM work_days")
    suspend fun clearAllWorkDays()

    @Query("DELETE FROM expenses")
    suspend fun clearAllExpenses()

    @Query("DELETE FROM vehicle_fixed_expenses")
    suspend fun clearAllVehicleCostItems()

    @Query("DELETE FROM energy_fuel_records")
    suspend fun clearAllFuelChargeRecords()

    @Query("DELETE FROM vehicle_profile")
    suspend fun clearVehicleProfile()
}
