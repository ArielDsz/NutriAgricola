package com.nutriagricola.app.ui.farmer.record.soil

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.nutriagricola.app.data.repository.CropRepository

class SoilRecordViewModelFactory(
    private val cropId: String,
    private val cropRepository: CropRepository
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        require(modelClass.isAssignableFrom(SoilRecordViewModel::class.java)) {
            "Unknown ViewModel class: ${modelClass.name}"
        }
        return SoilRecordViewModel(cropId, cropRepository) as T
    }
}
