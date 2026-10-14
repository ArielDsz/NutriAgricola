package com.nutriagricola.app.ui.farmer.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.nutriagricola.app.domain.model.user.User
import com.nutriagricola.app.ui.farmer.FarmerContextUiState
import com.nutriagricola.app.ui.farmer.home.components.CropsSection
import com.nutriagricola.app.ui.farmer.home.components.EmptyFarmCard
import com.nutriagricola.app.ui.farmer.home.components.FarmCard
import com.nutriagricola.app.ui.farmer.home.components.FarmerBottomBar
import com.nutriagricola.app.ui.farmer.home.components.FarmerBottomItem
import com.nutriagricola.app.ui.farmer.home.components.HomeHeader
import com.nutriagricola.app.ui.farmer.home.components.SyncStatus
import com.nutriagricola.app.ui.farmer.record.components.RecordHistoryCard

@Composable
fun FarmerHomeScreen(
    user: User,
    viewModel: FarmerHomeViewModel,
    contextState: FarmerContextUiState,
    onLogoutClick: () -> Unit,
    onHistoryClick: () -> Unit,
    onRecordClick: (String) -> Unit,
    onCropClick: (String) -> Unit,
    onNewRecordClick: (String, String) -> Unit
) {
    val initials = remember(user.firstName, user.lastName) {
        listOf(user.firstName, user.lastName)
            .mapNotNull { it.trim().firstOrNull()?.toString() }
            .joinToString("")
            .uppercase()
    }
    val loadedState by viewModel.uiState.collectAsState()
    val farm = contextState.selectedFarm
    val state = if (loadedState.farmId == farm?.id) loadedState
    else FarmerHomeUiState(farmId = farm?.id, isLoading = true)
    val error = contextState.error ?: state.error

    Scaffold(
        bottomBar = {
            FarmerBottomBar(
                selectedItem = FarmerBottomItem.HOME,
                onHomeClick = {},
                onHistoryClick = onHistoryClick,
                onProgramClick = {},
                onFarmsClick = {}
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier.fillMaxSize()
                .padding(bottom = innerPadding.calculateBottomPadding())
                .verticalScroll(rememberScrollState())
        ) {
            Box(modifier = Modifier.fillMaxWidth().height(200.dp)) {
                HomeHeader(
                    initials = initials,
                    onLogoutClick = onLogoutClick,
                    modifier = Modifier.height(150.dp)
                )

                when {
                    contextState.isLoading -> CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.BottomCenter)
                    )
                    farm != null -> FarmCard(
                        farm = farm,
                        cropCount = state.crops.size,
                        onClick = {},
                        modifier = Modifier.align(Alignment.BottomCenter)
                            .fillMaxWidth().padding(horizontal = 16.dp)
                    )
                    else -> EmptyFarmCard(
                        onClick = {},
                        modifier = Modifier.align(Alignment.BottomCenter)
                            .fillMaxWidth().padding(horizontal = 16.dp)
                    )
                }
            }

            SyncStatus(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp)
            )

            CropsSection(
                crops = state.crops,
                onAddClick = {},
                onCropClick = { onCropClick(it.id) }
            )

            Button(
                onClick = {
                    farm?.let { onNewRecordClick(it.id, it.name) }
                },
                enabled = farm != null && !contextState.isLoading,
                modifier = Modifier.fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 16.dp).height(56.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Nuevo registro", style = MaterialTheme.typography.titleMedium)
            }

            Column(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Historial reciente", style = MaterialTheme.typography.titleMedium)
                    TextButton(onClick = onHistoryClick) { Text("Ver todos") }
                }

                when {
                    error != null -> Text(error, color = MaterialTheme.colorScheme.error)
                    contextState.isLoading || (farm != null && state.isLoading) ->
                        CircularProgressIndicator()
                    farm == null -> Text("No hay un predio seleccionado")
                    state.recentRecords.isEmpty() -> Text("No hay registros recientes")
                    else -> state.recentRecords.forEach { record ->
                        RecordHistoryCard(
                            record = record,
                            cropName = state.crops.find { it.id == record.cropId }?.name ?: "Cultivo",
                            onClick = { onRecordClick(record.id) }
                        )
                    }
                }
            }
        }
    }
}
