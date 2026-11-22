package com.nutriagricola.app.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class RecordPageDto(val content: List<RecordSummaryDto>, val page: Int, val size: Int,
                         val totalElements: Long, val totalPages: Int)

@Serializable
data class RecordSummaryDto(val id: String, val cropId: String, val userId: String,
                            val date: String, val type: String, val status: String,
                            val observations: String? = null)

@Serializable
data class RecordDetailDto(
    val id: String, val cropId: String, val userId: String, val date: String,
    val type: String, val status: String, val observations: String? = null,
    val soilData: SoilDataDto? = null, val waterData: WaterDataDto? = null,
    val foliarData: FoliarDataDto? = null, val foliarPhotos: List<PhotoResponseDto> = emptyList(),
    val reviews: List<ReviewResponseDto> = emptyList()
)

@Serializable
data class SoilDataDto(val ph: Double? = null, val electricalConductivity: Double? = null,
    val nitrogen: Double? = null, val phosphorus: Double? = null, val potassium: Double? = null,
    val calcium: Double? = null, val magnesium: Double? = null, val organicMatter: Double? = null)

@Serializable
data class WaterDataDto(val ph: Double? = null, val electricalConductivity: Double? = null,
    val nitrogen: Double? = null, val phosphorus: Double? = null, val potassium: Double? = null,
    val calcium: Double? = null, val magnesium: Double? = null, val waterSource: String? = null)

@Serializable
data class FoliarDataDto(val nitrogen: Double? = null, val phosphorus: Double? = null,
    val potassium: Double? = null, val calcium: Double? = null, val magnesium: Double? = null,
    val symptoms: String? = null)

@Serializable
data class PhotoResponseDto(val id: String, val path: String, val uploaded: Boolean,
    val contentType: String? = null, val sizeBytes: Long? = null, val downloadUrl: String? = null)

@Serializable
data class ReviewResponseDto(val id: String, val advisorId: String, val advisorName: String,
    val date: String, val result: String, val comment: String)
