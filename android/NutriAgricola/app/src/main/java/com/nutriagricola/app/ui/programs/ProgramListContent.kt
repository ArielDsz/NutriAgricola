package com.nutriagricola.app.ui.programs

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.nutriagricola.app.R
import com.nutriagricola.app.domain.model.program.NutritionalProgram
import com.nutriagricola.app.domain.model.user.UserRole
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@Composable
fun ProgramListContent(viewModel: ProgramsViewModel, modifier: Modifier = Modifier, headerActions: @Composable () -> Unit = {}) {
    val state by viewModel.state.collectAsState()
    var filtersExpanded by remember { mutableStateOf(false) }
    LazyColumn(modifier, contentPadding = PaddingValues(bottom = 16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        item {
            Box(Modifier.fillMaxWidth().heightIn(min = 160.dp)
                .clip(RoundedCornerShape(bottomStart = 20.dp, bottomEnd = 20.dp))) {
                Image(painterResource(R.drawable.header_background), null,
                    contentScale = ContentScale.Crop, alignment = Alignment.TopCenter,
                    modifier = Modifier.matchParentSize())
                Row(Modifier.fillMaxWidth().statusBarsPadding().padding(start = 24.dp, end = 16.dp, top = 16.dp, bottom = 32.dp),
                    verticalAlignment = Alignment.CenterVertically) {
                    Text("Programas", style = MaterialTheme.typography.headlineLarge, modifier = Modifier.weight(1f))
                    if (viewModel.role == UserRole.ADVISOR) {
                        IconButton(onClick = viewModel::newProgram) {
                            Icon(painterResource(R.drawable.ic_add_circle), "Nuevo programa", tint = MaterialTheme.colorScheme.primary)
                        }
                    }
                    headerActions()
                }
            }
        }
        item {
            OutlinedTextField(state.query, viewModel::setQuery,
                placeholder = { Text("Buscar", style = MaterialTheme.typography.bodyMedium) },
                textStyle = MaterialTheme.typography.bodyMedium,
                leadingIcon = { Icon(painterResource(R.drawable.ic_search), null, Modifier.size(22.dp)) },
                trailingIcon = {
                    Box {
                        IconButton(onClick = { filtersExpanded = true }) {
                            Icon(painterResource(R.drawable.ic_expand_more), "Filtrar programas", Modifier.size(24.dp))
                        }
                        DropdownMenu(expanded = filtersExpanded, onDismissRequest = { filtersExpanded = false }) {
                            ProgramStatusFilter.entries.forEach { filter ->
                                DropdownMenuItem(text = { Text(filter.label) }, onClick = {
                                    viewModel.setStatusFilter(filter); filtersExpanded = false
                                }, trailingIcon = { if (filter == state.statusFilter) Icon(painterResource(R.drawable.ic_check), null, Modifier.size(18.dp)) })
                            }
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp), singleLine = true,
                shape = RoundedCornerShape(14.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = MaterialTheme.colorScheme.surface,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant,
                    focusedBorderColor = MaterialTheme.colorScheme.primary)
            )
        }
        if (state.loading) item { LinearProgressIndicator(Modifier.fillMaxWidth().padding(horizontal = 16.dp)) }
        state.error?.let { error -> item {
            Text(error, modifier = Modifier.padding(horizontal = 16.dp), color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall)
        } }
        if (!state.loading && state.visiblePrograms.isEmpty()) item {
            Text("No hay programas", modifier = Modifier.padding(24.dp), style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        items(state.visiblePrograms, key = { it.id }) { program ->
            Box(Modifier.padding(horizontal = 16.dp)) { ProgramHistoryCard(program, onClick = { viewModel.open(program.id) }) }
        }
        if (state.hasMore) item {
            Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                TextButton(onClick = viewModel::loadMore, enabled = !state.loading) { Text("Ver más") }
            }
        }
    }
}

@Composable
private fun ProgramHistoryCard(program: NutritionalProgram, onClick: () -> Unit) {
    Card(onClick = onClick, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(14.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.5.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
        Row(Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 14.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.size(56.dp)) {
                Image(painterResource(R.drawable.default_crop), null, contentScale = ContentScale.Crop,
                    modifier = Modifier.size(52.dp).clip(RoundedCornerShape(10.dp)))
                Surface(Modifier.size(28.dp).align(Alignment.BottomEnd), shape = CircleShape,
                    color = MaterialTheme.colorScheme.surface, shadowElevation = 1.dp) {
                    Icon(painterResource(R.drawable.ic_program), null, Modifier.padding(5.dp), tint = MaterialTheme.colorScheme.primary)
                }
            }
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(program.name, style = MaterialTheme.typography.bodyMedium, maxLines = 2, overflow = TextOverflow.Ellipsis)
                (program.species?.takeIf { it.isNotBlank() } ?: program.advisorName)?.let {
                    Text(it, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1, overflow = TextOverflow.Ellipsis)
                }
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(painterResource(R.drawable.ic_calendar), null, Modifier.size(14.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(runCatching { LocalDate.parse(program.startDate).format(DateTimeFormatter.ofPattern("dd/MM/yyyy")) }.getOrDefault(program.startDate),
                        style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            Column(horizontalAlignment = Alignment.End, verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Icon(painterResource(R.drawable.ic_chevron_right), null, Modifier.size(18.dp), tint = MaterialTheme.colorScheme.primary)
                Surface(shape = RoundedCornerShape(50), color = if (program.active) MaterialTheme.colorScheme.primaryContainer
                    else MaterialTheme.colorScheme.surfaceVariant) {
                    Text(if (program.active) "Activo" else "Archivado", style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface, modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp))
                }
            }
        }
    }
}
