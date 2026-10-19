package com.nutriagricola.app.ui.advisor.pending

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nutriagricola.app.data.repository.CropRepository
import com.nutriagricola.app.data.repository.FarmRepository
import com.nutriagricola.app.data.repository.RecordRepository
import com.nutriagricola.app.data.repository.UserRepository
import com.nutriagricola.app.domain.model.record.RecordStatus
import com.nutriagricola.app.domain.model.record.RecordType
import com.nutriagricola.app.ui.advisor.model.AdvisorRecordItem
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class AdvisorPendingRecordsUiState(
    val pendingRecords: List<AdvisorRecordItem> = emptyList(),
    val filteredRecords: List<AdvisorRecordItem> = emptyList(),
    val selectedTypeFilter: RecordType? = null,
    val searchQuery: String = "",
    val isLoading: Boolean = true,
    val error: String? = null
)

class AdvisorPendingRecordsViewModel(
    private val recordRepository: RecordRepository,
    private val farmRepository: FarmRepository,
    private val cropRepository: CropRepository,
    private val userRepository: UserRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(AdvisorPendingRecordsUiState())
    val uiState: StateFlow<AdvisorPendingRecordsUiState> = _uiState.asStateFlow()

    init {
        loadData()
    }

    fun loadData() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            try {
                val pendingRecords = recordRepository.getByStatus(RecordStatus.PENDING)
                val farms = farmRepository.getAll()
                val crops = farms.flatMap { cropRepository.getByFarmId(it.id) }.associateBy { it.id }
                val users = userRepository.getAll().associateBy { it.id }
                val farmsMap = farms.associateBy { it.id }

                val items = pendingRecords.map { record ->
                    val crop = crops[record.cropId]
                    val farmName = crop?.let { farmsMap[it.farmId]?.name } ?: "Predio"
                    val cropName = crop?.name ?: "Cultivo"
                    val farmerName = users[record.userId]?.let { "${it.firstName} ${it.lastName}" } ?: "Agricultor"
                    AdvisorRecordItem(
                        record = record,
                        farmName = farmName,
                        cropName = cropName,
                        farmerName = farmerName
                    )
                }

                _uiState.update {
                    it.copy(
                        pendingRecords = items,
                        isLoading = false
                    )
                }
                applyFilters()
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = "No se pudieron cargar los análisis pendientes"
                    )
                }
            }
        }
    }

    fun setTypeFilter(type: RecordType?) {
        _uiState.update { it.copy(selectedTypeFilter = type) }
        applyFilters()
    }

    fun setSearchQuery(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
        applyFilters()
    }

    private fun applyFilters() {
        val current = _uiState.value
        val filtered = current.pendingRecords.filter { item ->
            val matchesType = current.selectedTypeFilter == null || item.record.type == current.selectedTypeFilter
            val query = current.searchQuery.trim().lowercase()
            val matchesQuery = query.isEmpty() ||
                    item.farmName.lowercase().contains(query) ||
                    item.cropName.lowercase().contains(query) ||
                    item.farmerName.lowercase().contains(query)
            matchesType && matchesQuery
        }
        _uiState.update { it.copy(filteredRecords = filtered) }
    }
}
