package com.nutriagricola.app.ui.farmer.record.soil

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp

@Composable
fun SoilRecordScreen(
    viewModel: SoilRecordViewModel,
    onBackClick: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .imePadding()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            TextButton(onClick = onBackClick) {
                Text("← Volver")
            }

            Text(
                text = "Análisis de suelo",
                style = MaterialTheme.typography.titleLarge
            )

            if (uiState.isLoading) {
                CircularProgressIndicator()
            }

            uiState.error?.let { error ->
                Text(text = error, color = MaterialTheme.colorScheme.error)
            }

            uiState.crop?.let { crop ->
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.primaryContainer
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text("Cultivo", style = MaterialTheme.typography.labelMedium)
                        Text(
                            text = if (crop.variety.isNullOrBlank()) crop.name
                            else "${crop.name} - ${crop.variety}",
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                }

                SoilNumericField("pH", uiState.ph, viewModel::updatePh)
                SoilNumericField(
                    "Conductividad eléctrica (CE)",
                    uiState.electricalConductivity,
                    viewModel::updateElectricalConductivity
                )
                SoilNumericField("Nitrógeno (N)", uiState.nitrogen, viewModel::updateNitrogen)
                SoilNumericField("Fósforo (P)", uiState.phosphorus, viewModel::updatePhosphorus)
                SoilNumericField("Potasio (K)", uiState.potassium, viewModel::updatePotassium)
                SoilNumericField("Calcio (Ca)", uiState.calcium, viewModel::updateCalcium)
                SoilNumericField("Magnesio (Mg)", uiState.magnesium, viewModel::updateMagnesium)
                SoilNumericField(
                    "Materia orgánica",
                    uiState.organicMatter,
                    viewModel::updateOrganicMatter
                )

                OutlinedTextField(
                    value = uiState.observations,
                    onValueChange = viewModel::updateObservations,
                    label = { Text("Observaciones") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3,
                    shape = RoundedCornerShape(12.dp)
                )
            }
        }
    }
}

@Composable
private fun SoilNumericField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        modifier = Modifier.fillMaxWidth(),
        singleLine = true,
        keyboardOptions = KeyboardOptions(
            keyboardType = KeyboardType.Decimal,
            imeAction = ImeAction.Next
        ),
        shape = RoundedCornerShape(12.dp)
    )
}
