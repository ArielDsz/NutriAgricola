package com.nutriagricola.app.ui.programs

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.nutriagricola.app.data.repository.ProgramRepository
import com.nutriagricola.app.data.remote.dto.ProgramRequestDto
import com.nutriagricola.app.domain.model.program.NutritionalProgram
import com.nutriagricola.app.domain.model.user.UserRole
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import retrofit2.HttpException
import java.io.IOException

enum class ProgramMode { LIST, CREATE, DETAIL }
data class ProgramsState(
    val programs: List<NutritionalProgram> = emptyList(), val mode: ProgramMode = ProgramMode.LIST,
    val selectedId: String? = null, val draftId: String? = null, val loading: Boolean = false, val saving: Boolean = false,
    val hasMore: Boolean = false, val error: String? = null
)

class ProgramsViewModel(private val userId: String, val role: UserRole, private val repository: ProgramRepository) : ViewModel() {
    private val mutableState = MutableStateFlow(ProgramsState())
    val state = mutableState.asStateFlow()
    private var nextPage = 0
    private var retryRequest: ProgramRequestDto? = null
    init {
        viewModelScope.launch { repository.observePrograms().collect { rows -> mutableState.update { it.copy(programs = rows) } } }
        refresh()
    }

    fun refresh() = download(0)
    fun loadMore() { if (state.value.hasMore) download(nextPage) }
    private fun download(page: Int) {
        if (state.value.loading) return
        mutableState.update { it.copy(loading = true, error = null) }
        viewModelScope.launch {
            try {
                val more = repository.downloadPage(userId, page)
                nextPage = page + 1
                mutableState.update { it.copy(hasMore = more) }
            } catch (error: CancellationException) { throw error
            } catch (error: Exception) { showError(error)
            } finally { mutableState.update { it.copy(loading = false) } }
        }
    }

    fun open(id: String) {
        mutableState.update { it.copy(mode = ProgramMode.DETAIL, selectedId = id, error = null) }
        refreshDetail()
    }
    fun refreshDetail() {
        val id = state.value.selectedId ?: return
        if (state.value.loading) return
        mutableState.update { it.copy(loading = true, error = null) }
        viewModelScope.launch {
            try { repository.downloadDetail(userId, id)
            } catch (error: CancellationException) { throw error
            } catch (error: Exception) { showError(error)
            } finally { mutableState.update { it.copy(loading = false) } }
        }
    }

    fun newProgram() {
        if (role == UserRole.ADVISOR) mutableState.update { it.copy(mode = ProgramMode.CREATE, selectedId = null,
            draftId = java.util.UUID.randomUUID().toString(), error = null) }
    }
    fun back(): Boolean {
        if (state.value.saving) return true
        if (state.value.mode == ProgramMode.LIST) return false
        retryRequest = null
        mutableState.update { it.copy(mode = ProgramMode.LIST, selectedId = null, error = null) }
        return true
    }

    // Un reintento conserva UUID y contenido: una respuesta perdida no crea un duplicado.
    fun save(request: ProgramRequestDto) {
        if (role != UserRole.ADVISOR || state.value.saving) return
        val pending = retryRequest ?: request.also { retryRequest = it }
        mutableState.update { it.copy(saving = true, error = null) }
        viewModelScope.launch {
            try {
                repository.create(userId, pending)
                retryRequest = null
                mutableState.update { it.copy(mode = ProgramMode.DETAIL, selectedId = pending.id) }
            } catch (error: CancellationException) { throw error
            } catch (error: Exception) {
                // Una respuesta 4xx (salvo timeout) confirma rechazo: permite corregir entradas.
                if (error is HttpException && error.code() in 400..499 && error.code() != 408) retryRequest = null
                showError(error)
            } finally { mutableState.update { it.copy(saving = false) } }
        }
    }

    fun hasPendingSave() = retryRequest != null
    private fun showError(error: Exception) {
        val message = when (error) {
            is IOException -> "No se pudo conectar al servidor. Los detalles descargados siguen disponibles."
            is HttpException -> when (error.code()) {
                401 -> "Tu sesión venció. Vuelve a iniciar sesión para conectar."
                403 -> "No tienes permiso para realizar esta acción."
                409 -> "El programa ya existe con datos diferentes. Actualiza la lista para revisarlo."
                400 -> "Revisa los datos del programa."
                else -> "No se pudo completar la operación (${error.code()}). Intenta nuevamente."
            }
            else -> error.message ?: "No se pudo completar la operación."
        }
        mutableState.update { it.copy(error = message) }
    }
}

class ProgramsViewModelFactory(private val userId: String, private val role: UserRole,
                               private val repository: ProgramRepository) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        require(modelClass.isAssignableFrom(ProgramsViewModel::class.java))
        return ProgramsViewModel(userId, role, repository) as T
    }
}
