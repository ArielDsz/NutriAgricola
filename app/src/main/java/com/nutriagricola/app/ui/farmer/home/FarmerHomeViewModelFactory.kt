package com.nutriagricola.app.ui.farmer.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.nutriagricola.app.data.repository.CropRepository
import com.nutriagricola.app.data.repository.FarmRepository

class FarmerHomeViewModelFactory(
    private val farmRepository: FarmRepository,
    private val cropRepository: CropRepository
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return FarmerHomeViewModel(farmRepository, cropRepository) as T
    }
}