package com.nutriagricola.app.domain.model.user

import java.util.UUID

data class User(
    val id: String = UUID.randomUUID().toString(),
    val firstName: String,
    val lastName: String,
    val email: String,
    val role: UserRole,
    val active: Boolean
)