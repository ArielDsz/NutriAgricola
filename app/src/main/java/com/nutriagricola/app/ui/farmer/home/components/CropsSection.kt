package com.nutriagricola.app.ui.farmer.home.components

import androidx.compose.foundation.gestures.snapping.rememberSnapFlingBehavior
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.nutriagricola.app.domain.model.farm.Crop

@Composable
fun CropsSection(
    crops: List<Crop>,
    onCropClick: (Crop) -> Unit,
    modifier: Modifier = Modifier
){
    val listState = rememberLazyListState()

    Column(modifier = modifier
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 16.dp, end = 16.dp, top = 6.dp, bottom = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Cultivos",
                style = MaterialTheme.typography.bodyLarge,
                fontSize = 19.sp,
                fontWeight = FontWeight.ExtraBold
            )
        }
        LazyRow(
            state = listState,
            flingBehavior = rememberSnapFlingBehavior(listState),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 4.dp)
        ) {
            items(crops, key = { it.id }) { crop ->
                CropCard(
                    crop = crop,
                    onClick = {
                        onCropClick(crop)
                    },
                    modifier = Modifier
                        .fillParentMaxWidth(0.44f)
                        .heightIn(min = 120.dp)
                )
            }
        }
    }
}
