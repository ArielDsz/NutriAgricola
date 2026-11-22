package com.nutriagricola.app.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class RecordCreateDto(val id: String, val cropId: String, val date: String, val type: String,
    val observations: String? = null, val soilData: SoilDataDto? = null,
    val waterData: WaterDataDto? = null, val foliarData: FoliarDataDto? = null,
    val foliarPhotos: List<PhotoCreateDto> = emptyList())

@Serializable
data class PhotoCreateDto(val id: String, val path: String)

@Serializable
data class RecordUpdateDto(val date: String, val observations: String? = null,
    val soilData: SoilDataDto? = null, val waterData: WaterDataDto? = null,
    val foliarData: FoliarDataDto? = null)

@Serializable
data class RecordCreateResponseDto(val id: String, val status: String)
