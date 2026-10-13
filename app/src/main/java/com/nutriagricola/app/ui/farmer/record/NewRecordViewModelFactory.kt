package com.nutriagricola.app.ui.farmer.record

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.nutriagricola.app.data.repository.CropRepository

class NewRecordViewModelFactory(
    private val cropRepository: CropRepository
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return NewRecordViewModel(
            cropRepository
        ) as T
    }
}