package com.nutriagricola.app.ui.farmer.record.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil3.compose.SubcomposeAsyncImage

@Composable
fun FoliarPhotoGallery(
    photos: List<String>,
    enabled: Boolean = true,
    onRemove: ((String) -> Unit)? = null
) {
    var expandedPhoto by remember(photos) { mutableStateOf<String?>(null) }
    if (photos.isEmpty()) return

    Text("Fotografías", style = MaterialTheme.typography.titleMedium)
    LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        itemsIndexed(photos, key = { _, path -> path }) { index, path ->
            Column {
                SubcomposeAsyncImage(
                    model = path,
                    contentDescription = "Fotografía foliar ${index + 1}",
                    contentScale = ContentScale.Crop,
                    error = { Text("No se pudo cargar la imagen") },
                    modifier = Modifier.size(112.dp).clickable { expandedPhoto = path }
                )
                if (onRemove != null) {
                    TextButton(onClick = { onRemove(path) }, enabled = enabled) { Text("Quitar") }
                }
            }
        }
    }

    expandedPhoto?.let { path ->
        Dialog(
            onDismissRequest = { expandedPhoto = null },
            properties = DialogProperties(usePlatformDefaultWidth = false)
        ) {
            Surface(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
                Column(modifier = Modifier.padding(12.dp)) {
                    SubcomposeAsyncImage(
                        model = path,
                        contentDescription = "Fotografía foliar ampliada",
                        contentScale = ContentScale.Fit,
                        error = { Text("No se pudo cargar la imagen") },
                        modifier = Modifier.fillMaxWidth().height(400.dp)
                    )
                    TextButton(onClick = { expandedPhoto = null }) { Text("Cerrar") }
                }
            }
        }
    }
}
