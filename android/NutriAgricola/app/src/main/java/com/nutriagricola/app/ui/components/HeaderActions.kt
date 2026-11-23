package com.nutriagricola.app.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.nutriagricola.app.R
import com.nutriagricola.app.domain.model.user.User
import com.nutriagricola.app.ui.farmer.home.components.WeatherCard
import com.nutriagricola.app.ui.weather.WeatherViewModel

fun userInitials(user: User): String = listOf(user.firstName, user.lastName)
    .mapNotNull { it.trim().firstOrNull()?.toString() }.joinToString("").uppercase()

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HeaderActions(initials: String, onLogout: () -> Unit) {
    val weather: WeatherViewModel = viewModel()
    val weatherState by weather.state.collectAsState()
    var showWeather by rememberSaveable { mutableStateOf(false) }
    var accountExpanded by remember { mutableStateOf(false) }
    LaunchedEffect(showWeather) { if (showWeather) weather.load() }
    if (showWeather) ModalBottomSheet(onDismissRequest = { showWeather = false }, containerColor = MaterialTheme.colorScheme.surface) {
        WeatherCard(weatherState, onRetry = weather::load,
            modifier = Modifier.fillMaxWidth().padding(start = 16.dp, end = 16.dp, bottom = 24.dp))
    }
    Row(verticalAlignment = Alignment.CenterVertically) {
        IconButton(onClick = { showWeather = true }, modifier = Modifier.size(48.dp)) {
            Image(painterResource(R.drawable.weather_sun_cloud), "Ver clima", Modifier.size(44.dp))
        }
        Box(Modifier.size(48.dp), contentAlignment = Alignment.Center) {
            Surface(onClick = { accountExpanded = true }, shape = CircleShape,
                color = MaterialTheme.colorScheme.primary, modifier = Modifier.size(40.dp)) {
                Box(contentAlignment = Alignment.Center) {
                    Text(initials, color = MaterialTheme.colorScheme.onPrimary,
                        style = MaterialTheme.typography.bodyLarge.copy(fontSize = MaterialTheme.typography.bodyLarge.fontSize * 0.85f))
                }
            }
            DropdownMenu(expanded = accountExpanded, onDismissRequest = { accountExpanded = false },
                containerColor = MaterialTheme.colorScheme.surface) {
                DropdownMenuItem(text = { Text("Cerrar sesión", color = MaterialTheme.colorScheme.primary) },
                    onClick = { accountExpanded = false; onLogout() })
            }
        }
    }
}
