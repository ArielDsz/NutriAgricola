package com.nutriagricola.app.ui.records

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.nutriagricola.app.data.repository.RecordRemoteRepository
import com.nutriagricola.app.data.repository.RecordRepository
import com.nutriagricola.app.data.sync.RecordUploadScheduler

class RecordDownloadViewModelFactory(
    private val repository: RecordRemoteRepository,
    private val records: RecordRepository,
    private val uploads: RecordUploadScheduler,
    private val canUpload: Boolean,
    private val userId: String,
    private val onSessionExpired: () -> Unit
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        require(modelClass.isAssignableFrom(RecordDownloadViewModel::class.java))
        return RecordDownloadViewModel(repository, records, uploads, canUpload, userId, onSessionExpired) as T
    }
}
