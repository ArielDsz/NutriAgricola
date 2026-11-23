package com.nutriagricola.app.domain.model.program

import org.junit.Assert.*
import org.junit.Test

class ProgramCalculatorTest {
    @Test fun `inverse recipe matches the website example without intermediate rounding`() {
        val target = NutrientAmounts(225.0, 68.0, 225.0, 231.0, 95.0)
        val doses = ProgramCalculator.suggest(target)
        val expected = listOf(113.333333, 855.555556, 593.75, 500.0, 20.676329)
        expected.zip(doses).forEach { (value, dose) -> assertEquals(value, dose.kgHa, 0.00001) }
        val actual = ProgramCalculator.contributions(doses)
        target.values().take(5).zip(actual.values()).forEach { (value, result) ->
            assertEquals(value, result, 0.00001)
        }
        assertEquals(77.1875, actual.sulfur, 0.00001)
    }

    @Test fun `secondary nitrogen surplus is preserved without negative fertilizer doses`() {
        val doses = ProgramCalculator.suggest(NutrientAmounts(phosphorusOxide = 60.0))
        assertEquals(100.0, doses[0].kgHa, 0.00001)
        assertEquals(0.0, doses[4].kgHa, 0.0)
        assertEquals(12.0, ProgramCalculator.contributions(doses).nitrogen, 0.00001)
    }

    @Test fun `zero is valid and invalid numbers are rejected`() {
        assertTrue(ProgramCalculator.suggest(NutrientAmounts()).all { it.kgHa == 0.0 })
        listOf(-1.0, Double.NaN, Double.POSITIVE_INFINITY, Double.MAX_VALUE).forEach { value ->
            assertThrows(IllegalArgumentException::class.java) {
                ProgramCalculator.suggest(NutrientAmounts(phosphorusOxide = value))
            }
        }
    }
}
