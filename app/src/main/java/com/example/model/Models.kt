package com.example.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "daily_earnings")
data class DailyEarning(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val dateStr: String, // "dd/MM/yyyy"
    val totalAmount: Double, // Valor total ganho no dia
    val tripsCount: Int, // Quantidade total de corridas feitas no dia
    val kmDriven: Double, // Total de km rodados no dia
    val notes: String = "",
    val timestamp: Long = System.currentTimeMillis(),
    // Etapas da Jornada de Trabalho (Início da jornada, Pausa almoço, Retorno almoço, Fim da jornada)
    val startTime: String = "", // Etapa 1: Início da jornada (ex: "07:30")
    val lunchStartTime: String = "", // Etapa 2: Início da pausa/almoço (ex: "12:00")
    val lunchEndTime: String = "", // Etapa 3: Retorno do almoço (ex: "13:00")
    val endTime: String = "", // Etapa 4: Fim da jornada (ex: "17:30")
    val workedMinutes: Int = 0 // Minutos totais trabalhados
) {
    val calculatedMinutes: Int
        get() {
            if (workedMinutes > 0) return workedMinutes
            return calculateWorkStageMinutes(startTime, lunchStartTime, lunchEndTime, endTime)
        }

    val calculatedHours: Double
        get() = calculatedMinutes / 60.0

    val formattedWorkedTime: String
        get() {
            val mins = calculatedMinutes
            if (mins <= 0) return "0h"
            val h = mins / 60
            val m = mins % 60
            return if (m == 0) "${h}h" else "${h}h ${m}min"
        }

    val stageCount: Int
        get() {
            var count = 0
            if (startTime.isNotBlank()) count++
            if (lunchStartTime.isNotBlank()) count++
            if (lunchEndTime.isNotBlank()) count++
            if (endTime.isNotBlank()) count++
            return count
        }
}

fun calculateWorkStageMinutes(
    startTime: String,
    lunchStartTime: String,
    lunchEndTime: String,
    endTime: String
): Int {
    fun parseTimeToMinutes(t: String): Int? {
        val clean = t.trim()
        if (clean.isBlank()) return null
        val parts = clean.split(":")
        if (parts.size >= 2) {
            val h = parts[0].trim().toIntOrNull() ?: return null
            val m = parts[1].trim().toIntOrNull() ?: return null
            return h * 60 + m
        }
        return null
    }

    fun diffMinutes(from: Int, to: Int): Int {
        return if (to >= from) {
            to - from
        } else {
            (to + 24 * 60) - from
        }
    }

    val start = parseTimeToMinutes(startTime)
    val lunchStart = parseTimeToMinutes(lunchStartTime)
    val lunchEnd = parseTimeToMinutes(lunchEndTime)
    val end = parseTimeToMinutes(endTime)

    if (start == null) return 0

    // Full 4 stages: (lunchStart - start) + (end - lunchEnd)
    if (lunchStart != null && lunchEnd != null && end != null) {
        val shift1 = diffMinutes(start, lunchStart)
        val shift2 = diffMinutes(lunchEnd, end)
        return shift1 + shift2
    }

    // 2 stages: start and end (no lunch break)
    if (end != null) {
        return diffMinutes(start, end)
    }

    // Partial: start and lunchStart (finished shift 1)
    if (lunchStart != null) {
        return diffMinutes(start, lunchStart)
    }

    return 0
}

@Entity(tableName = "work_days")
data class WorkDay(
    @PrimaryKey val dateStr: String, // "dd/MM/yyyy"
    val isWorkDay: Boolean = true
)

@Entity(tableName = "expenses")
data class Expense(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val category: String, // "Combustível", "Alimentação", "Manutenção", "Estacionamento", "Outros"
    val amount: Double,
    val description: String,
    val dateStr: String, // "dd/MM/yyyy"
    val timestamp: Long = System.currentTimeMillis()
)

// Tipos de Veículo solicitados: Combustão, Híbrido, Híbrido plug-in, Elétrico
enum class VehiclePowertrain(val displayName: String) {
    COMBUSTAO("Combustão"),
    HIBRIDO("Híbrido"),
    HIBRIDO_PLUGIN("Híbrido plug-in"),
    ELETRICO("Elétrico")
}

// Custos recorrentes e estruturais do veículo
enum class VehicleExpensePeriodicity(val displayName: String) {
    MENSAL("Mensal"),
    ANUAL("Anual"),
    AVULSA("Avulsa (Única)")
}

