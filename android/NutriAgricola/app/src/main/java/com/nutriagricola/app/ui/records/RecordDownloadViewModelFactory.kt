package com.nutriagricola.app.ui.records

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.nutriagricola.app.data.repository.RecordRemoteRepository

class RecordDownloadViewModelFactory(
    private val repository: RecordRemoteRepository,
    private val userId: String,
    private val onSessionExpired: () -> Unit
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        require(modelClass.isAssignableFrom(RecordDownloadViewModel::class.java))
        return RecordDownloadViewModel(repository, userId, onSessionExpired) as T
    }
}
