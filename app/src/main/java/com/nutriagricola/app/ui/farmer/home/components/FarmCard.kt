package com.nutriagricola.app.ui.farmer.home.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.nutriagricola.app.R
import com.nutriagricola.app.domain.model.farm.Farm

@Composable
fun FarmCard(
    farm: Farm,
    cropCount: Int,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
){
    Card(
        onClick = onClick,
        modifier = modifier,
        shape = RoundedCornerShape(14.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        BoxWithConstraints {
        val imageWidth = (maxWidth * 0.27f).coerceIn(72.dp, 100.dp)
        Row(
            modifier = Modifier.padding(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {

            val fallback = painterResource(R.drawable.default_crop)
            AsyncImage(
                model = farm.imagePath?.takeIf { it.isNotBlank() },
                contentDescription = "Imagen del predio ${farm.name}",
                placeholder = fallback,
                fallback = fallback,
                error = fallback,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .width(imageWidth).height(imageWidth * 0.82f)
                    .clip(RoundedCornerShape(10.dp))
            )

            Spacer(
                modifier = Modifier.width(10.dp)
            )

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {

                Text(
                    text = "Predio seleccionado",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Text(
                    text = farm.name,
                    style = MaterialTheme.typography.titleMedium
                )
                Surface(shape = RoundedCornerShape(50), color = MaterialTheme.colorScheme.primaryContainer) {
                    Text(
                        text = "$cropCount cultivos",
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        style = MaterialTheme.typography.bodySmall
                    )
                }

            }

            Icon(
                painter = painterResource(R.drawable.ic_expand_more),
                contentDescription = "Cambiar predio",
                modifier = Modifier.size(24.dp),
                tint = MaterialTheme.colorScheme.primary
            )
        }
        }
    }
}
