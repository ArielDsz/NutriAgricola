package com.nutriagricola.app.ui.session

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.nutriagricola.app.data.repository.SessionRepository

class SessionViewModelFactory(
    private val sessionRepository: SessionRepository
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        require(modelClass.isAssignableFrom(SessionViewModel::class.java)) {
            "Unknown ViewModel class: ${modelClass.name}"
        }
        return SessionViewModel(sessionRepository) as T
    }
}
