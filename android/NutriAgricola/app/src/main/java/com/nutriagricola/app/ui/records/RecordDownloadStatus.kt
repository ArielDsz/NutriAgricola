package com.nutriagricola.app.ui.records

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun RecordDownloadStatus(
    state: RecordDownloadUiState,
    onRefresh: () -> Unit,
    onLoadMore: () -> Unit,
    onUpload: () -> Unit,
    modifier: Modifier = Modifier
) {
    if (!state.isBusy && state.error == null && state.pendingUploads == 0 && !state.isUploading &&
        state.uploadMessage == null && !state.hasMore) return
    Column(modifier = modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp)) {
        if (state.isBusy) {
            LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
            Text("Descargando registros…", style = MaterialTheme.typography.bodySmall)
        }
        state.error?.let { Text(it, color = MaterialTheme.colorScheme.error,
            style = MaterialTheme.typography.bodySmall) }
        if (state.pendingUploads > 0 || state.isUploading) {
            Text(if (state.isUploading) "Enviando registros y fotos…"
                else "Registros pendientes de envío: " + state.pendingUploads,
                style = MaterialTheme.typography.bodySmall)
            TextButton(onClick = onUpload, enabled = !state.isUploading) { Text("Enviar pendientes") }
        }
        state.uploadMessage?.let { Text(it, color = MaterialTheme.colorScheme.error,
            style = MaterialTheme.typography.bodySmall) }
        if (state.hasMore) {
            TextButton(onClick = onLoadMore, enabled = !state.isBusy) { Text("Ver más") }
        }
    }
}
