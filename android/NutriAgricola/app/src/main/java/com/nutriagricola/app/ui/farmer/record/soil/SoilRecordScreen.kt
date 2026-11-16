package com.nutriagricola.app.ui.farmer.record.soil

import com.nutriagricola.app.R
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.nutriagricola.app.ui.farmer.record.components.RecordForm
import com.nutriagricola.app.ui.farmer.record.components.RecordNumericField
import com.nutriagricola.app.ui.farmer.record.components.RecordTextField

@Composable
fun SoilRecordScreen(
    viewModel: SoilRecordViewModel,
    onBackClick: () -> Unit,
    onSaved: () -> Unit
) {
    val state by viewModel.uiState.collectAsState()
    val enabled = !state.isSaving && !state.isSaved

    LaunchedEffect(state.isSaved) {
        if (state.isSaved) onSaved()
    }

    RecordForm(
        title = "Análisis de suelo",
        subtitle = "Registra los resultados del análisis de suelo para tu cultivo.",
        crop = state.crop,
        isLoading = state.isLoading,
        error = state.error,
        isSaving = state.isSaving,
        isSaved = state.isSaved,
        onBackClick = onBackClick,
        onSaveClick = viewModel::save
    ) {
        RecordNumericField(
            "pH", state.ph, viewModel::updatePh,
            error = state.fieldErrors["ph"], enabled = enabled, iconRes = R.drawable.ic_ph
        )
        RecordNumericField(
            "Conductividad eléctrica (CE)", state.electricalConductivity, viewModel::updateElectricalConductivity,
            error = state.fieldErrors["electricalConductivity"], enabled = enabled, iconRes = R.drawable.ic_conductivity
        )
        RecordNumericField(
            "Nitrógeno (N)", state.nitrogen, viewModel::updateNitrogen,
            error = state.fieldErrors["nitrogen"], enabled = enabled, iconRes = R.drawable.ic_nitrogen
        )
        RecordNumericField(
            "Fósforo (P)", state.phosphorus, viewModel::updatePhosphorus,
            error = state.fieldErrors["phosphorus"], enabled = enabled, iconRes = R.drawable.ic_phosphorus
        )
        RecordNumericField(
            "Potasio (K)", state.potassium, viewModel::updatePotassium,
            error = state.fieldErrors["potassium"], enabled = enabled, iconRes = R.drawable.ic_potassium
        )
        RecordNumericField(
            "Calcio (Ca)", state.calcium, viewModel::updateCalcium,
            error = state.fieldErrors["calcium"], enabled = enabled, iconRes = R.drawable.ic_calcium
        )
        RecordNumericField(
            "Magnesio (Mg)", state.magnesium, viewModel::updateMagnesium,
            error = state.fieldErrors["magnesium"], enabled = enabled, iconRes = R.drawable.ic_magnesium
        )
        RecordNumericField(
            "Materia orgánica", state.organicMatter, viewModel::updateOrganicMatter,
            error = state.fieldErrors["organicMatter"], enabled = enabled, iconRes = R.drawable.ic_organic_matter
        )
        RecordTextField(
            "Observaciones", state.observations, viewModel::updateObservations,
            enabled = enabled, iconRes = R.drawable.ic_observations, minLines = 3
        )
    }
}
