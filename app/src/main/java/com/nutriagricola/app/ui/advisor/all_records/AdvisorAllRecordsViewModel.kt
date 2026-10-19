package com.nutriagricola.app.ui.advisor.all_records

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nutriagricola.app.data.repository.CropRepository
import com.nutriagricola.app.data.repository.FarmRepository
import com.nutriagricola.app.data.repository.RecordRepository
import com.nutriagricola.app.data.repository.UserRepository
import com.nutriagricola.app.domain.model.farm.Farm
import com.nutriagricola.app.domain.model.record.RecordStatus
import com.nutriagricola.app.domain.model.record.RecordType
import com.nutriagricola.app.ui.advisor.model.AdvisorRecordItem
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class AdvisorAllRecordsUiState(
    val allRecords: List<AdvisorRecordItem> = emptyList(),
    val filteredRecords: List<AdvisorRecordItem> = emptyList(),
    val farms: List<Farm> = emptyList(),
    val selectedFarmId: String? = null,
    val selectedStatusFilter: RecordStatus? = null,
    val selectedTypeFilter: RecordType? = null,
    val searchQuery: String = "",
    val isLoading: Boolean = true,
    val error: String? = null
)

class AdvisorAllRecordsViewModel(
    private val recordRepository: RecordRepository,
    private val farmRepository: FarmRepository,
    private val cropRepository: CropRepository,
    private val userRepository: UserRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(AdvisorAllRecordsUiState())
    val uiState: StateFlow<AdvisorAllRecordsUiState> = _uiState.asStateFlow()

    init {
        loadData()
    }

    fun loadData() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            try {
                val records = recordRepository.getAll()
                val farms = farmRepository.getAll()
                val crops = farms.flatMap { cropRepository.getByFarmId(it.id) }.associateBy { it.id }
                val users = userRepository.getAll().associateBy { it.id }
                val farmsMap = farms.associateBy { it.id }

                val items = records.map { record ->
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
                        allRecords = items,
                        farms = farms,
                        isLoading = false
                    )
                }
                applyFilters()
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = "No se pudieron cargar los registros de los predios"
                    )
                }
            }
        }
    }

    fun setStatusFilter(status: RecordStatus?) {
        _uiState.update { it.copy(selectedStatusFilter = status) }
        applyFilters()
    }

    fun setTypeFilter(type: RecordType?) {
        _uiState.update { it.copy(selectedTypeFilter = type) }
        applyFilters()
    }

    fun setFarmFilter(farmId: String?) {
        _uiState.update { it.copy(selectedFarmId = farmId) }
        applyFilters()
    }

    fun setSearchQuery(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
        applyFilters()
    }

    private fun applyFilters() {
        val current = _uiState.value
        val filtered = current.allRecords.filter { item ->
            val matchesStatus = current.selectedStatusFilter == null || item.record.status == current.selectedStatusFilter
            val matchesType = current.selectedTypeFilter == null || item.record.type == current.selectedTypeFilter
            val matchesFarm = current.selectedFarmId == null || item.farmName == current.farms.find { it.id == current.selectedFarmId }?.name
            val query = current.searchQuery.trim().lowercase()
            val matchesQuery = query.isEmpty() ||
                    item.farmName.lowercase().contains(query) ||
                    item.cropName.lowercase().contains(query) ||
                    item.farmerName.lowercase().contains(query)
            matchesStatus && matchesType && matchesFarm && matchesQuery
        }
        _uiState.update { it.copy(filteredRecords = filtered) }
    }
}
