package com.nutriagricola.app.data.repository

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import java.io.IOException
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first

private val Context.farmerPreferences by preferencesDataStore(name = "farmer_preferences")

class FarmerPreferencesRepository(context: Context) {
    private val dataStore = context.applicationContext.farmerPreferences

    suspend fun getSelectedFarmId(userId: String): String? = dataStore.data
        .catch { exception ->
            if (exception is IOException) emit(emptyPreferences()) else throw exception
        }
        .first()[farmKey(userId)]

    suspend fun saveSelectedFarmId(userId: String, farmId: String) {
        dataStore.edit { it[farmKey(userId)] = farmId }
    }

    private fun farmKey(userId: String) = stringPreferencesKey("selectedFarmId_$userId")
}
