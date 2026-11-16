package com.nutriagricola.app.ui.farmer.record.water

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.nutriagricola.app.data.repository.CropRepository
import com.nutriagricola.app.data.repository.RecordRepository
import com.nutriagricola.app.domain.model.user.User

class WaterRecordViewModelFactory(
    private val cropId: String,
    private val cropRepository: CropRepository,
    private val recordRepository: RecordRepository,
    private val getCurrentUser: () -> User?
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        require(modelClass.isAssignableFrom(WaterRecordViewModel::class.java)) {
            "Unknown ViewModel class: ${modelClass.name}"
        }
        return WaterRecordViewModel(cropId, cropRepository, recordRepository, getCurrentUser) as T
    }
}
