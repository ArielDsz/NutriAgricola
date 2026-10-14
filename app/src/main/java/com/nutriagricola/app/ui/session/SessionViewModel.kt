package com.nutriagricola.app.ui.session

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nutriagricola.app.data.repository.SessionRepository
import com.nutriagricola.app.domain.model.user.User
import com.nutriagricola.app.domain.model.user.UserRole
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class SessionUiState(
    val currentUser: User? = null,
    val isRestored: Boolean = false,
    val isBusy: Boolean = false,
    val error: String? = null
)

class SessionViewModel(
    private val sessionRepository: SessionRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(SessionUiState())
    val uiState = _uiState.asStateFlow()

    suspend fun restoreSession() {
        if (_uiState.value.isRestored) return

        try {
            val user = sessionRepository.getSavedUser()
            val validUser = user?.takeIf { isSupportedUser(it) }
            if (validUser == null) {
                sessionRepository.clearUserId()
            }
            _uiState.value = SessionUiState(currentUser = validUser, isRestored = true)
        } catch (exception: CancellationException) {
            throw exception
        } catch (_: Exception) {
            _uiState.value = SessionUiState(
                error = "No se pudo recuperar la sesión. Reinicia la aplicación."
            )
        }
    }

    fun createSession(user: User) {
        val state = _uiState.value
        if (!state.isRestored || state.isBusy || state.currentUser != null) return
        if (!isSupportedUser(user)) {
            _uiState.value = state.copy(error = "Este perfil no tiene acceso disponible")
            return
        }

        _uiState.value = state.copy(isBusy = true, error = null)
        viewModelScope.launch {
            try {
                sessionRepository.saveUserId(user.id)
                _uiState.value = SessionUiState(currentUser = user, isRestored = true)
            } catch (exception: CancellationException) {
                throw exception
            } catch (_: Exception) {
                _uiState.value = state.copy(
                    error = "No se pudo guardar la sesión. Vuelve a iniciar sesión."
                )
            }
        }
    }

    fun logout() {
        val state = _uiState.value
        if (!state.isRestored || state.isBusy || state.currentUser == null) return

        _uiState.value = state.copy(isBusy = true, error = null)
        viewModelScope.launch {
            try {
                sessionRepository.clearUserId()
                _uiState.value = SessionUiState(isRestored = true)
            } catch (exception: CancellationException) {
                throw exception
            } catch (_: Exception) {
                _uiState.value = state.copy(
                    error = "No se pudo cerrar la sesión. Inténtalo nuevamente."
                )
            }
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }

    private fun isSupportedUser(user: User): Boolean {
        return user.active && (user.role == UserRole.FARMER || user.role == UserRole.ADVISOR)
    }
}
