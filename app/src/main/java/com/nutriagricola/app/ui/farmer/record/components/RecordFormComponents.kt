package com.nutriagricola.app.ui.farmer.record.components

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.nutriagricola.app.domain.model.farm.Crop
import com.nutriagricola.app.ui.components.dismissKeyboardOnBackgroundTap

@Composable
fun RecordForm(
    title: String,
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
            modifier = Modifier.fillMaxSize().padding(innerPadding).imePadding()
                .verticalScroll(rememberScrollState()).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            TextButton(onClick = onBackClick, enabled = !isSaving) {
                Text("← Volver")
            }
            Text(title, style = MaterialTheme.typography.titleLarge)

            if (isLoading) CircularProgressIndicator()
            error?.let { Text(it, color = MaterialTheme.colorScheme.error) }

            if (crop != null) {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.primaryContainer
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text("Cultivo", style = MaterialTheme.typography.labelMedium)
                        Text(
                            text = if (crop.variety.isNullOrBlank()) crop.name
                            else "${crop.name} - ${crop.variety}",
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                }

                content()

                Button(
                    onClick = {
                        focusManager.clearFocus()
                        onSaveClick()
                    },
                    enabled = !isLoading && !isSaving && !isSaved,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    if (isSaving) {
                        CircularProgressIndicator(modifier = Modifier.size(24.dp), strokeWidth = 2.dp)
                    } else {
                        Text("Guardar")
                    }
                }
            }
        }
    }
}

@Composable
fun RecordNumericField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    error: String?,
    enabled: Boolean
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        modifier = Modifier.fillMaxWidth(),
        enabled = enabled,
        isError = error != null,
        supportingText = error?.let { message -> { Text(message) } },
        singleLine = true,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal, imeAction = ImeAction.Next),
        shape = RoundedCornerShape(12.dp)
    )
}

@Composable
fun RecordTextField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    enabled: Boolean,
    minLines: Int = 1
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        modifier = Modifier.fillMaxWidth(),
        enabled = enabled,
        singleLine = minLines == 1,
        minLines = minLines,
        shape = RoundedCornerShape(12.dp)
    )
}
