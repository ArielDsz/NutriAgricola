package com.nutriagricola.app.data.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.nutriagricola.app.data.mapper.toDomain
import com.nutriagricola.app.data.remote.InvalidUserProfileException
import com.nutriagricola.app.data.remote.api.AuthApi
import com.nutriagricola.app.domain.model.user.User
import com.nutriagricola.app.domain.model.user.UserRole
import kotlinx.coroutines.flow.first
import retrofit2.HttpException
import java.io.IOException

private val Context.sessionDataStore by preferencesDataStore(name = "user_session")

class SessionRepository(
    private val dataStore: DataStore<Preferences>,
    private val userRepository: UserRepository,
    private val authApi: AuthApi
) {
    constructor(context: Context, userRepository: UserRepository, authApi: AuthApi) :
            this(context.applicationContext.sessionDataStore, userRepository, authApi)

    private val userIdKey = stringPreferencesKey("userId")
    private val accessTokenKey = stringPreferencesKey("accessToken")

    suspend fun saveSession(user: User, accessToken: String) {
        require(accessToken.isNotBlank()) { "El token no puede estar vacío" }
        require(user.active && user.role in listOf(UserRole.FARMER, UserRole.ADVISOR))
        userRepository.upsert(user)
        dataStore.edit { preferences ->
            preferences[userIdKey] = user.id
            preferences[accessTokenKey] = accessToken
        }
    }

    suspend fun getAccessToken(): String? =
        dataStore.data.first()[accessTokenKey]?.takeIf { it.isNotBlank() }

    suspend fun hasSession(userId: String): Boolean {
        val preferences = dataStore.data.first()
        return preferences[userIdKey] == userId && !preferences[accessTokenKey].isNullOrBlank()
    }

    suspend fun getSavedUser(): User? {
        val preferences = dataStore.data.first()
        val userId = preferences[userIdKey]
        val token = preferences[accessTokenKey]
        if (userId == null || token.isNullOrBlank()) {
            clearSession()
            return null
        }
        val cachedUser = userRepository.getById(userId)
        if (cachedUser == null || !cachedUser.active || cachedUser.role !in listOf(UserRole.FARMER, UserRole.ADVISOR)) {
            clearSession()
            return null
        }
        val remoteUser = try {
            authApi.getCurrentUser("Bearer $token").toDomain()
        } catch (_: IOException) {
            return cachedUser
        } catch (error: HttpException) {
            if (error.code() in 500..599) return cachedUser
            if (error.code() == 401 || error.code() == 403) {
                clearSession()
                return null
            }
            throw error
        } catch (_: InvalidUserProfileException) {
            clearSession()
            return null
        }
        if (remoteUser.id != userId) {
            clearSession()
            return null
        }
        userRepository.upsert(remoteUser)
        return remoteUser
    }

    suspend fun clearSession() {
        dataStore.edit { preferences ->
            preferences.remove(userIdKey)
            preferences.remove(accessTokenKey)
        }
    }
}
