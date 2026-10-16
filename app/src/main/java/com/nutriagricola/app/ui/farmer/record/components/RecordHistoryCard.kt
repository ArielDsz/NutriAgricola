package com.nutriagricola.app.ui.farmer.record.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.nutriagricola.app.R
import com.nutriagricola.app.ui.theme.OfflineBackground
import com.nutriagricola.app.domain.model.record.Record
import com.nutriagricola.app.domain.model.record.RecordStatus
import com.nutriagricola.app.domain.model.record.RecordType
import java.time.format.DateTimeFormatter

@Composable
fun RecordHistoryCard(record: Record, cropName: String, onClick: () -> Unit) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.5.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(
            modifier = Modifier.padding(10.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Image(
                painterResource(when (record.type) {
                    RecordType.SOIL -> R.drawable.analysis_soil
                    RecordType.WATER -> R.drawable.analysis_water
                    RecordType.FOLIAR -> R.drawable.analysis_foliar
                }),
                contentDescription = null,
                modifier = Modifier.size(44.dp)
            )
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    when (record.type) {
                        RecordType.SOIL -> "Análisis de suelo"
                        RecordType.WATER -> "Análisis de agua"
                        RecordType.FOLIAR -> "Análisis foliar"
                    },
                    style = MaterialTheme.typography.bodyMedium
                )
                Text(cropName, style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically) {
                    Icon(painterResource(R.drawable.ic_calendar), null,
                        modifier = Modifier.size(14.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(
                        record.date.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            Column(horizontalAlignment = Alignment.End, verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Icon(painterResource(R.drawable.ic_chevron_right), null,
                    tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
                Surface(
                    color = if (record.status == RecordStatus.PENDING) OfflineBackground
                        else MaterialTheme.colorScheme.primaryContainer,
                    shape = RoundedCornerShape(50)
                ) {
                    Row(modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp),
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalAlignment = Alignment.CenterVertically) {
                        if (record.status == RecordStatus.PENDING || record.status == RecordStatus.VALIDATED) {
                            Icon(painterResource(if (record.status == RecordStatus.PENDING) R.drawable.ic_history
                                else R.drawable.ic_check), null, modifier = Modifier.size(14.dp),
                                tint = MaterialTheme.colorScheme.primary)
                        }
                        Text(record.status.historyLabel(), style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface)
                    }
                }
            }
        }
    }
}

fun RecordType.historyLabel(): String = when (this) {
    RecordType.SOIL -> "Suelo"
    RecordType.WATER -> "Agua"
    RecordType.FOLIAR -> "Foliar"
}

fun RecordStatus.historyLabel(): String = when (this) {
    RecordStatus.DRAFT -> "Borrador"
    RecordStatus.PENDING -> "Pendiente"
    RecordStatus.VIEWED -> "Visto"
    RecordStatus.DELETED -> "Eliminado"
    RecordStatus.VALIDATED -> "Validado"
}
