package com.nutriagricola.app.data.mapper

import com.nutriagricola.app.data.remote.dto.*
import com.nutriagricola.app.domain.model.program.*

fun ProgramNutrientsDto.toValues() = listOf(nitrogen, phosphorusOxide, potassiumOxide, calciumOxide, magnesiumOxide, sulfur)
fun ProgramNutrientsDto.toAmountsOrNull(): NutrientAmounts? {
    val values = toValues()
    if (values.any { it == null || !it.isFinite() || it < 0 }) return null
    return NutrientAmounts(values[0]!!, values[1]!!, values[2]!!, values[3]!!, values[4]!!, values[5]!!)
}
fun NutrientAmounts.toDto() = ProgramNutrientsDto(nitrogen, phosphorusOxide, potassiumOxide, calciumOxide, magnesiumOxide, sulfur)

fun ProgramResponseDto.toSummary() = ProgramSummaryDto(id, advisorId, content.name, content.startDate,
    active, content.season, content.endDate, species, plot)

fun ProgramSummaryDto.toDomain(detail: ProgramResponseDto? = null) = NutritionalProgram(
    id = id, name = name, advisorId = advisorId, advisorName = detail?.advisorName,
    species = species, plot = plot, season = season, startDate = startDate, endDate = endDate,
    active = active, detailAvailable = detail != null,
    requirements = detail?.content?.nutrients?.toValues().orEmpty(),
    fertilizers = detail?.content?.fertilizers?.map {
        ProgramFertilizer(it.name, it.doseKgHa, it.composition?.toAmountsOrNull(), it.notes)
    }.orEmpty(),
    stages = detail?.content?.stages?.map { ProgramStage(it.name, it.weeks, it.irrigationsPerWeek, it.instructions) }.orEmpty(),
    notes = detail?.content?.irrigationNotes
)
