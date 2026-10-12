package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.model.*
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("Motorista Pro", appName)
  }

  @Test
  fun `test brazilian and acre holidays calculation`() {
    val holidays2026 = BrazilianHolidays.getHolidaysForYear(2026)
    assertTrue("Deveria conter feriados para 2026", holidays2026.isNotEmpty())

    // Feriado Nacional: Tiradentes 21/04
    val tiradentes = holidays2026.find { it.day == 21 && it.month == 4 }
    assertNotNull(tiradentes)
    assertEquals(HolidayType.NACIONAL, tiradentes?.type)

    // Feriado Estadual Acre: Aniversário do Acre 15/06
    val acre = holidays2026.find { it.day == 15 && it.month == 6 }
    assertNotNull(acre)
    assertEquals(HolidayType.ESTADUAL_ACRE, acre?.type)

    // Feriado Municipal Rio Branco: São Sebastião 20/01
    val saoSebastiao = holidays2026.find { it.day == 20 && it.month == 1 }
    assertNotNull(saoSebastiao)
    assertEquals(HolidayType.MUNICIPAL_RIO_BRANCO, saoSebastiao?.type)
  }

  @Test
  fun `test viability calculator with fixed and variable costs and deadhead`() {
    val profile = VehicleProfile(
      driverName = "Carlos",
      modelName = "Onix",
      manufacturingYear = "2023",
      fuelType = "Gasolina",
      averageConsumption = 12.0, // 12 km/L
      fuelOrEnergyPrice = 6.00, // R$ 6.00 / L -> R$ 0.50/km de combustível
      monthlyGoal = 6000.0,
      isGoalNetProfit = false,
      isProfileConfigured = true,
      avgDailyKm = 160.0,
      avgDailyHours = 8.0,
      deadheadPercentage = 25.0, // 25% sem passageiro -> 120 km remunerados
      safetyMarginPercentage = 10.0 // 10% margem
    )

    val costItems = listOf(
      VehicleCostItem(
        title = "Financiamento",
        category = VehicleExpenseCategory.FINANCIAMENTO_ALUGUEL,
        amountPaid = 1200.0,
        periodicity = VehicleExpensePeriodicity.MENSAL,
        installmentAmount = 1200.0
      ),
      VehicleCostItem(
        title = "Seguro Anual",
        category = VehicleExpenseCategory.SEGURO_PROTECAO,
        amountPaid = 2400.0, // R$ 200/mês
        periodicity = VehicleExpensePeriodicity.ANUAL
      )
    )

    val result = ViabilityCalculator.calculate(profile, costItems, 20)
    assertTrue("Deveria conter dados suficientes", result.hasSufficientData)
    assertEquals(1400.0, result.monthlyFixedCosts, 0.01) // 1200 + 200
    assertEquals(70.0, result.dailyFixedCost, 0.01) // 1400 / 20 dias
    assertTrue("Custo por km deve ser positivo", result.estimatedCostPerKm > 0)
    assertTrue("Valor mínimo por km deve ser positivo", result.recommendedMinRatePerKm > 0)
    assertTrue("Valor mínimo por hora deve ser positivo", result.recommendedMinRatePerHour > 0)
  }
}
