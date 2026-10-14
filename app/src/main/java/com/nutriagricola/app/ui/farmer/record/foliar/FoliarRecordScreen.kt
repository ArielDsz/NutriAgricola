package com.nutriagricola.app.ui.farmer.record.foliar

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.nutriagricola.app.ui.farmer.record.components.RecordForm
import com.nutriagricola.app.ui.farmer.record.components.RecordNumericField
import com.nutriagricola.app.ui.farmer.record.components.RecordTextField

@Composable
fun FoliarRecordScreen(
    viewModel: FoliarRecordViewModel,
    onBackClick: () -> Unit,
    onSaved: () -> Unit
) {
    val state by viewModel.uiState.collectAsState()
    val enabled = !state.isSaving && !state.isSaved

    LaunchedEffect(state.isSaved) {
        if (state.isSaved) onSaved()
    }

    RecordForm(
        title = "Análisis foliar",
        crop = state.crop,
        isLoading = state.isLoading,
        error = state.error,
        isSaving = state.isSaving,
        isSaved = state.isSaved,
        onBackClick = onBackClick,
        onSaveClick = viewModel::save
    ) {
        RecordNumericField(
            "Nitrógeno (N)", state.nitrogen, viewModel::updateNitrogen,
            error = state.fieldErrors["nitrogen"], enabled = enabled
        )
        RecordNumericField(
            "Fósforo (P)", state.phosphorus, viewModel::updatePhosphorus,
            error = state.fieldErrors["phosphorus"], enabled = enabled
        )
        RecordNumericField(
            "Potasio (K)", state.potassium, viewModel::updatePotassium,
            error = state.fieldErrors["potassium"], enabled = enabled
        )
        RecordNumericField(
            "Calcio (Ca)", state.calcium, viewModel::updateCalcium,
            error = state.fieldErrors["calcium"], enabled = enabled
        )
        RecordNumericField(
            "Magnesio (Mg)", state.magnesium, viewModel::updateMagnesium,
            error = state.fieldErrors["magnesium"], enabled = enabled
        )
        RecordTextField(
            "Síntomas", state.symptoms, viewModel::updateSymptoms,
            enabled = enabled, minLines = 3
        )
        RecordTextField(
            "Observaciones", state.observations, viewModel::updateObservations,
            enabled = enabled, minLines = 3
        )
    }
}
