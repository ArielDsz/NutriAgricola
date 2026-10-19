package com.nutriagricola.app.ui.advisor.all_records

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.nutriagricola.app.R
import com.nutriagricola.app.domain.model.record.RecordStatus
import com.nutriagricola.app.domain.model.record.RecordType
import com.nutriagricola.app.ui.advisor.components.AdvisorBottomBar
import com.nutriagricola.app.ui.advisor.components.AdvisorBottomItem
import com.nutriagricola.app.ui.advisor.model.AdvisorRecordItem
import com.nutriagricola.app.ui.theme.ForestGreen
import com.nutriagricola.app.ui.theme.LeafGreen
import com.nutriagricola.app.ui.theme.OfflineBackground

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdvisorAllRecordsScreen(
    viewModel: AdvisorAllRecordsViewModel,
    onBackClick: () -> Unit,
    onPendingClick: () -> Unit,
    onRecordClick: (String) -> Unit
) {
    val state by viewModel.uiState.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.loadData()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Predios y Análisis Totales", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            painter = painterResource(R.drawable.ic_chevron_right),
                            contentDescription = "Volver",
                            modifier = Modifier.size(24.dp)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        bottomBar = {
            AdvisorBottomBar(
                selectedItem = AdvisorBottomItem.ALL_RECORDS,
                onHomeClick = onBackClick,
                onPendingClick = onPendingClick,
                onAllRecordsClick = {},
                onProgramClick = {}
            )
        }
    ) { paddingValues ->
        if (state.isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else if (state.error != null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(text = state.error ?: "Error", color = MaterialTheme.colorScheme.error)
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(onClick = { viewModel.loadData() }) {
                        Text("Reintentar")
                    }
                }
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(horizontal = 16.dp)
            ) {
                // Search Input
                OutlinedTextField(
                    value = state.searchQuery,
                    onValueChange = { viewModel.setSearchQuery(it) },
                    placeholder = { Text("Buscar por predio, cultivo o agricultor...") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true
                )

                // Status Filter Chips
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.padding(bottom = 6.dp)
                ) {
                    item {
                        FilterChip(
                            selected = state.selectedStatusFilter == null,
                            onClick = { viewModel.setStatusFilter(null) },
                            label = { Text("Todos (${state.allRecords.size})") }
                        )
                    }
                    item {
                        FilterChip(
                            selected = state.selectedStatusFilter == RecordStatus.PENDING,
                            onClick = { viewModel.setStatusFilter(RecordStatus.PENDING) },
                            label = { Text("Pendientes") }
                        )
                    }
                    item {
                        FilterChip(
                            selected = state.selectedStatusFilter == RecordStatus.VIEWED,
                            onClick = { viewModel.setStatusFilter(RecordStatus.VIEWED) },
                            label = { Text("Observados") }
                        )
                    }
                    item {
                        FilterChip(
                            selected = state.selectedStatusFilter == RecordStatus.VALIDATED,
                            onClick = { viewModel.setStatusFilter(RecordStatus.VALIDATED) },
                            label = { Text("Validados") }
                        )
                    }
                }

                // Farm Filter Chips
                if (state.farms.isNotEmpty()) {
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.padding(bottom = 12.dp)
                    ) {
                        item {
                            FilterChip(
                                selected = state.selectedFarmId == null,
                                onClick = { viewModel.setFarmFilter(null) },
                                label = { Text("Todos los Predios") }
                            )
                        }
                        items(state.farms) { farm ->
                            FilterChip(
                                selected = state.selectedFarmId == farm.id,
                                onClick = { viewModel.setFarmFilter(farm.id) },
                                label = { Text(farm.name) }
                            )
                        }
                    }
                }

                // List
                if (state.filteredRecords.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No se encontraron análisis en este predio/filtro",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                } else {
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                        contentPadding = PaddingValues(bottom = 16.dp)
                    ) {
                        items(state.filteredRecords) { item ->
                            AllRecordsItemCard(
                                item = item,
                                onClick = { onRecordClick(item.record.id) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun AllRecordsItemCard(
    item: AdvisorRecordItem,
    onClick: () -> Unit
) {
    val typeName = when (item.record.type) {
        RecordType.SOIL -> "Suelo"
        RecordType.WATER -> "Agua"
        RecordType.FOLIAR -> "Foliar"
    }

    val (statusLabel, statusBgColor, statusTextColor) = when (item.record.status) {
        RecordStatus.PENDING -> Triple("Pendiente", MaterialTheme.colorScheme.primaryContainer, ForestGreen)
        RecordStatus.VIEWED -> Triple("Observado", OfflineBackground, ForestGreen)
        RecordStatus.VALIDATED -> Triple("Validado", LeafGreen.copy(alpha = 0.2f), ForestGreen)
        else -> Triple(item.record.status.name, MaterialTheme.colorScheme.surfaceVariant, MaterialTheme.colorScheme.onSurfaceVariant)
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = RoundedCornerShape(4.dp),
                    color = LeafGreen.copy(alpha = 0.15f)
                ) {
                    Text(
                        text = typeName,
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = ForestGreen,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                    )
                }

                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = statusBgColor
                ) {
                    Text(
                        text = statusLabel,
                        style = MaterialTheme.typography.labelSmall,
                        color = statusTextColor,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "${item.farmName} — ${item.cropName}",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "Agricultor: ${item.farmerName}",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Text(
                text = "Fecha muestra: ${item.record.date}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