enum class VehicleExpenseCategory(val displayName: String, val isFixedCost: Boolean) {
    FINANCIAMENTO_ALUGUEL("Financiamento / Aluguel", true),
    SEGURO_PROTECAO("Seguro e Proteção Veicular", true),
    IPVA_LICENCIAMENTO("IPVA e Licenciamento", true),
    OLEO_FILTROS("Trocas de Óleo e Filtros", false),
    PNEUS_FREIOS("Pneus, Freios, Alinhamento", false),
    REVISAO_MANUTENCAO("Revisões e Manutenção", false),
    LAVAGEM_ESTACIONAMENTO("Lavagens, Estacionamento, Pedágios", false),
    OUTRA("Outras despesas", false)
}

@Entity(tableName = "vehicle_fixed_expenses")
data class VehicleCostItem(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val category: VehicleExpenseCategory,
    val amountPaid: Double, // Valor efetivamente pago
    val periodicity: VehicleExpensePeriodicity, // Mensal, Anual, Avulsa
    val dateStr: String = "",
    val notes: String = "",
    // Dados de financiamento/parcelamento
    val isPaidOff: Boolean = false, // Quitado ou não
    val installmentAmount: Double = 0.0, // Valor da parcela
    val totalInstallments: Int = 0, // Total de parcelas
    val remainingInstallments: Int = 0 // Quantas ainda faltam
) {
    // Converte para valor mensal equivalente para cálculo de planejamento e custo fixo
    val monthlyEquivalentCost: Double
        get() = when (periodicity) {
            VehicleExpensePeriodicity.MENSAL -> if (isPaidOff) 0.0 else (if (installmentAmount > 0) installmentAmount else amountPaid)
            VehicleExpensePeriodicity.ANUAL -> amountPaid / 12.0
            VehicleExpensePeriodicity.AVULSA -> amountPaid / 12.0 // amortizado ao longo do ano para previsibilidade
        }
}

// Registros de Abastecimento e Recarga detalhados
enum class ChargeLocation(val displayName: String) {
    CASA("Casa (Tarifa Residencial)"),
    PUBLICA("Estação Pública / Eletroposto")
}

@Entity(tableName = "energy_fuel_records")
data class FuelChargeRecord(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val dateStr: String,
    val isElectricCharge: Boolean, // true = recarga elétrica (kWh), false = combustível líquido (L)
    val quantity: Double, // Litros ou kWh consumidos
    val unitPrice: Double, // Preço por Litro ou Preço por kWh
    val totalCost: Double, // Valor total pago
    val chargeLocation: ChargeLocation = ChargeLocation.CASA,
    val mileageAtRecord: Double = 0.0,
    val notes: String = "",
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "vehicle_profile")
data class VehicleProfile(
    @PrimaryKey val id: Int = 1,
    val driverName: String = "",
    val modelName: String = "",
    val manufacturingYear: String = "",
    val fuelType: String = "Flex", // Gasolina, Etanol, Flex, GNV, Diesel, Elétrico, Híbrido
    val isElectric: Boolean = false,
    val vehiclePowertrain: VehiclePowertrain = VehiclePowertrain.COMBUSTAO, // Combustão, Híbrido, Híbrido plug-in, Elétrico
    // Consumo e preços
    val averageConsumption: Double = 0.0, // km/L (combustão/híbrido) ou km/kWh (elétrico/híbrido plug-in)
    val electricConsumption: Double = 0.0, // km/kWh específico para híbrido plug-in
    val fuelOrEnergyPrice: Double = 0.0, // R$/L ou R$/kWh padrão
    val homeKwhPrice: Double = 0.85, // Preço por kWh em casa
    val publicKwhPrice: Double = 2.10, // Preço por kWh em eletroposto público
    val currentMileage: Double = 0.0,
    val monthlyGoal: Double = 6000.0, // Meta mensal de Lucro Líquido
    val isGoalNetProfit: Boolean = true, // true = Lucro líquido desejado (regra do app)
    val isProfileConfigured: Boolean = false,
    // Parâmetros para cálculo de viabilidade (R$/km e R$/hora mínimo)
    val avgDailyKm: Double = 150.0, // Quilometragem média diária estimada
    val avgDailyHours: Double = 8.0, // Horas médias trabalhadas por dia
    val deadheadPercentage: Double = 25.0, // % de km sem passageiros (deslocamento/retorno)
    val safetyMarginPercentage: Double = 10.0 // % margem de segurança para imprevistos
)
