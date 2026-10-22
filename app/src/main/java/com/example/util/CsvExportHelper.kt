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
import com.example.model.WorkShiftState
import java.io.OutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object CsvExportHelper {

    fun generateFullCsv(
        profile: VehicleProfile,
        dailyEarnings: List<DailyEarning>,
        expenses: List<Expense>,
        vehicleCostItems: List<VehicleCostItem>,
        workDays: List<WorkDay>,
        viabilityResult: ViabilityCalculationResult,
        shiftState: WorkShiftState = WorkShiftState.IDLE,
        shiftStartTimeStr: String = "",
        shiftStartTimeMillis: Long? = null,
        accumulatedWorkSeconds: Long = 0L,
        breakStartTimeMillis: Long? = null,
        accumulatedBreakSeconds: Long = 0L,
        breakStartTimeStr: String = "",
        breakEndTimeStr: String = ""
    ): String {
        val sb = StringBuilder()

        val now = System.currentTimeMillis()
        val sdfDate = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        val sdfTime = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
        val todayDateStr = sdfDate.format(Date(now))
        val currentTimeStr = sdfTime.format(Date(now))

        // Calculate live stopwatch seconds
        val liveWorkSeconds = if (shiftState == WorkShiftState.WORKING && shiftStartTimeMillis != null) {
            val liveSec = (now - shiftStartTimeMillis).coerceAtLeast(0L) / 1000L
            accumulatedWorkSeconds + liveSec
        } else {
            accumulatedWorkSeconds
        }

        val liveBreakSeconds = if (shiftState == WorkShiftState.ON_BREAK && breakStartTimeMillis != null) {
            val liveSec = (now - breakStartTimeMillis).coerceAtLeast(0L) / 1000L
            accumulatedBreakSeconds + liveSec
        } else {
            accumulatedBreakSeconds
        }

        fun formatSeconds(totalSec: Long): String {
            val h = totalSec / 3600
            val m = (totalSec % 3600) / 60
            val s = totalSec % 60
            return String.format(Locale.getDefault(), "%02d:%02d:%02d", h, m, s)
        }

        val todayEarningExisting = dailyEarnings.find { it.dateStr == todayDateStr }
        val hasShiftStartedToday = shiftState != WorkShiftState.IDLE ||
                shiftStartTimeStr.isNotBlank() ||
                (todayEarningExisting != null && todayEarningExisting.startTime.isNotBlank()) ||
                accumulatedWorkSeconds > 0L

        val shiftStatusLabel = when (shiftState) {
            WorkShiftState.WORKING -> "EM ANDAMENTO (Trabalhando agora)"
            WorkShiftState.ON_BREAK -> "EM DESCANSO (Pausa ativa)"
            WorkShiftState.IDLE -> if (hasShiftStartedToday) "FINALIZADO / CONCLUÍDO HOJE" else "NÃO INICIADO"
        }

        val effectiveStart = when {
            shiftStartTimeStr.isNotBlank() -> shiftStartTimeStr
            todayEarningExisting?.startTime?.isNotBlank() == true -> todayEarningExisting.startTime
            else -> ""
        }

        val effectiveBreakStart = when {
            breakStartTimeStr.isNotBlank() -> breakStartTimeStr
            todayEarningExisting?.lunchStartTime?.isNotBlank() == true -> todayEarningExisting.lunchStartTime
            else -> ""
        }

        val effectiveBreakEnd = when {
            breakEndTimeStr.isNotBlank() -> breakEndTimeStr
            todayEarningExisting?.lunchEndTime?.isNotBlank() == true -> todayEarningExisting.lunchEndTime
            else -> ""
        }

        // BOM for Excel compatibility with UTF-8
        sb.append('\uFEFF')
        sb.appendLine("sep=;")

        // HEADER: METADADOS DA EXPORTAÇÃO
        sb.appendLine("=== RELATÓRIO GERAL MOTORISTA PRO ===")
        sb.appendLine("Data de Exportação;$todayDateStr")
        sb.appendLine("Horário de Exportação;$currentTimeStr")
        sb.appendLine()

        // 1. STATUS DO EXPEDIENTE E CRONÔMETRO DE HOJE
        sb.appendLine("=== STATUS DO EXPEDIENTE E CRONÔMETRO DE HOJE ===")
        sb.appendLine("Cronômetro de Trabalho Iniciado Hoje?;${if (hasShiftStartedToday) "SIM" else "NÃO"}")
        sb.appendLine("Status Atual do Turno;$shiftStatusLabel")
        sb.appendLine("Horário de Início do Expediente;${if (effectiveStart.isNotBlank()) effectiveStart else "Não iniciado hoje"}")
        sb.appendLine("Horário de Início do Descanso;${if (effectiveBreakStart.isNotBlank()) effectiveBreakStart else "Nenhum descanso registrado"}")
        sb.appendLine("Horário de Retorno do Descanso;${if (effectiveBreakEnd.isNotBlank()) effectiveBreakEnd else "N/A"}")
        sb.appendLine("Tempo Ativo Trabalhado no Cronômetro;${formatSeconds(liveWorkSeconds)}")
        sb.appendLine("Tempo Total em Descanso;${formatSeconds(liveBreakSeconds)}")
        sb.appendLine()

        // 2. DADOS DO MOTORISTA
        sb.appendLine("=== DADOS DO MOTORISTA ===")
        sb.appendLine("Nome Completo;${profile.driverName.ifBlank { "Não informado" }}")
        sb.appendLine("Plataforma(s);${profile.platform.ifBlank { "Uber e 99" }}")
        sb.appendLine("Telefone;${profile.phoneNumber.ifBlank { "Não informado" }}")
        sb.appendLine("Meta Mensal de Lucro Líquido;R$ ${String.format(Locale.GERMAN, "%.2f", profile.monthlyGoal)}")
        sb.appendLine()

        // 3. DADOS DO VEÍCULO
        sb.appendLine("=== DADOS DO VEÍCULO ===")
        sb.appendLine("Marca / Modelo;${profile.modelName.ifBlank { "Não informado" }}")
        sb.appendLine("Ano de Fabricação;${profile.manufacturingYear.ifBlank { "Não informado" }}")
        sb.appendLine("Tipo de Combustível;${profile.fuelType}")
        sb.appendLine("Motorização;${profile.vehiclePowertrain.displayName}")
        val unitConsumption = if (profile.isElectric) "km/kWh" else "km/l"
        sb.appendLine("Consumo Médio;${String.format(Locale.GERMAN, "%.2f", profile.averageConsumption)} $unitConsumption")
        val unitPrice = if (profile.isElectric) "R$/kWh" else "R$/L"
        sb.appendLine("Preço Combustível / Energia Ref.;R$ ${String.format(Locale.GERMAN, "%.2f", profile.fuelOrEnergyPrice)} $unitPrice")
        sb.appendLine("Autonomia Tanque/Carga Completa;${String.format(Locale.GERMAN, "%.1f", profile.fullRangeKm)} km")
        sb.appendLine("Hodômetro Atual;${String.format(Locale.GERMAN, "%.1f", profile.currentMileage)} km")
        sb.appendLine()

        // 4. PLANEJAMENTO E VIABILIDADE
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

        // 5. HISTÓRICO DE DIAS TRABALHADOS (CORRIDAS E GANHOS)
        sb.appendLine("=== REGISTROS DIÁRIOS DE TRABALHO ===")
        sb.appendLine("Data;Ganhos Brutos (R$);Corridas;Km Rodados;Combustível/Recarga (R$);Tipo Abastecimento;Início Expediente;Início Almoço;Fim Almoço;Fim Expediente;Tempo Trabalhado;Observações")

        // Prepare combined earnings list including today's live shift if active or recorded
        val earningsListToExport = dailyEarnings.toMutableList()
        val todayIndex = earningsListToExport.indexOfFirst { it.dateStr == todayDateStr }

        if (todayIndex >= 0) {
            val existing = earningsListToExport[todayIndex]
            val liveMins = if (liveWorkSeconds > 0) (liveWorkSeconds / 60L).toInt() else existing.calculatedMinutes
            val effectiveEndStr = when {
                shiftState == WorkShiftState.WORKING -> "Em andamento"
                shiftState == WorkShiftState.ON_BREAK -> "Em descanso"
                existing.endTime.isNotBlank() -> existing.endTime
                else -> ""
            }
            earningsListToExport[todayIndex] = existing.copy(
                startTime = if (effectiveStart.isNotBlank()) effectiveStart else existing.startTime,
                lunchStartTime = if (effectiveBreakStart.isNotBlank()) effectiveBreakStart else existing.lunchStartTime,
                lunchEndTime = if (effectiveBreakEnd.isNotBlank()) effectiveBreakEnd else existing.lunchEndTime,
                endTime = effectiveEndStr,
                workedMinutes = liveMins
            )
        } else if (hasShiftStartedToday) {
            // Include today's live active shift even if no final record has been saved yet
            val liveMins = (liveWorkSeconds / 60L).toInt()
            val effectiveEndStr = when (shiftState) {
                WorkShiftState.WORKING -> "Em andamento"
                WorkShiftState.ON_BREAK -> "Em descanso"
                else -> "Finalizado"
            }
            earningsListToExport.add(
                0,
                DailyEarning(
                    dateStr = todayDateStr,
                    totalAmount = 0.0,
                    tripsCount = 0,
                    kmDriven = 0.0,
                    notes = "Expediente de hoje ($shiftStatusLabel)",
                    startTime = effectiveStart,
                    lunchStartTime = effectiveBreakStart,
                    lunchEndTime = effectiveBreakEnd,
                    endTime = effectiveEndStr,
                    workedMinutes = liveMins,
                    fuelOrChargeCost = 0.0,
                    isElectricCharge = profile.isElectric
                )
            )
        }

        val sortedEarnings = earningsListToExport.sortedByDescending { it.dateStr }
        for (item in sortedEarnings) {
            val fuelTypeStr = if (item.isElectricCharge) "Elétrico (kWh)" else "Combustível"
            val workedTimeStr = if (item.dateStr == todayDateStr && (shiftState == WorkShiftState.WORKING || shiftState == WorkShiftState.ON_BREAK)) {
                formatSeconds(liveWorkSeconds)
            } else {
                item.formattedWorkedTime
            }
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
                "$workedTimeStr;" +
                "\"${item.notes.replace("\"", "\"\"")}\""
            )
        }
        sb.appendLine()

        // 6. DESPESAS E MANUTENÇÕES
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

        // 7. CUSTOS FIXOS DO VEÍCULO
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
            context.startActivity(Intent.createChooser(sendIntent, "Compartilhar dados via..."))
        } catch (e: Exception) {
            Toast.makeText(context, "Erro ao compartilhar: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }
}
