package com.nutriagricola.app.ui.farmer.home.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.nutriagricola.app.R
import com.nutriagricola.app.domain.model.farm.Crop

@Composable
fun CropCard(
    crop: Crop,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        onClick = onClick,
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.5.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier.padding(6.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {

            val fallback = painterResource(R.drawable.default_crop)
            AsyncImage(
                model = crop.imagePath?.takeIf { it.isNotBlank() },
                contentDescription = "Imagen de ${crop.name}",
                placeholder = fallback,
                fallback = fallback,
                error = fallback,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxWidth().height(84.dp)
                    .clip(RoundedCornerShape(8.dp))
            )

            Column(
                modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = crop.name,
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier.weight(1f)
                    )
                    Icon(painterResource(R.drawable.ic_chevron_right), null,
                        modifier = Modifier.size(18.dp), tint = MaterialTheme.colorScheme.primary)
                }

                crop.variety?.let {
                    Text(
                        text = it,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                crop.phenologicalStage?.let {
                    Surface(shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.primaryContainer) {
                        Text(
                            text = it,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            style = MaterialTheme.typography.bodySmall,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                crop.areaHectares?.let {
                    Text(
                        text = "$it ha",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        }
    }
}
