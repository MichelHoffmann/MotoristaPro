package com.example.util

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import com.example.model.DailyEarning
import com.example.model.Expense
import com.example.model.VehicleCostItem
import com.example.model.VehicleProfile
import com.example.model.ViabilityCalculationResult
import com.example.model.WorkDay
import java.io.OutputStream
import java.util.Locale

object CsvExportHelper {

    fun generateFullCsv(
        profile: VehicleProfile,
        dailyEarnings: List<DailyEarning>,
        expenses: List<Expense>,
        vehicleCostItems: List<VehicleCostItem>,
        workDays: List<WorkDay>,
        viabilityResult: ViabilityCalculationResult
    ): String {
        val sb = StringBuilder()

        // BOM for Excel compatibility with UTF-8
        sb.append('\uFEFF')
        sb.appendLine("sep=;")

        // 1. DADOS DO MOTORISTA
        sb.appendLine("=== DADOS DO MOTORISTA ===")
        sb.appendLine("Nome Completo;${profile.driverName.ifBlank { "Não informado" }}")
        sb.appendLine("Plataforma(s);${profile.platform.ifBlank { "Uber e 99" }}")
        sb.appendLine("Telefone;${profile.phoneNumber.ifBlank { "Não informado" }}")
        sb.appendLine("Meta Mensal de Lucro Líquido;R$ ${String.format(Locale.GERMAN, "%.2f", profile.monthlyGoal)}")
        sb.appendLine()

        // 2. DADOS DO VEÍCULO
        sb.appendLine("=== DADOS DO VEÍCULO ===")
        sb.appendLine("Marca / Modelo;${profile.modelName.ifBlank { "Não informado" }}")
        sb.appendLine("Ano de Fabricação;${profile.manufacturingYear.ifBlank { "Não informado" }}")
        sb.appendLine("Tipo de Combustível;${profile.fuelType}")
        sb.appendLine("Motorização;${profile.vehiclePowertrain.displayName}")
        val unitConsumption = if (profile.isElectric) "km/kWh" else "km/l"
        sb.appendLine("Consumo Médio;${String.format(Locale.GERMAN, "%.2f", profile.averageConsumption)} $unitConsumption")
        val unitPrice = if (profile.isElectric) "R$/kWh" else "R$/L"
        sb.appendLine("Preço Combustível / Energia Ref.;R$ ${String.format(Locale.GERMAN, "%.2f", profile.fuelOrEnergyPrice)} $unitPrice")
        sb.appendLine("Hodômetro Atual;${String.format(Locale.GERMAN, "%.1f", profile.currentMileage)} km")
        sb.appendLine()

        // 3. PLANEJAMENTO E VIABILIDADE
        sb.appendLine("=== PLANEJAMENTO E INDICADORES DE VIABILIDADE ===")
        val plannedDays = workDays.count { it.isWorkDay }
        sb.appendLine("Total de Dias Planejados no Mês;$plannedDays dias")
        sb.appendLine("Km Médio Diário Estimado;${String.format(Locale.GERMAN, "%.1f", profile.avgDailyKm)} km")
        sb.appendLine("Jornada Média Diária Estimada;${String.format(Locale.GERMAN, "%.1f", profile.avgDailyHours)} horas")
        sb.appendLine("Margem de Segurança;${String.format(Locale.GERMAN, "%.1f", profile.safetyMarginPercentage)} %")
        if (viabilityResult.hasSufficientData) {
            sb.appendLine("Custo Mínimo por Km (Viabilidade);R$ ${String.format(Locale.GERMAN, "%.2f", viabilityResult.recommendedMinRatePerKm)}")
            sb.appendLine("Custo Mínimo por Hora (Viabilidade);R$ ${String.format(Locale.GERMAN, "%.2f", viabilityResult.recommendedMinRatePerHour)}")
        }
        sb.appendLine()

        // 4. HISTÓRICO DE DIAS TRABALHADOS (CORRIDAS E GANHOS)
        sb.appendLine("=== REGISTROS DIÁRIOS DE TRABALHO ===")
        sb.appendLine("Data;Ganhos Brutos (R$);Corridas;Km Rodados;Combustível/Recarga (R$);Tipo Abastecimento;Início Expediente;Início Almoço;Fim Almoço;Fim Expediente;Tempo Trabalhado;Observações")
        val sortedEarnings = dailyEarnings.sortedByDescending { it.dateStr }
        for (item in sortedEarnings) {
            val fuelTypeStr = if (item.isElectricCharge) "Elétrico (kWh)" else "Combustível"
            sb.appendLine(
                "${item.dateStr};" +
                "${String.format(Locale.GERMAN, "%.2f", item.totalAmount)};" +
                "${item.tripsCount};" +
                "${String.format(Locale.GERMAN, "%.1f", item.kmDriven)};" +
                "${String.format(Locale.GERMAN, "%.2f", item.fuelOrChargeCost)};" +
                "$fuelTypeStr;" +
                "${item.startTime};" +
                "${item.lunchStartTime};" +
                "${item.lunchEndTime};" +
                "${item.endTime};" +
                "${item.formattedWorkedTime};" +
                "\"${item.notes.replace("\"", "\"\"")}\""
            )
        }
        sb.appendLine()

        // 5. DESPESAS E MANUTENÇÕES
        sb.appendLine("=== DESPESAS ADICIONAIS E MANUTENÇÃO ===")
        sb.appendLine("Data;Categoria;Descrição;Valor (R$)")
        val sortedExpenses = expenses.sortedByDescending { it.dateStr }
        for (exp in sortedExpenses) {
            sb.appendLine(
                "${exp.dateStr};" +
                "${exp.category};" +
                "\"${exp.description.replace("\"", "\"\"")}\";" +
                "${String.format(Locale.GERMAN, "%.2f", exp.amount)}"
            )
        }
        sb.appendLine()

        // 6. CUSTOS FIXOS DO VEÍCULO
        sb.appendLine("=== CUSTOS FIXOS E ESTRUTURAIS DO VEÍCULO ===")
        sb.appendLine("Item;Categoria;Valor (R$);Periodicidade;Quitado?;Parcelas Restantes;Observações")
        for (cost in vehicleCostItems) {
            sb.appendLine(
                "${cost.title};" +
                "${cost.category.displayName};" +
                "${String.format(Locale.GERMAN, "%.2f", cost.amountPaid)};" +
                "${cost.periodicity.displayName};" +
                "${if (cost.isPaidOff) "Sim" else "Não"};" +
                "${cost.remainingInstallments};" +
                "\"${cost.notes.replace("\"", "\"\"")}\""
            )
        }

        return sb.toString()
    }

    fun writeCsvToUri(context: Context, uri: Uri, csvContent: String): Boolean {
        return try {
            context.contentResolver.openOutputStream(uri)?.use { output: OutputStream ->
                output.write(csvContent.toByteArray(Charsets.UTF_8))
                output.flush()
            }
            true
        } catch (e: Exception) {
            false
        }
    }

    fun shareCsvText(context: Context, csvContent: String) {
        try {
            val sendIntent = Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_SUBJECT, "Relatório Completo - Motorista Pro")
                putExtra(Intent.EXTRA_TEXT, csvContent)
            }
            context.startActivity(Intent.createChooser(sendIntent, "Compartilhar dados"))
        } catch (e: Exception) {
            Toast.makeText(context, "Erro ao compartilhar: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }
}
