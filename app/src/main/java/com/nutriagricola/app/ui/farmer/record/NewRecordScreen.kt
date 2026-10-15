package com.nutriagricola.app.ui.farmer.record

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.nutriagricola.app.data.local.database.AppDatabase
import com.nutriagricola.app.data.repository.CropRepository
import com.nutriagricola.app.R
import com.nutriagricola.app.domain.model.record.RecordType
import com.nutriagricola.app.ui.farmer.record.components.RecordTypeCard
import com.nutriagricola.app.ui.farmer.record.components.CropSelectorBottomSheet
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button

@Composable
fun NewRecordScreen(
    farmId: String,
    farmName: String,
    onBackClick: () -> Unit,
    onContinueClick: (String, RecordType) -> Unit
) {

    val context = LocalContext.current

    val database = remember {
        AppDatabase.getDatabase(context)
    }

    val cropRepository = remember {
        CropRepository(database.cropDao())
    }

    val factory = remember {
        NewRecordViewModelFactory(cropRepository)
    }

    val viewModel: NewRecordViewModel = viewModel(
        factory = factory
    )

    val crops by viewModel.crops.collectAsState()
    val selectedCrop by viewModel.selectedCrop.collectAsState()

    var showCropSelector by remember(farmId) {
        mutableStateOf(false)
    }

    val selectedType by viewModel.selectedType.collectAsState()

    LaunchedEffect(farmId) {
        viewModel.loadCrops(farmId)
    }

    if (showCropSelector) {
        CropSelectorBottomSheet(
            crops = crops,
            selectedCropId = selectedCrop?.id,
            onDismiss = { showCropSelector = false },
            onCropSelected = { crop ->
                viewModel.selectCrop(crop)
                showCropSelector = false
            }
        )
    }

    Scaffold { innerPadding ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {

            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextButton(
                    onClick = onBackClick
                ) {
                    Text("←")
                }

                Column {
                    Text(
                        text = "Nuevo registro",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.SemiBold
                    )

                    Text(
                        text = "Selecciona el cultivo y tipo de registro",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }

            Column {
                Text(
                    text = "Predio",
                    style = MaterialTheme.typography.labelMedium
                )

                Text(
                    text = farmName,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
            }

            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {

                Text(
                    text = "Cultivo",
                    style = MaterialTheme.typography.titleMedium
                )

                Card(
                    onClick = {
                        showCropSelector = true
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = selectedCrop?.name ?: "Seleccionar cultivo",
                        modifier = Modifier.padding(16.dp),
                        style = MaterialTheme.typography.bodyLarge
                    )
                }

                if (selectedCrop != null) {

                    Column(
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {

                        Text(
                            text = "Tipo de registro",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold
                        )

                        RecordTypeCard(
                            title = "Análisis de suelo",
                            description = "pH, CE, N, P, K, Ca, Mg",
                            imageRes = R.drawable.analysis_soil,
                            selected = selectedType == RecordType.SOIL,
                            onClick = {
                                viewModel.selectType(RecordType.SOIL)
                            }
                        )

                        RecordTypeCard(
                            title = "Análisis de agua",
                            description = "pH, CE, nutrientes y fuente de agua",
                            imageRes = R.drawable.analysis_water,
                            selected = selectedType == RecordType.WATER,
                            onClick = {
                                viewModel.selectType(RecordType.WATER)
                            }
                        )

                        RecordTypeCard(
                            title = "Análisis foliar",
                            description = "Nutrientes, síntomas y fotografías",
                            imageRes = R.drawable.analysis_foliar,
                            selected = selectedType == RecordType.FOLIAR,
                            onClick = {
                                viewModel.selectType(RecordType.FOLIAR)
                            }
                        )
                    }
                }
            }
            Button(
                onClick = {
                    selectedCrop?.let { crop ->
                        selectedType?.let { type ->
                            onContinueClick(crop.id, type)
                        }
                    }
                },
                enabled = selectedCrop != null && selectedType != null,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Continuar")
            }
        }
    }
}
