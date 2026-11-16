package com.nutriagricola.app.data.repository

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.nutriagricola.app.domain.model.user.User
import kotlinx.coroutines.flow.first

private val Context.sessionDataStore by preferencesDataStore(name = "user_session")

class SessionRepository(
    context: Context,
    private val userRepository: UserRepository
) {
    private val dataStore = context.applicationContext.sessionDataStore
    private val userIdKey = stringPreferencesKey("userId")

    suspend fun getSavedUser(): User? {
        val userId = dataStore.data.first()[userIdKey] ?: return null
        return userRepository.getById(userId)
    }

    suspend fun saveUserId(userId: String) {
        dataStore.edit { preferences ->
            preferences[userIdKey] = userId
        }
    }

    suspend fun clearUserId() {
        dataStore.edit { preferences ->
            preferences.remove(userIdKey)
        }
    }
}
