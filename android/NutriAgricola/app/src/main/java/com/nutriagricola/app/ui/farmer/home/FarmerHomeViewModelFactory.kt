package com.nutriagricola.app.ui.farmer.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.nutriagricola.app.data.repository.CropRepository
import com.nutriagricola.app.data.repository.RecordRepository
import com.nutriagricola.app.domain.model.farm.Farm
import kotlinx.coroutines.flow.Flow

class FarmerHomeViewModelFactory(
    private val userId: String,
    private val selectedFarm: Flow<Farm?>,
    private val cropRepository: CropRepository,
    private val recordRepository: RecordRepository
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        require(modelClass.isAssignableFrom(FarmerHomeViewModel::class.java)) {
            "Unknown ViewModel class: ${modelClass.name}"
        }
        return FarmerHomeViewModel(userId, selectedFarm, cropRepository, recordRepository) as T
    }
}
