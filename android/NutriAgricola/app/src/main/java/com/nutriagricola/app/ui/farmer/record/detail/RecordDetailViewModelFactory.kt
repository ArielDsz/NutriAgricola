package com.nutriagricola.app.ui.farmer.record.detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.nutriagricola.app.data.repository.CropRepository
import com.nutriagricola.app.data.repository.RecordRepository
import com.nutriagricola.app.domain.model.farm.Farm
import com.nutriagricola.app.domain.model.user.User
import kotlinx.coroutines.flow.Flow

class RecordDetailViewModelFactory(
    private val recordId: String,
    private val recordRepository: RecordRepository,
    private val cropRepository: CropRepository,
    private val getCurrentUser: () -> User?,
    private val selectedFarm: Flow<Farm?>
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        require(modelClass.isAssignableFrom(RecordDetailViewModel::class.java)) {
            "Unknown ViewModel class: ${modelClass.name}"
        }
        return RecordDetailViewModel(
            recordId, recordRepository, cropRepository, getCurrentUser, selectedFarm
        ) as T
    }
}
