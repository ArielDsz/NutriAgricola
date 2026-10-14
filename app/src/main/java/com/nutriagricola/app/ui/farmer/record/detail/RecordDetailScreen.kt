package com.nutriagricola.app.ui.farmer.record.detail

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.nutriagricola.app.domain.model.record.FoliarData
import com.nutriagricola.app.domain.model.record.SoilData
import com.nutriagricola.app.domain.model.record.WaterData
import com.nutriagricola.app.ui.farmer.FarmerContextUiState
import com.nutriagricola.app.ui.farmer.record.components.historyLabel
import java.time.format.DateTimeFormatter

@Composable
fun RecordDetailScreen(
    viewModel: RecordDetailViewModel,
    contextState: FarmerContextUiState,
    onBackClick: () -> Unit
) {
    val loadedState by viewModel.uiState.collectAsState()
    // Do not display cached data while a different farm is being validated.
    val state = if (loadedState.farmId == contextState.selectedFarm?.id) loadedState
    else RecordDetailUiState()
    val error = contextState.error ?: state.error

    Scaffold { innerPadding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(innerPadding)
                .verticalScroll(rememberScrollState()).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            TextButton(onClick = onBackClick) { Text("← Volver") }
            Text("Detalle de registro", style = MaterialTheme.typography.titleLarge)

            when {
                error != null -> Text(error, color = MaterialTheme.colorScheme.error)
                contextState.isLoading || state.isLoading -> CircularProgressIndicator()
                else -> {
                    val record = state.record
                    val crop = state.crop
                    if (record != null && crop != null) {
                        DetailField("Cultivo", crop.name)
                        DetailField("Tipo", record.type.historyLabel())
                        DetailField("Fecha", record.date.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")))
                        DetailField("Estado", record.status.historyLabel())

                        when (val data = record.data) {
                            is SoilData -> {
                                DetailField("pH", data.ph?.toString())
                                DetailField("Conductividad eléctrica (CE)", data.electricalConductivity?.toString())
                                DetailField("Nitrógeno (N)", data.nitrogen?.toString())
                                DetailField("Fósforo (P)", data.phosphorus?.toString())
                                DetailField("Potasio (K)", data.potassium?.toString())
                                DetailField("Calcio (Ca)", data.calcium?.toString())
                                DetailField("Magnesio (Mg)", data.magnesium?.toString())
                                DetailField("Materia orgánica", data.organicMatter?.toString())
                            }
                            is WaterData -> {
                                DetailField("pH", data.ph?.toString())
                                DetailField("Conductividad eléctrica (CE)", data.electricalConductivity?.toString())
                                DetailField("Nitrógeno (N)", data.nitrogen?.toString())
                                DetailField("Fósforo (P)", data.phosphorus?.toString())
                                DetailField("Potasio (K)", data.potassium?.toString())
                                DetailField("Calcio (Ca)", data.calcium?.toString())
                                DetailField("Magnesio (Mg)", data.magnesium?.toString())
                                DetailField("Fuente de agua", data.waterSource)
                            }
                            is FoliarData -> {
                                DetailField("Nitrógeno (N)", data.nitrogen?.toString())
                                DetailField("Fósforo (P)", data.phosphorus?.toString())
                                DetailField("Potasio (K)", data.potassium?.toString())
                                DetailField("Calcio (Ca)", data.calcium?.toString())
                                DetailField("Magnesio (Mg)", data.magnesium?.toString())
                                DetailField("Síntomas", data.symptoms)
                            }
                        }
                        DetailField("Observaciones", record.observations)
                    }
                }
            }
        }
    }
}

@Composable
private fun DetailField(label: String, value: String?) {
    if (!value.isNullOrBlank()) {
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(label, style = MaterialTheme.typography.labelMedium)
            Text(value, style = MaterialTheme.typography.bodyLarge)
        }
    }
}
