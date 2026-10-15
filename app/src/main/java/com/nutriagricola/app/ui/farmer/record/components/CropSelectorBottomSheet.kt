package com.nutriagricola.app.ui.farmer.record.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.nutriagricola.app.R
import com.nutriagricola.app.domain.model.farm.Crop

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CropSelectorBottomSheet(
    crops: List<Crop>,
    selectedCropId: String?,
    onDismiss: () -> Unit,
    onCropSelected: (Crop) -> Unit
) {
    ModalBottomSheet(onDismissRequest = onDismiss) {
        Text(
            "Seleccionar cultivo",
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
        )
        LazyColumn(
            modifier = Modifier.fillMaxWidth().heightIn(max = 400.dp).selectableGroup(),
            contentPadding = PaddingValues(bottom = 24.dp)
        ) {
            if (crops.isEmpty()) {
                item {
                    Text("No hay cultivos disponibles en este predio", modifier = Modifier.padding(16.dp))
                }
            }
            items(crops, key = { it.id }) { crop ->
                val selected = crop.id == selectedCropId
                Row(
                    modifier = Modifier.fillMaxWidth().selectable(
                        selected = selected,
                        role = Role.RadioButton,
                        onClick = { onCropSelected(crop) }
                    ).padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    RadioButton(selected = selected, onClick = null)
                    val fallback = painterResource(R.drawable.default_crop)
                    AsyncImage(
                        model = crop.imagePath?.takeIf { it.isNotBlank() },
                        contentDescription = "Imagen de ${crop.name}",
                        placeholder = fallback,
                        fallback = fallback,
                        error = fallback,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.size(56.dp).clip(RoundedCornerShape(12.dp))
                    )
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(crop.name, style = MaterialTheme.typography.titleMedium)
                        crop.variety?.takeIf { it.isNotBlank() }?.let {
                            Text(it, style = MaterialTheme.typography.bodyMedium)
                        }
                        if (selected) {
                            Text("Seleccionado", style = MaterialTheme.typography.labelMedium)
                        }
                    }
                }
            }
        }
    }
}
