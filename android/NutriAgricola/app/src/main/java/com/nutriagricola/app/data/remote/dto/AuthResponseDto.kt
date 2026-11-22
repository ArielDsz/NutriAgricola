package com.nutriagricola.app.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class AuthResponseDto(
    val accessToken: String,
    val user: UserResponseDto
)
