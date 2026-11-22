package com.nutriagricola.app.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class CropResponseDto(
    val id: String,
    val farmId: String,
    val name: String,
    val variety: String? = null,
    val areaHectares: Double? = null,
    val phenologicalStage: String? = null,
    val imagePath: String? = null,
    val active: Boolean
)
