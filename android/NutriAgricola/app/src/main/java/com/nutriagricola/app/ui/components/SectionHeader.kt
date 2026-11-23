package com.nutriagricola.app.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.nutriagricola.app.R

@Composable
fun SectionHeader(title: String, actions: @Composable RowScope.() -> Unit) {
    Box(Modifier.fillMaxWidth().heightIn(min = 164.dp)
        .clip(RoundedCornerShape(bottomStart = 20.dp, bottomEnd = 20.dp))) {
        Image(painterResource(R.drawable.header_background), null, contentScale = ContentScale.Crop,
            alignment = Alignment.TopCenter, modifier = Modifier.matchParentSize())
        Row(Modifier.fillMaxWidth().statusBarsPadding().padding(start = 16.dp, end = 16.dp, top = 2.dp, bottom = 8.dp)
            .heightIn(min = 64.dp), verticalAlignment = Alignment.CenterVertically) {
            Text(title, style = MaterialTheme.typography.headlineLarge, modifier = Modifier.weight(1f))
            actions()
        }
    }
}
