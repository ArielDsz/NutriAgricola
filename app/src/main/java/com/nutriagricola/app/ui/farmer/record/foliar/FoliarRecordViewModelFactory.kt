package com.nutriagricola.app.ui.farmer.record.foliar

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.nutriagricola.app.data.repository.CropRepository
import com.nutriagricola.app.data.repository.RecordRepository
import com.nutriagricola.app.domain.model.user.User

class FoliarRecordViewModelFactory(
    private val cropId: String,
    private val cropRepository: CropRepository,
    private val recordRepository: RecordRepository,
    private val getCurrentUser: () -> User?
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        require(modelClass.isAssignableFrom(FoliarRecordViewModel::class.java)) {
            "Unknown ViewModel class: ${modelClass.name}"
        }
        return FoliarRecordViewModel(cropId, cropRepository, recordRepository, getCurrentUser) as T
    }
}
