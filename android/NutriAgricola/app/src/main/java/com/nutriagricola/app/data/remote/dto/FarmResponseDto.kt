package com.nutriagricola.app.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class FarmResponseDto(
    val id: String,
    val name: String,
    val location: String? = null,
    val imagePath: String? = null,
    val active: Boolean
)
