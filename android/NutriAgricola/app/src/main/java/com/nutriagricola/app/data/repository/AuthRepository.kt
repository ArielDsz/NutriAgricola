package com.nutriagricola.app.data.repository

import com.nutriagricola.app.data.mapper.toDomain
import com.nutriagricola.app.data.remote.InvalidUserProfileException
import com.nutriagricola.app.data.remote.api.AuthApi
import com.nutriagricola.app.data.remote.dto.LoginRequestDto
import com.nutriagricola.app.domain.model.user.User
import retrofit2.HttpException
import java.util.Locale

class AuthRepository(
    private val authApi: AuthApi,
    private val sessionRepository: SessionRepository
) {
    suspend fun authenticate(email: String, password: String): User? {
        val response = try {
            authApi.login(LoginRequestDto(email.trim().lowercase(Locale.ROOT), password))
        } catch (error: HttpException) {
            if (error.code() == 401) return null
            throw error
        }
        if (response.accessToken.isBlank()) {
            throw InvalidUserProfileException("El servidor no devolvió una sesión válida")
        }
        val user = response.user.toDomain()
        sessionRepository.saveSession(user, response.accessToken)
        return user
    }
}
