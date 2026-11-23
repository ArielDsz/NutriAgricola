package com.nutriagricola.app.domain.model.program

data class NutritionalProgram(
    val id: String, val name: String, val advisorId: String, val advisorName: String?,
    val species: String?, val plot: String?, val season: String?, val startDate: String,
    val endDate: String?, val active: Boolean, val detailAvailable: Boolean,
    val requirements: List<Double?> = emptyList(),
    val fertilizers: List<ProgramFertilizer> = emptyList(),
    val stages: List<ProgramStage> = emptyList(), val notes: String? = null
) {
    fun contributions(): NutrientAmounts? {
        if (!detailAvailable || fertilizers.isEmpty() || fertilizers.any { it.composition == null }) return null
        return runCatching { ProgramCalculator.contributions(fertilizers.mapIndexed { index, item ->
            FertilizerDose(FertilizerMaterial("saved-$index", item.name, item.composition!!), item.doseKgHa)
        }) }.getOrNull()
    }
}

data class ProgramFertilizer(val name: String, val doseKgHa: Double, val composition: NutrientAmounts?, val notes: String?)
data class ProgramStage(val name: String, val weeks: Int?, val irrigationsPerWeek: Int?, val instructions: String?)
