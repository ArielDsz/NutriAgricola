package com.nutriagricola.app.ui.farmer

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.nutriagricola.app.data.repository.FarmRepository

class FarmerContextViewModelFactory(
    private val farmRepository: FarmRepository
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        require(modelClass.isAssignableFrom(FarmerContextViewModel::class.java)) {
            "Unknown ViewModel class: ${modelClass.name}"
        }
        return FarmerContextViewModel(farmRepository) as T
    }
}
