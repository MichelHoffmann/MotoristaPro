package com.example.model

/**
 * Viability Calculation Result according to User Requirements:
 * - Meta mensal cadastrada
 * - Dias de trabalho planejados
 * - Custos fixos mensais do veículo
 * - Custo variável por quilômetro (combustível/energia, manutenção etc.)
 * - Quilometragem média diária (incluindo deslocamentos sem passageiros / deadhead)
 * - Horas médias de trabalho por dia
 * - Margem de segurança para imprevistos
 *
 * Resultados:
 * - Meta de faturamento diário
 * - Meta de lucro líquido diário
 * - Valor mínimo recomendado por quilômetro
 * - Valor mínimo recomendado por hora
 * - Custo estimado por quilômetro
 * - Custo diário estimado do veículo
 * - Faturamento necessário para cobrir todos os custos
 * - Lucro líquido estimado após as despesas
 */
data class ViabilityCalculationResult(
    val hasSufficientData: Boolean,
    val missingDataMessage: String = "",
    val isGoalNetProfit: Boolean = false,

    // Metas
    val monthlyGoal: Double = 0.0,
    val plannedWorkDaysCount: Int = 0,
    val dailyRevenueTarget: Double = 0.0, // Meta de faturamento diário
    val dailyNetProfitTarget: Double = 0.0, // Meta de lucro líquido diário

    // Custos do Veículo
    val monthlyFixedCosts: Double = 0.0, // Custo fixo mensal (financiamento, seguro, IPVA)
    val dailyFixedCost: Double = 0.0, // Custo fixo por dia de trabalho
    val estimatedCostPerKm: Double = 0.0, // Custo estimado total por km (energia/combustível + desgaste)
    val energyCostPerKm: Double = 0.0, // Parcela de combustível / energia por km
    val maintenanceCostPerKm: Double = 0.0, // Parcela de manutenção preventiva / pneus por km
    val estimatedDailyVehicleCost: Double = 0.0, // Custo diário estimado do veículo

    // Mínimos Recomendados
    val recommendedMinRatePerKm: Double = 0.0, // Valor mínimo recomendado por km remunerado
    val recommendedMinRatePerHour: Double = 0.0, // Valor mínimo recomendado por hora trabalhada

    // Totais e Projeções
    val monthlyNecessaryRevenue: Double = 0.0, // Faturamento necessário total no mês
    val dailyNecessaryRevenue: Double = 0.0, // Faturamento diário necessário para cobrir custos + lucro
    val estimatedNetProfit: Double = 0.0, // Lucro líquido estimado
    val deadheadPercentage: Double = 25.0,
    val safetyMarginPercentage: Double = 10.0,
    val avgDailyKm: Double = 150.0,
    val avgDailyHours: Double = 8.0,
    val estimatedDailyKmToGoal: Double = 0.0, // Estimativa de km que o motorista provavelmente rodará para bater a meta

    // Explicação didática simples e transparente
    val explanationText: String = ""
)

object ViabilityCalculator {

