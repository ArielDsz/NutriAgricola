package com.nutriagricola.app.domain.model.program

import kotlin.math.floor

data class CropNutritionProfile(val name: String, val baseYieldTonHa: Double,
                                val base: NutrientAmounts, val citrus: Boolean = false)

/** Valores y regla de cálculo del código público de NutriXpert, 23/11/2026. */
object CropNutritionCatalog {
    val profiles = listOf(
        CropNutritionProfile("Uva de mesa", 50.0, NutrientAmounts(120.0, 70.0, 240.0, 160.0, 50.0)),
        CropNutritionProfile("Uva vinífera", 25.0, NutrientAmounts(110.0, 50.0, 150.0, 110.0, 35.0)),
        CropNutritionProfile("Cerezo", 18.0, NutrientAmounts(130.0, 60.0, 160.0, 150.0, 45.0)),
        CropNutritionProfile("Avellano europeo", 5.0, NutrientAmounts(120.0, 45.0, 110.0, 100.0, 30.0)),
        CropNutritionProfile("Nogal", 7.0, NutrientAmounts(160.0, 70.0, 180.0, 170.0, 50.0)),
        CropNutritionProfile("Clementinas / Mandarinos", 50.0, NutrientAmounts(240.0, 70.0, 240.0, 180.0, 50.0), true),
        CropNutritionProfile("Naranjos", 50.0, NutrientAmounts(260.0, 80.0, 260.0, 210.0, 60.0), true),
        CropNutritionProfile("Limones", 50.0, NutrientAmounts(220.0, 65.0, 220.0, 200.0, 60.0), true),
        CropNutritionProfile("Arándanos", 22.0, NutrientAmounts(110.0, 40.0, 100.0, 60.0, 25.0)),
        CropNutritionProfile("Palto (Avocado)", 28.0, NutrientAmounts(220.0, 65.0, 250.0, 140.0, 55.0))
    )

    fun requirements(profile: CropNutritionProfile, yieldTonHa: Double, efficiencyPercent: Double): NutrientAmounts {
        require(yieldTonHa.isFinite() && yieldTonHa >= 0) { "Revisa el rendimiento" }
        require(efficiencyPercent.isFinite() && efficiencyPercent > 0 && efficiencyPercent <= 100) { "La eficiencia debe estar entre 0 y 100, sin incluir 0" }
        val factor = yieldTonHa / profile.baseYieldTonHa / (efficiencyPercent / 100)
        val base = profile.base
        val values = listOf(base.nitrogen, if (profile.citrus) base.nitrogen * 0.3 else base.phosphorusOxide,
            if (profile.citrus) base.nitrogen else base.potassiumOxide, base.calciumOxide, base.magnesiumOxide)
            .map { floor(it * factor + 0.5).also { value -> require(value.isFinite()) { "Los valores son demasiado grandes" } } }
        return NutrientAmounts(values[0], values[1], values[2], values[3], values[4], values[4] * 0.8)
    }
}
