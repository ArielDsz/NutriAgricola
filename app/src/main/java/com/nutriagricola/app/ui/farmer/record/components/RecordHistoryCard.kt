package com.nutriagricola.app.ui.farmer.record.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.nutriagricola.app.domain.model.record.Record
import com.nutriagricola.app.domain.model.record.RecordStatus
import com.nutriagricola.app.domain.model.record.RecordType
import java.time.format.DateTimeFormatter

@Composable
fun RecordHistoryCard(record: Record, cropName: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(cropName, style = MaterialTheme.typography.titleMedium)
            Text(record.type.historyLabel(), style = MaterialTheme.typography.bodyMedium)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    record.date.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")),
                    style = MaterialTheme.typography.bodyMedium
                )
                Text(record.status.historyLabel(), style = MaterialTheme.typography.bodyMedium)
            }
        }
    }
}

fun RecordType.historyLabel(): String = when (this) {
    RecordType.SOIL -> "Suelo"
    RecordType.WATER -> "Agua"
    RecordType.FOLIAR -> "Foliar"
}

private fun RecordStatus.historyLabel(): String = when (this) {
    RecordStatus.DRAFT -> "Borrador"
    RecordStatus.PENDING -> "Pendiente"
    RecordStatus.VIEWED -> "Visto"
    RecordStatus.DELETED -> "Eliminado"
    RecordStatus.VALIDATED -> "Validado"
}
