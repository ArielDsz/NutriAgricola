package com.nutriagricola.app.ui.farmer.crop.detail

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.nutriagricola.app.R
import com.nutriagricola.app.domain.model.record.RecordType
import com.nutriagricola.app.ui.farmer.FarmerContextUiState
import com.nutriagricola.app.ui.farmer.record.components.RecordHistoryCard
import com.nutriagricola.app.ui.farmer.record.components.historyLabel

@Composable
fun CropDetailScreen(
    viewModel: CropDetailViewModel,
    contextState: FarmerContextUiState,
    onBackClick: () -> Unit,
    onRecordClick: (String) -> Unit
) {
    val loadedState by viewModel.uiState.collectAsState()
    // Hide cached data immediately when it no longer belongs to the current farm.
    val state = if (loadedState.farmId == contextState.selectedFarm?.id) loadedState
    else CropDetailUiState()
    val error = contextState.error ?: state.error
    val crop = state.crop

    Scaffold { innerPadding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(innerPadding).padding(horizontal = 16.dp),
            contentPadding = PaddingValues(bottom = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Column(modifier = Modifier.padding(top = 16.dp)) {
                    TextButton(onClick = onBackClick) { Text("← Volver") }
                    Text("Detalle del cultivo", style = MaterialTheme.typography.titleLarge)
                }
            }
            when {
                contextState.isLoading -> item { CircularProgressIndicator() }
                error != null -> item { Text(error, color = MaterialTheme.colorScheme.error) }
                state.isLoading -> item { CircularProgressIndicator() }
                crop != null -> {
                    item {
                        val fallback = painterResource(R.drawable.default_crop)
                        AsyncImage(
                            model = crop.imagePath?.takeIf { it.isNotBlank() },
                            contentDescription = "Imagen de ${crop.name}",
                            placeholder = fallback,
                            fallback = fallback,
                            error = fallback,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxWidth().height(180.dp)
                                .clip(RoundedCornerShape(12.dp))
                        )
                    }
                    item {
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text(crop.name, style = MaterialTheme.typography.titleLarge)
                            crop.variety?.takeIf { it.isNotBlank() }?.let {
                                Text("Variedad: $it", style = MaterialTheme.typography.bodyMedium)
                            }
                            crop.areaHectares?.let {
                                Text("Superficie: $it ha", style = MaterialTheme.typography.bodyMedium)
                            }
                            crop.phenologicalStage?.takeIf { it.isNotBlank() }?.let {
                                Text("Etapa fenológica: $it", style = MaterialTheme.typography.bodyMedium)
                            }
                        }
                    }
                    item { Text("Últimos registros", style = MaterialTheme.typography.titleMedium) }
                    if (state.latestRecords.isEmpty()) {
                        item { Text("Todavía no hay registros para este cultivo") }
                    } else {
                        items(state.latestRecords, key = { "latest_${it.id}" }) { record ->
                            RecordHistoryCard(
                                record = record,
                                cropName = crop.name,
                                onClick = { onRecordClick(record.id) }
                            )
                        }
                    }
                    item { Text("Historial del cultivo", style = MaterialTheme.typography.titleMedium) }
                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            FilterChip(
                                selected = state.selectedType == null,
                                onClick = { viewModel.selectType(null) },
                                label = { Text("Todos") }
                            )
                            RecordType.entries.forEach { type ->
                                FilterChip(
                                    selected = state.selectedType == type,
                                    onClick = { viewModel.selectType(type) },
                                    label = { Text(type.historyLabel()) }
                                )
                            }
                        }
                    }
                    if (state.records.isEmpty()) {
                        item { Text("No hay registros para esta selección") }
                    } else {
                        items(state.records, key = { "history_${it.id}" }) { record ->
                            RecordHistoryCard(
                                record = record,
                                cropName = crop.name,
                                onClick = { onRecordClick(record.id) }
                            )
                        }
                    }
                }
            }
        }
    }
}
