package com.nutriagricola.app.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class ProgramNutrientsDto(
    val nitrogen: Double? = null, val phosphorusOxide: Double? = null,
    val potassiumOxide: Double? = null, val calciumOxide: Double? = null,
    val magnesiumOxide: Double? = null, val sulfur: Double? = null
)

@Serializable
data class ProgramFertilizerDto(val name: String, val doseKgHa: Double, val notes: String? = null,
                                 val composition: ProgramNutrientsDto? = null)
@Serializable
data class ProgramStageDto(val name: String, val weeks: Int? = null,
                            val irrigationsPerWeek: Int? = null, val instructions: String? = null)
@Serializable
data class ProgramContentDto(
    val name: String, val startDate: String, val nutrients: ProgramNutrientsDto,
    val fertilizers: List<ProgramFertilizerDto>, val stages: List<ProgramStageDto>,
    val season: String? = null, val endDate: String? = null, val targetYieldTonHa: Double? = null,
    val irrigationEfficiencyPercent: Double? = null, val irrigationNotes: String? = null
)
@Serializable
data class ProgramRequestDto(val id: String, val species: String?, val plot: String?, val content: ProgramContentDto)
@Serializable
data class ProgramSummaryDto(
    val id: String, val advisorId: String, val name: String, val startDate: String, val active: Boolean,
    val season: String? = null, val endDate: String? = null, val species: String? = null, val plot: String? = null
)
@Serializable
data class ProgramResponseDto(
    val id: String, val advisorId: String, val advisorName: String, val content: ProgramContentDto,
    val active: Boolean, val createdAt: String, val species: String? = null, val plot: String? = null
)
@Serializable
data class ProgramPageDto(val content: List<ProgramSummaryDto>, val page: Int, val size: Int,
                           val totalElements: Long, val totalPages: Int)
