package com.nutriagricola.app.domain.model.program

import org.junit.Assert.*
import org.junit.Test

class CropNutritionCatalogTest {
    @Test fun `table grape requirements match NutriXpert at 50 tonnes and 90 percent efficiency`() {
        val result = CropNutritionCatalog.requirements(CropNutritionCatalog.profiles.first(), 50.0, 90.0)
        assertEquals(listOf(133.0, 78.0, 267.0, 178.0, 56.0), result.values().take(5))
        assertEquals(44.8, result.sulfur, 0.00001)
    }
    @Test fun `citrus applies the nitrogen based phosphorus and potassium exception`() {
        val lemon = CropNutritionCatalog.profiles.first { it.name == "Limones" }
        val result = CropNutritionCatalog.requirements(lemon, 50.0, 90.0)
        assertEquals(listOf(244.0, 73.0, 244.0, 222.0, 67.0), result.values().take(5))
        assertEquals(53.6, result.sulfur, 0.00001)
    }
    @Test fun `zero yield stays zero and invalid efficiencies are rejected`() {
        val crop = CropNutritionCatalog.profiles.first()
        assertTrue(CropNutritionCatalog.requirements(crop, 0.0, 90.0).values().all { it == 0.0 })
        listOf(0.0, -1.0, 101.0, Double.NaN).forEach { efficiency ->
            assertThrows(IllegalArgumentException::class.java) { CropNutritionCatalog.requirements(crop, 50.0, efficiency) }
        }
    }
    @Test fun `reference catalogs have ten distinct entries with their chemical properties`() {
        assertEquals(10, CropNutritionCatalog.profiles.size)
        assertEquals(10, FertilizerCatalog.materials.map { it.id }.distinct().size)
        assertTrue(FertilizerCatalog.materials.all { it.solubilityGramsL != null && it.solutionPh != null && it.conductivityDsM != null && it.tankGroup != null })
    }
}
