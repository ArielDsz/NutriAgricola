package com.nutriagricola.app.ui.farmer

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.nutriagricola.app.data.repository.FarmRepository
import com.nutriagricola.app.data.repository.FarmerPreferencesRepository

class FarmerContextViewModelFactory(
    private val farmRepository: FarmRepository,
    private val preferencesRepository: FarmerPreferencesRepository,
    private val userId: String
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        require(modelClass.isAssignableFrom(FarmerContextViewModel::class.java)) {
            "Unknown ViewModel class: ${modelClass.name}"
        }
        require(userId.isNotBlank()) { "An authenticated user is required" }
        return FarmerContextViewModel(farmRepository, preferencesRepository, userId) as T
    }
}
