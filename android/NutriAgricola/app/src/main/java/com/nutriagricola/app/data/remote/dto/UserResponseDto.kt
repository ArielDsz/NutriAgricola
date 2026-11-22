package com.nutriagricola.app.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class UserResponseDto(
    val id: String,
    val run: String,
    val firstName: String,
    val lastName: String,
    val email: String,
    val role: String,
    val active: Boolean,
    val birthDate: String,
    val createdAt: String,
    val phoneNumber: String? = null,
    val region: String? = null,
    val comuna: String? = null
)
