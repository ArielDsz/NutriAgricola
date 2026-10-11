package com.nutriagricola.app.ui.theme.farner.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.nutriagricola.app.data.repository.FarmRepository

class FarmerHomeViewModelFactory(
    private val farmRepository: FarmRepository
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return FarmerHomeViewModel(farmRepository) as T
    }
}