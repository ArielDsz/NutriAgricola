package com.nutriagricola.app.ui.farmer.record.soil

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.nutriagricola.app.data.repository.CropRepository
import com.nutriagricola.app.data.repository.RecordRepository
import com.nutriagricola.app.domain.model.user.User

class SoilRecordViewModelFactory(
    private val cropId: String,
    private val cropRepository: CropRepository,
    private val recordRepository: RecordRepository,
    private val getCurrentUser: () -> User?
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        require(modelClass.isAssignableFrom(SoilRecordViewModel::class.java)) {
            "Unknown ViewModel class: ${modelClass.name}"
        }
        return SoilRecordViewModel(cropId, cropRepository, recordRepository, getCurrentUser) as T
    }
}
