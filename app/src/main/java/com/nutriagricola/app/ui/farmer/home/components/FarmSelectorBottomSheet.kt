package com.nutriagricola.app.ui.farmer.home.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import com.nutriagricola.app.domain.model.farm.Farm

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FarmSelectorBottomSheet(
    farms: List<Farm>,
    selectedFarmId: String?,
    onDismiss: () -> Unit,
    onFarmSelected: (String) -> Unit
) {
    ModalBottomSheet(onDismissRequest = onDismiss) {
        Text(
            "Seleccionar predio",
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
        )
        LazyColumn(
            modifier = Modifier.fillMaxWidth().heightIn(max = 400.dp).selectableGroup(),
            contentPadding = PaddingValues(bottom = 24.dp)
        ) {
            if (farms.isEmpty()) {
                item { Text("No hay predios disponibles", modifier = Modifier.padding(16.dp)) }
            }
            items(farms, key = { it.id }) { farm ->
                val selected = farm.id == selectedFarmId
                Row(
                    modifier = Modifier.fillMaxWidth().selectable(
                        selected = selected,
                        role = Role.RadioButton,
                        onClick = { onFarmSelected(farm.id) }
                    ).padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    RadioButton(selected = selected, onClick = null)
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(farm.name, style = MaterialTheme.typography.titleMedium)
                        farm.location?.takeIf { it.isNotBlank() }?.let {
                            Text(it, style = MaterialTheme.typography.bodyMedium)
                        }
                        if (selected) {
                            Text("Seleccionado", style = MaterialTheme.typography.labelMedium)
                        }
                    }
                }
            }
        }
    }
}
