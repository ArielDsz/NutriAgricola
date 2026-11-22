package com.nutriagricola.app.data.mapper

import com.nutriagricola.app.data.remote.InvalidUserProfileException
import com.nutriagricola.app.data.remote.dto.UserResponseDto
import com.nutriagricola.app.domain.model.user.User
import com.nutriagricola.app.domain.model.user.UserRole
import java.util.UUID

fun UserResponseDto.toDomain(): User {
    val validId = try {
        UUID.fromString(id).toString() == id
    } catch (_: IllegalArgumentException) {
        false
    }
    if (!validId || email.isBlank()) {
        throw InvalidUserProfileException("El servidor devolvió un perfil inválido")
    }
    if (!active) {
        throw InvalidUserProfileException("La cuenta no está activa")
    }
    val userRole = when (role) {
        "FARMER" -> UserRole.FARMER
        "ADVISOR" -> UserRole.ADVISOR
        else -> throw InvalidUserProfileException("Este perfil no tiene acceso disponible")
    }
    return User(id, firstName, lastName, email, userRole, active)
}
