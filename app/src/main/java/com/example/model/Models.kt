package com.example.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "trips")
data class Trip(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val platform: String, // "99", "Uber", "InDrive", "Particular"
    val netAmount: Double, // R$ ganho líquido
    val grossAmount: Double = netAmount,
    val fee: Double = 0.0,
    val distanceKm: Double, // km rodados
    val durationMinutes: Int, // minutos
    val timeStr: String, // "10:24"
    val dateStr: String, // "26/05/2025" ou data atual
    val notes: String = "",
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "expenses")
data class Expense(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val category: String, // "Combustível", "Alimentação", "Manutenção", "Estacionamento", "Lavagem", "Outros"
    val amount: Double,
    val description: String,
    val dateStr: String, // "26/05/2025" ou data atual
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "fuel_logs")
data class FuelLog(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val dateStr: String,
    val liters: Double, // Litros ou kWh se elétrico
    val totalAmount: Double,
    val pricePerLiter: Double,
    val fuelType: String, // "Gasolina", "Etanol", "GNV", "Diesel", "Elétrico"
    val odometerKm: Double,
    val averageKmPerLiter: Double, // km/L ou km/kWh
    val stationName: String = "Posto / Ponto de Recarga",
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "maintenance_items")
data class MaintenanceItem(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val cost: Double,
    val dateStr: String,
    val alertKmRemaining: Int = 0,
    val alertDaysRemaining: Int = 0,
    val isCompleted: Boolean = false,
    val notes: String = "",
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "vehicle_profile")
data class VehicleProfile(
    @PrimaryKey val id: Int = 1,
    val driverName: String = "",
    val modelName: String = "",
    val manufacturingYear: String = "",
    val fuelType: String = "Flex", // "Gasolina", "Etanol", "Flex", "GNV", "Diesel", "Elétrico", "Híbrido"
    val isElectric: Boolean = false,
    val averageConsumption: Double = 0.0, // km/L ou km/kWh
    val fuelOrEnergyPrice: Double = 0.0, // R$/L ou R$/kWh
    val currentMileage: Double = 0.0,
    val dailyGoal: Double = 250.0,
    val weeklyGoal: Double = 1500.0,
    val activePlatforms: String = "99, Uber, InDrive",
    val isProfileConfigured: Boolean = false,
    val notificationsEnabled: Boolean = true
)
