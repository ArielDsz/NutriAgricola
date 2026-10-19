package com.nutriagricola.app.ui.advisor.all_records

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.nutriagricola.app.data.repository.CropRepository
import com.nutriagricola.app.data.repository.FarmRepository
import com.nutriagricola.app.data.repository.RecordRepository
import com.nutriagricola.app.data.repository.UserRepository

class AdvisorAllRecordsViewModelFactory(
    private val recordRepository: RecordRepository,
    private val farmRepository: FarmRepository,
    private val cropRepository: CropRepository,
    private val userRepository: UserRepository
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        require(modelClass.isAssignableFrom(AdvisorAllRecordsViewModel::class.java)) {
            "Unknown ViewModel class: ${modelClass.name}"
        }
        return AdvisorAllRecordsViewModel(
            recordRepository,
            farmRepository,
            cropRepository,
            userRepository
        ) as T
    }
}
