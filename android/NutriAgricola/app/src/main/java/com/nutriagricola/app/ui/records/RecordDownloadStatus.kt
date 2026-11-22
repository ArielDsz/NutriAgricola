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
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp)) {
        if (state.isBusy) {
            LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
            Text("Descargando registros…", style = MaterialTheme.typography.bodySmall)
        }
        state.error?.let { Text(it, color = MaterialTheme.colorScheme.error,
            style = MaterialTheme.typography.bodySmall) }
        TextButton(onClick = onRefresh, enabled = !state.isBusy) { Text("Actualizar registros") }
        if (state.hasMore) {
            TextButton(onClick = onLoadMore, enabled = !state.isBusy) { Text("Descargar más registros") }
        }
    }
}