    fun calculate(
        profile: VehicleProfile,
        costItems: List<VehicleCostItem>,
        plannedDaysCount: Int
    ): ViabilityCalculationResult {
        // Validação e adoção de valores médios quando não informados
        val days = if (plannedDaysCount > 0) plannedDaysCount else 22 // padrão 22 dias se ainda não marcou
        val avgKm = if (profile.avgDailyKm > 0) profile.avgDailyKm else 150.0
        val avgHours = if (profile.avgDailyHours > 0) profile.avgDailyHours else 8.0

        // Valores médios automáticos para cada tipo de propulsão
        val defaultConsumption = VehicleCatalog.getDefaultConsumption(profile.vehiclePowertrain)
        val defaultElectricConsumption = VehicleCatalog.getDefaultElectricConsumption(profile.vehiclePowertrain)
        val defaultFuelPrice = VehicleCatalog.getDefaultFuelPrice(profile.vehiclePowertrain)

        val consumption = if (profile.averageConsumption > 0.0) profile.averageConsumption else defaultConsumption
        val electricConsumption = if (profile.electricConsumption > 0.0) profile.electricConsumption else defaultElectricConsumption
        val fuelPrice = if (profile.fuelOrEnergyPrice > 0.0) profile.fuelOrEnergyPrice else defaultFuelPrice

        // 1. Custos Fixos Mensais do Veículo (financiamento/aluguel, seguro, IPVA, etc.)
        val monthlyFixed = costItems.filter { it.category.isFixedCost }.sumOf { it.monthlyEquivalentCost }
        val dailyFixed = if (days > 0) monthlyFixed / days else 0.0

        // 2. Custo de Combustível / Energia por km
        val energyPerKm = when (profile.vehiclePowertrain) {
            VehiclePowertrain.ELETRICO -> {
                val effectiveKwhPrice = if (profile.homeKwhPrice > 0) {
                    profile.homeKwhPrice * 0.7 + profile.publicKwhPrice * 0.3
                } else fuelPrice
                effectiveKwhPrice / consumption
            }
            VehiclePowertrain.HIBRIDO_PLUGIN -> {
                val gasPart = fuelPrice / consumption
                val elecPart = if (electricConsumption > 0) profile.homeKwhPrice / electricConsumption else 0.18
                (gasPart * 0.5) + (elecPart * 0.5)
            }
            VehiclePowertrain.COMBUSTAO, VehiclePowertrain.HIBRIDO -> {
                fuelPrice / consumption
            }
        }

        // 3. Custos Variáveis de Manutenção (pneus, óleo, revisões periódicas)
        val recordedMaintenanceMonthly = costItems.filter { !it.category.isFixedCost }.sumOf { it.monthlyEquivalentCost }
        val totalMonthlyEstimatedKm = days * avgKm
        val maintenancePerKm = if (totalMonthlyEstimatedKm > 0 && recordedMaintenanceMonthly > 0) {
            recordedMaintenanceMonthly / totalMonthlyEstimatedKm
        } else {
            0.16
        }

        val baseVariableCostPerKm = energyPerKm + maintenancePerKm
        val safetyFactor = 1.0 + (profile.safetyMarginPercentage.coerceIn(0.0, 50.0) / 100.0)
        val estimatedTotalCostPerKm = baseVariableCostPerKm * safetyFactor

        // Custo diário estimado do veículo = Custo Fixo do dia + Custo Variável dos km diários
        val dailyVariableCost = avgKm * estimatedTotalCostPerKm
        val estimatedDailyVehicleCost = dailyFixed + dailyVariableCost
        val monthlyVehicleCosts = estimatedDailyVehicleCost * days

        // 4. Meta de Lucro Líquido (sempre lucro líquido como solicitado)
        val monthlyGoal = if (profile.monthlyGoal > 0) profile.monthlyGoal else 6000.0
        val isNetProfit = true

        val targetNet = monthlyGoal / days
        val targetGross = targetNet + estimatedDailyVehicleCost
        val (dailyTargetRevenue, dailyTargetNetProfit, monthlyNecessaryRevenue) = Triple(
            targetGross,
            targetNet,
            monthlyGoal + monthlyVehicleCosts
        )

        // 5. Consideração de Quilometragem sem Passageiros (Deadhead)
        // Se 25% dos km são vazios, apenas 75% dos km são remunerados pela plataforma
        val paidKmRatio = (1.0 - (profile.deadheadPercentage.coerceIn(0.0, 60.0) / 100.0)).coerceAtLeast(0.4)
        val dailyPaidKm = avgKm * paidKmRatio

        // Tabela fixa padrão do app em R$ 2,00 por km rodado
        val calculatedRatePerKm = if (dailyPaidKm > 0) dailyTargetRevenue / dailyPaidKm else 2.00
        val recommendedRatePerKm = maxOf(calculatedRatePerKm, 2.00)

        // Quilometragem estimada total que o motorista provavelmente rodará no dia para atingir a meta
        val estimatedDailyKmToGoal = if (paidKmRatio > 0 && recommendedRatePerKm > 0) {
            (dailyTargetRevenue / recommendedRatePerKm) / paidKmRatio
        } else avgKm

        // Valor mínimo recomendado por hora trabalhada:
        val recommendedRatePerHour = if (avgHours > 0) dailyTargetRevenue / avgHours else 35.0

        val explanation = buildString {
            append("1. Custos do Veículo: Seu custo por km é de R$ ${String.format("%.2f", estimatedTotalCostPerKm).replace(".", ",")}, ")
            append("incluindo combustível/energia (R$ ${String.format("%.2f", energyPerKm).replace(".", ",")}/km) ")
            append("e manutenção/desgaste (R$ ${String.format("%.2f", maintenancePerKm).replace(".", ",")}/km) ")
            append("com margem de segurança de ${profile.safetyMarginPercentage.toInt()}%.\n")
            append("2. Custo diário fixo diluído em $days dias de trabalho: R$ ${String.format("%.2f", dailyFixed).replace(".", ",")}/dia.\n")
            append("3. Quilômetros Não Remunerados: Consideramos que ${profile.deadheadPercentage.toInt()}% dos seus ${avgKm.toInt()} km diários são vazios (deslocamento e retorno), ")
            append("restando ${String.format("%.1f", dailyPaidKm).replace(".", ",")} km remunerados.\n")
            append("4. Para atingir sua meta de lucro líquido de R$ ${String.format("%.2f", monthlyGoal).replace(".", ",")}, ")
            append("você precisará faturar R$ ${String.format("%.2f", dailyTargetRevenue).replace(".", ",")} por dia e provavelmente rodará cerca de ~${estimatedDailyKmToGoal.toInt()} km no dia.\n")
            append("5. Tabela fixa padrão do app: R$ ${String.format("%.2f", recommendedRatePerKm).replace(".", ",")} por km rodado (tabela padrão de R$ 2,00/km) e R$ ${String.format("%.2f", recommendedRatePerHour).replace(".", ",")} por hora.")
        }

        return ViabilityCalculationResult(
            hasSufficientData = true,
            isGoalNetProfit = isNetProfit,
            monthlyGoal = monthlyGoal,
            plannedWorkDaysCount = days,
            dailyRevenueTarget = dailyTargetRevenue,
            dailyNetProfitTarget = dailyTargetNetProfit,
            monthlyFixedCosts = monthlyFixed,
            dailyFixedCost = dailyFixed,
            estimatedCostPerKm = estimatedTotalCostPerKm,
            energyCostPerKm = energyPerKm,
            maintenanceCostPerKm = maintenancePerKm,
            estimatedDailyVehicleCost = estimatedDailyVehicleCost,
            recommendedMinRatePerKm = recommendedRatePerKm,
            recommendedMinRatePerHour = recommendedRatePerHour,
            monthlyNecessaryRevenue = monthlyNecessaryRevenue,
            dailyNecessaryRevenue = dailyTargetRevenue,
            estimatedNetProfit = dailyTargetNetProfit * days,
            deadheadPercentage = profile.deadheadPercentage,
            safetyMarginPercentage = profile.safetyMarginPercentage,
            avgDailyKm = avgKm,
            avgDailyHours = avgHours,
            estimatedDailyKmToGoal = estimatedDailyKmToGoal,
            explanationText = explanation
        )
    }
}
