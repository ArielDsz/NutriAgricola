package com.nutriagricola.app.domain.model.program

/** kg/ha de N, P2O5, K2O, CaO, MgO y S; no son concentraciones de laboratorio. */
data class NutrientAmounts(
    val nitrogen: Double = 0.0,
    val phosphorusOxide: Double = 0.0,
    val potassiumOxide: Double = 0.0,
    val calciumOxide: Double = 0.0,
    val magnesiumOxide: Double = 0.0,
    val sulfur: Double = 0.0
) {
    fun values() = listOf(nitrogen, phosphorusOxide, potassiumOxide, calciumOxide, magnesiumOxide, sulfur)
}

data class FertilizerMaterial(val id: String, val name: String, val percentages: NutrientAmounts)
data class FertilizerDose(val material: FertilizerMaterial, val kgHa: Double)

/** Catálogo de referencia de https://nutrixpert.netlify.app/, revisado el 22-11-2026.
 * Reproduce sus composiciones; no certifica la idoneidad agronómica de una mezcla.
 */
object FertilizerCatalog {
    val materials = listOf(
        FertilizerMaterial("map", "Fosfato monoamónico (MAP)", NutrientAmounts(12.0, 60.0)),
        FertilizerMaterial("calcium_nitrate", "Nitrato de calcio", NutrientAmounts(nitrogen = 16.0, calciumOxide = 27.0)),
        FertilizerMaterial("magnesium_sulfate", "Sulfato de magnesio heptahidratado", NutrientAmounts(magnesiumOxide = 16.0, sulfur = 13.0)),
        FertilizerMaterial("potassium_nitrate", "Nitrato de potasio", NutrientAmounts(nitrogen = 13.0, potassiumOxide = 45.0)),
        FertilizerMaterial("smartblue", "SmartBlue + N2 Stabil", NutrientAmounts(nitrogen = 46.0)),
        FertilizerMaterial("phosphoric_acid", "Ácido fosfórico 85%", NutrientAmounts(phosphorusOxide = 52.0)),
        FertilizerMaterial("potassium_sulfate", "Sulfato de potasio", NutrientAmounts(potassiumOxide = 51.0, sulfur = 17.0)),
        FertilizerMaterial("ammonium_nitrate", "Nitrato de amonio", NutrientAmounts(nitrogen = 34.0)),
        FertilizerMaterial("ammonium_sulfate", "Sulfato de amonio", NutrientAmounts(nitrogen = 21.0, sulfur = 22.0)),
        FertilizerMaterial("magnesium_nitrate", "Nitrato de magnesio", NutrientAmounts(nitrogen = 12.0, magnesiumOxide = 15.0))
    )
}

object ProgramCalculator {
    /** Receta fija de cinco productos de la referencia; no es un optimizador. */
    fun suggest(requirements: NutrientAmounts): List<FertilizerDose> {
        require(requirements.values().all { it.isFinite() && it >= 0 })
        val materials = FertilizerCatalog.materials
        val doses = listOf(
            requirements.phosphorusOxide / 0.60,
            requirements.calciumOxide / 0.27,
            requirements.magnesiumOxide / 0.16,
            requirements.potassiumOxide / 0.45
        )
        val secondaryNitrogen = doses[0] * 0.12 + doses[1] * 0.16 + doses[3] * 0.13
        val nitrogenDose = (requirements.nitrogen - secondaryNitrogen).coerceAtLeast(0.0) / 0.46
        return (doses + nitrogenDose).mapIndexed { index, dose ->
            require(dose.isFinite()) { "Los valores son demasiado grandes" }
            FertilizerDose(materials[index], dose)
        }
    }

    fun contributions(doses: List<FertilizerDose>): NutrientAmounts {
        require(doses.all { it.kgHa.isFinite() && it.kgHa >= 0 })
        val totals = (0..5).map { nutrient ->
            doses.sumOf { it.kgHa * (it.material.percentages.values()[nutrient] / 100.0) }
                .also { require(it.isFinite()) { "Los valores son demasiado grandes" } }
        }
        return NutrientAmounts(totals[0], totals[1], totals[2], totals[3], totals[4], totals[5])
    }
}
