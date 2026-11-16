package com.nutriagricola.app.ui.advisor.detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.nutriagricola.app.data.repository.CropRepository
import com.nutriagricola.app.data.repository.FarmRepository
import com.nutriagricola.app.data.repository.RecordRepository
import com.nutriagricola.app.data.repository.RecordReviewRepository
import com.nutriagricola.app.data.repository.UserRepository
import com.nutriagricola.app.domain.model.user.User

class AdvisorRecordDetailViewModelFactory(
    private val recordId: String,
    private val getCurrentUser: () -> User?,
    private val recordRepository: RecordRepository,
    private val recordReviewRepository: RecordReviewRepository,
    private val farmRepository: FarmRepository,
    private val cropRepository: CropRepository,
    private val userRepository: UserRepository
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        require(modelClass.isAssignableFrom(AdvisorRecordDetailViewModel::class.java)) {
            "Unknown ViewModel class: ${modelClass.name}"
        }
        return AdvisorRecordDetailViewModel(
            recordId,
            getCurrentUser,
            recordRepository,
            recordReviewRepository,
            farmRepository,
            cropRepository,
            userRepository
        ) as T
    }
}
