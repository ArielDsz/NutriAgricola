package com.nutriagricola.app.data.mapper

import com.nutriagricola.app.data.local.entity.UserEntity
import com.nutriagricola.app.domain.model.user.User
import com.nutriagricola.app.domain.model.user.UserRole

fun UserEntity.toDomain(): User {
    return User(
        id = id,
        firstName = firstName,
        lastName = lastName,
        email = email,
        role = UserRole.valueOf(role),
        active = active
    )
}

fun User.toEntity(): UserEntity {
    return UserEntity(
        id = id,
        firstName = firstName,
        lastName = lastName,
        email = email,
        role = role.name,
        active = active
    )
}