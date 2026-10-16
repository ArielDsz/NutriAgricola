package com.nutriagricola.app.ui.farmer.record.components

import androidx.activity.compose.BackHandler
import androidx.annotation.DrawableRes
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.nutriagricola.app.R
import com.nutriagricola.app.domain.model.farm.Crop
import com.nutriagricola.app.ui.components.dismissKeyboardOnBackgroundTap

@Composable
fun RecordForm(
    title: String,
    subtitle: String,
    crop: Crop?,
    isLoading: Boolean,
    error: String?,
    isSaving: Boolean,
    isSaved: Boolean,
    onBackClick: () -> Unit,
    onSaveClick: () -> Unit,
    content: @Composable ColumnScope.() -> Unit
) {
    val focusManager = LocalFocusManager.current
    BackHandler(enabled = isSaving) { }

    Scaffold(modifier = Modifier.dismissKeyboardOnBackgroundTap()) { innerPadding ->
        Column(
            modifier = Modifier.fillMaxSize()
                .padding(bottom = innerPadding.calculateBottomPadding()).imePadding()
                .verticalScroll(rememberScrollState())
        ) {
            RecordFormHeader(title, subtitle, !isSaving, onBackClick)
            Column(
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                if (isLoading) CircularProgressIndicator()
                error?.let { Text(it, color = MaterialTheme.colorScheme.error) }

                if (crop != null) {
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        color = MaterialTheme.colorScheme.primaryContainer
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            val fallback = painterResource(R.drawable.default_crop)
                            AsyncImage(
                                model = crop.imagePath?.takeIf { it.isNotBlank() },
                                contentDescription = "Imagen de ${crop.name}",
                                placeholder = fallback, fallback = fallback, error = fallback,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.size(64.dp).clip(RoundedCornerShape(10.dp))
                            )
                            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Text("Cultivo", style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer)
                                Text(
                                    text = if (crop.variety.isNullOrBlank()) crop.name
                                        else "${crop.name} · ${crop.variety}",
                                    style = MaterialTheme.typography.bodyLarge
                                )
                            }
                        }
                    }

                    content()

                    val canSave = !isLoading && !isSaving && !isSaved
                    Button(
                        onClick = {
                            focusManager.clearFocus()
                            onSaveClick()
                        },
                        enabled = canSave,
                        modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
                        shape = RoundedCornerShape(14.dp),
                        contentPadding = PaddingValues(0.dp),
                        elevation = ButtonDefaults.buttonElevation(defaultElevation = 3.dp, pressedElevation = 1.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth().background(Brush.verticalGradient(
                                if (canSave) listOf(
                                    lerp(MaterialTheme.colorScheme.primary, MaterialTheme.colorScheme.secondary, 0.3f),
                                    MaterialTheme.colorScheme.primary
                                ) else listOf(MaterialTheme.colorScheme.surfaceVariant, MaterialTheme.colorScheme.surfaceVariant)
                            )).heightIn(min = 56.dp).padding(horizontal = 16.dp, vertical = 12.dp),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            if (isSaving) {
                                CircularProgressIndicator(modifier = Modifier.size(24.dp), strokeWidth = 2.dp,
                                    color = MaterialTheme.colorScheme.primary)
                            } else {
                                Text("Guardar", style = MaterialTheme.typography.bodyLarge)
                                Icon(painterResource(R.drawable.ic_chevron_right), null,
                                    modifier = Modifier.padding(start = 12.dp).size(24.dp))
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun RecordFormHeader(title: String, subtitle: String, enabled: Boolean, onBackClick: () -> Unit) {
    Box(modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(bottomStart = 16.dp, bottomEnd = 16.dp))) {
        Image(
            painterResource(R.drawable.header_background), contentDescription = null,
            contentScale = ContentScale.Crop, alignment = Alignment.TopCenter,
            modifier = Modifier.matchParentSize()
        )
        Box(modifier = Modifier.matchParentSize().background(Brush.verticalGradient(listOf(
            MaterialTheme.colorScheme.background.copy(alpha = 0.15f),
            MaterialTheme.colorScheme.background.copy(alpha = 0.95f)
        ))))
        Column(
            modifier = Modifier.fillMaxWidth().statusBarsPadding().padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            TextButton(onClick = onBackClick, enabled = enabled, contentPadding = PaddingValues(0.dp)) {
                Surface(shape = CircleShape, color = MaterialTheme.colorScheme.background.copy(alpha = 0.94f)) {
                    Box(modifier = Modifier.size(48.dp), contentAlignment = Alignment.Center) {
                        Icon(painterResource(R.drawable.ic_chevron_right), null,
                            modifier = Modifier.size(28.dp).rotate(180f))
                    }
                }
                Spacer(Modifier.width(8.dp))
                Text("Volver", style = MaterialTheme.typography.bodyMedium)
            }
            Text(title, style = MaterialTheme.typography.titleLarge)
            Text(subtitle, style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
fun RecordNumericField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    error: String?,
    enabled: Boolean,
    @DrawableRes iconRes: Int
) {
    RecordFieldContainer(label, iconRes, allowHorizontal = true) {
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            placeholder = { Text("Ingresa un valor", style = MaterialTheme.typography.bodySmall) },
            modifier = Modifier.fillMaxWidth().semantics { contentDescription = label },
            textStyle = MaterialTheme.typography.bodyLarge,
            enabled = enabled,
            isError = error != null,
            supportingText = error?.let { message -> { Text(message) } },
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal, imeAction = ImeAction.Next),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = MaterialTheme.colorScheme.surface,
                unfocusedContainerColor = MaterialTheme.colorScheme.surface
            ),
            shape = RoundedCornerShape(10.dp)
        )
    }
}

@Composable
fun RecordTextField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    enabled: Boolean,
    @DrawableRes iconRes: Int,
    minLines: Int = 1
) {
    RecordFieldContainer(label, iconRes, allowHorizontal = false) {
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            placeholder = { Text("Escribe aquí…", style = MaterialTheme.typography.bodySmall) },
            modifier = Modifier.fillMaxWidth().semantics { contentDescription = label },
            textStyle = MaterialTheme.typography.bodyLarge,
            enabled = enabled,
            singleLine = minLines == 1,
            minLines = minLines,
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = MaterialTheme.colorScheme.surface,
                unfocusedContainerColor = MaterialTheme.colorScheme.surface
            ),
            shape = RoundedCornerShape(10.dp)
        )
    }
}

@Composable
private fun RecordFieldContainer(
    label: String,
    @DrawableRes iconRes: Int,
    allowHorizontal: Boolean,
    content: @Composable () -> Unit
) {
    val fontScale = LocalDensity.current.fontScale
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.outline)
    ) {
        BoxWithConstraints(modifier = Modifier.padding(10.dp)) {
            if (allowHorizontal && maxWidth >= 320.dp && fontScale <= 1.2f) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(modifier = Modifier.weight(1f), verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        RecordFieldIcon(iconRes)
                        Text(label, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.weight(1f))
                    }
                    Box(modifier = Modifier.weight(1.1f)) { content() }
                }
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        RecordFieldIcon(iconRes)
                        Text(label, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.weight(1f))
                    }
                    content()
                }
            }
        }
    }
}

@Composable
private fun RecordFieldIcon(@DrawableRes iconRes: Int) {
    Surface(shape = RoundedCornerShape(10.dp), color = MaterialTheme.colorScheme.primaryContainer) {
        Box(modifier = Modifier.size(44.dp), contentAlignment = Alignment.Center) {
            Image(painterResource(iconRes), null, modifier = Modifier.requiredSize(64.dp))
        }
    }
}
