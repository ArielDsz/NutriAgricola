package com.nutriagricola.app.ui.farmer.crop.detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.nutriagricola.app.data.repository.CropRepository
import com.nutriagricola.app.data.repository.RecordRepository
import com.nutriagricola.app.domain.model.farm.Farm
import kotlinx.coroutines.flow.Flow

class CropDetailViewModelFactory(
    private val cropId: String,
    private val userId: String,
    private val selectedFarm: Flow<Farm?>,
    private val cropRepository: CropRepository,
    private val recordRepository: RecordRepository
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        require(modelClass.isAssignableFrom(CropDetailViewModel::class.java)) {
            "Unknown ViewModel class: ${modelClass.name}"
        }
        return CropDetailViewModel(
            cropId, userId, selectedFarm, cropRepository, recordRepository
        ) as T
    }
}
