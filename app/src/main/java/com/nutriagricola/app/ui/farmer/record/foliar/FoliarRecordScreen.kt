package com.nutriagricola.app.ui.farmer.record.foliar

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import com.nutriagricola.app.R
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.nutriagricola.app.ui.farmer.record.components.RecordForm
import com.nutriagricola.app.ui.farmer.record.components.RecordNumericField
import com.nutriagricola.app.ui.farmer.record.components.RecordTextField
import com.nutriagricola.app.ui.farmer.record.components.FoliarPhotoGallery

@Composable
fun FoliarRecordScreen(
    viewModel: FoliarRecordViewModel,
    onBackClick: () -> Unit,
    onSaved: () -> Unit
) {
    val state by viewModel.uiState.collectAsState()
    val enabled = !state.isSaving && !state.isSaved && !state.isProcessingPhoto &&
        state.pendingCameraPath == null
    val cameraLauncher = rememberLauncherForActivityResult(ActivityResultContracts.TakePicture()) { success ->
        viewModel.onCameraResult(success)
    }
    val imageLauncher = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        uri?.let(viewModel::importPhoto)
    }

    LaunchedEffect(state.isSaved) {
        if (state.isSaved) onSaved()
    }

    RecordForm(
        title = "Análisis foliar",
        subtitle = "Registra los nutrientes, síntomas y fotografías de tu cultivo.",
        crop = state.crop,
        isLoading = state.isLoading,
        error = state.error,
        isSaving = state.isSaving || state.isProcessingPhoto,
        isSaved = state.isSaved,
        onBackClick = onBackClick,
        onSaveClick = viewModel::save
    ) {
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
        RecordTextField(
            "Síntomas", state.symptoms, viewModel::updateSymptoms,
            enabled = enabled, iconRes = R.drawable.ic_symptoms, minLines = 3
        )
        RecordTextField(
            "Observaciones", state.observations, viewModel::updateObservations,
            enabled = enabled, iconRes = R.drawable.ic_observations, minLines = 3
        )
        OutlinedButton(
            enabled = enabled,
            modifier = Modifier.fillMaxWidth().heightIn(min = 56.dp),
            shape = RoundedCornerShape(14.dp),
            onClick = {
                viewModel.prepareCamera { uri ->
                    try {
                        cameraLauncher.launch(uri)
                    } catch (_: Exception) {
                        viewModel.onCameraResult(success = false, launchFailed = true)
                    }
                }
            }
        ) {
            Image(painterResource(R.drawable.ic_camera), null,
                modifier = Modifier.padding(end = 8.dp).size(40.dp))
            Text("Tomar foto", style = MaterialTheme.typography.bodyLarge)
        }
        OutlinedButton(
            enabled = enabled,
            modifier = Modifier.fillMaxWidth().heightIn(min = 56.dp),
            shape = RoundedCornerShape(14.dp),
            onClick = {
                try {
                    imageLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                } catch (_: Exception) {
                    viewModel.onPickerLaunchFailed()
                }
            }
        ) {
            Image(painterResource(R.drawable.ic_gallery), null,
                modifier = Modifier.padding(end = 8.dp).size(40.dp))
            Text("Seleccionar imagen", style = MaterialTheme.typography.bodyLarge)
        }
        FoliarPhotoGallery(
            photos = state.photos,
            enabled = enabled,
            onRemove = viewModel::removePhoto
        )
    }
}
