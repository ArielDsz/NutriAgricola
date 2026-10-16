package com.nutriagricola.app.ui.farmer.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.res.painterResource
import com.nutriagricola.app.R
import com.nutriagricola.app.domain.model.user.User
import com.nutriagricola.app.ui.farmer.FarmerContextUiState
import com.nutriagricola.app.ui.farmer.home.components.CropsSection
import com.nutriagricola.app.ui.farmer.home.components.EmptyFarmCard
import com.nutriagricola.app.ui.farmer.home.components.FarmCard
import com.nutriagricola.app.ui.farmer.home.components.FarmSelectorBottomSheet
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
    onFarmSelected: (String) -> Unit,
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
    var showFarmSelector by rememberSaveable { mutableStateOf(false) }

    if (showFarmSelector) {
        FarmSelectorBottomSheet(
            farms = contextState.farms,
            selectedFarmId = farm?.id,
            onDismiss = { showFarmSelector = false },
            onFarmSelected = { farmId ->
                onFarmSelected(farmId)
                showFarmSelector = false
            }
        )
    }

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
            Box(modifier = Modifier.fillMaxWidth()) {
                HomeHeader(
                    initials = initials,
                    onLogoutClick = onLogoutClick,
                    modifier = Modifier.fillMaxWidth().height(164.dp)
                )
                Column(modifier = Modifier.fillMaxWidth().padding(top = 116.dp)) {
                when {
                    contextState.isLoading -> CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.CenterHorizontally)
                    )
                    farm != null -> FarmCard(
                        farm = farm,
                        cropCount = state.crops.size,
                        onClick = { showFarmSelector = true },
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)
                    )
                    else -> EmptyFarmCard(
                        onClick = {},
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)
                    )
                }
                }
            }

            SyncStatus(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp)
            )

            key(farm?.id) {
                CropsSection(
                    crops = state.crops,
                    onCropClick = { onCropClick(it.id) }
                )
            }

            Button(
                onClick = {
                    farm?.let { onNewRecordClick(it.id, it.name) }
                },
                enabled = farm != null && !contextState.isLoading,
                modifier = Modifier.fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                shape = RoundedCornerShape(14.dp),
                contentPadding = PaddingValues(0.dp),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 3.dp, pressedElevation = 1.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth()
                        .background(Brush.verticalGradient(
                            if (farm != null && !contextState.isLoading) listOf(
                                lerp(MaterialTheme.colorScheme.primary, MaterialTheme.colorScheme.secondary, 0.3f),
                                MaterialTheme.colorScheme.primary
                            ) else listOf(MaterialTheme.colorScheme.surfaceVariant, MaterialTheme.colorScheme.surfaceVariant)
                        ))
                        .heightIn(min = 52.dp).padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                Icon(painterResource(R.drawable.ic_add_circle), contentDescription = null,
                    modifier = Modifier.padding(end = 10.dp).size(26.dp))
                Text("Nuevo registro", style = MaterialTheme.typography.bodyLarge)
                }
            }

            Column(
                modifier = Modifier.fillMaxWidth().padding(start = 16.dp, end = 16.dp, bottom = 16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Historial reciente", style = MaterialTheme.typography.bodyLarge,
                        fontSize = 19.sp, fontWeight = FontWeight.ExtraBold,
                        modifier = Modifier.weight(1f))
                    TextButton(onClick = onHistoryClick) {
                        Text("Ver todos", style = MaterialTheme.typography.bodySmall)
                        Icon(painterResource(R.drawable.ic_chevron_right), contentDescription = null,
                            modifier = Modifier.size(20.dp))
                    }
                }

                when {
                    error != null -> Text(error, color = MaterialTheme.colorScheme.error)
                    contextState.isLoading || (farm != null && state.isLoading) ->
                        CircularProgressIndicator()
                    farm == null -> Text("No hay un predio seleccionado")
                    state.recentRecords.isEmpty() -> Text("No hay registros recientes")
                    else -> state.recentRecords.forEach { record ->
                        val crop = state.crops.find { it.id == record.cropId }
                        RecordHistoryCard(
                            record = record,
                            cropName = crop?.name ?: "Cultivo",
                            onClick = { onRecordClick(record.id) },
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 14.dp),
                            cropImagePath = crop?.imagePath,
                            showCropThumbnail = true,
                            cropThumbnailScale = 1.15f
                        )
                    }
                }
            }
        }
    }
}
