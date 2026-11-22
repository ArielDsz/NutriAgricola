package com.nutriagricola.app.ui.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nutriagricola.app.data.repository.AuthRepository
import com.nutriagricola.app.data.remote.InvalidUserProfileException
import retrofit2.HttpException
import java.io.IOException
import com.nutriagricola.app.domain.model.user.User
import com.nutriagricola.app.domain.model.user.UserRole
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class LoginUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val authenticatedUser: User? = null
)

class LoginViewModel(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState = _uiState.asStateFlow()

    fun login(email: String, password: String) {
        if (_uiState.value.isLoading || _uiState.value.authenticatedUser != null) return

        if (email.isBlank() || password.isEmpty()) {
            _uiState.value = LoginUiState(error = "Ingresa tu correo y contraseña")
            return
        }

        _uiState.value = LoginUiState(isLoading = true)
        viewModelScope.launch {
            try {
                val user = authRepository.authenticate(email, password)
                _uiState.value = when {
                    user == null -> LoginUiState(error = "Correo o contraseña incorrectos")
                    user.role == UserRole.ADMIN ->
                        LoginUiState(error = "Este perfil no tiene acceso disponible")
                    else -> LoginUiState(authenticatedUser = user)
                }
            } catch (exception: CancellationException) {
                throw exception
            } catch (exception: InvalidUserProfileException) {
                _uiState.value = LoginUiState(error = exception.message)
            } catch (_: IOException) {
                _uiState.value = LoginUiState(error = "No se pudo conectar con el servidor. Inténtalo nuevamente.")
            } catch (exception: HttpException) {
                _uiState.value = LoginUiState(error = if (exception.code() in 500..599)
                    "El servicio no está disponible. Inténtalo más tarde."
                else "No se pudo iniciar sesión. Inténtalo nuevamente.")
            } catch (_: Exception) {
                _uiState.value = LoginUiState(
                    error = "No se pudo iniciar sesión. Inténtalo nuevamente"
                )
            }
        }
    }

    fun onNavigationHandled() {
        _uiState.value = LoginUiState()
    }
}
