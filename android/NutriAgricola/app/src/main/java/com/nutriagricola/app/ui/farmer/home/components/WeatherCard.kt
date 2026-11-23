package com.nutriagricola.app.ui.farmer.home.components

import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.nutriagricola.app.R
import com.nutriagricola.app.domain.model.weather.WeatherInfo
import kotlin.math.roundToInt

@Composable
fun WeatherCard(weatherState: WeatherUiState, onRetry: () -> Unit, modifier: Modifier = Modifier) {
    Column(modifier.fillMaxWidth().padding(4.dp), verticalArrangement = Arrangement.spacedBy(18.dp)) {
        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text("Clima", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text("Santiago", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        }
        when (weatherState) {
            WeatherUiState.Loading -> Surface(shape = RoundedCornerShape(24.dp),
                color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f), modifier = Modifier.fillMaxWidth()) {
                Column(Modifier.padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    WeatherIllustration(Modifier.size(92.dp))
                    CircularProgressIndicator(Modifier.size(22.dp), strokeWidth = 2.dp)
                }
            }
            is WeatherUiState.Error -> Surface(shape = RoundedCornerShape(24.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f), modifier = Modifier.fillMaxWidth()) {
                Column(Modifier.padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    WeatherIllustration(Modifier.size(92.dp))
                    Text("Clima no disponible", style = MaterialTheme.typography.titleMedium)
                    TextButton(onClick = onRetry) { Text("Reintentar") }
                }
            }
            is WeatherUiState.Success -> WeatherConditions(weatherState.data)
        }
        Text("Open-Meteo", style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.align(Alignment.End))
    }
}

@Composable
private fun WeatherConditions(info: WeatherInfo) {
    val largeText = LocalDensity.current.fontScale > 1.3f
    Card(shape = RoundedCornerShape(24.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.fillMaxWidth().background(Brush.linearGradient(listOf(
            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.55f),
            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.25f)
        ))).padding(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text("Ahora", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(info.temperature?.let { "${it.roundToInt()}°" } ?: "—", fontSize = 58.sp,
                        lineHeight = 64.sp, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.primary)
                    Text(info.getWeatherDescription(), style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                if (!largeText) WeatherIllustration(Modifier.size(116.dp))
            }
            if (info.tempMin != null || info.tempMax != null) {
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    info.tempMin?.let { TemperaturePill("Mín ${it.roundToInt()}°") }
                    info.tempMax?.let { TemperaturePill("Máx ${it.roundToInt()}°") }
                }
            }
        }
    }
    if (largeText) {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            WeatherMetric("Humedad", info.relativeHumidity?.let { "$it %" }, R.drawable.ic_weather_humidity, Modifier.fillMaxWidth())
            WeatherMetric("Viento", info.windSpeed?.let { "${it.roundToInt()} km/h" }, R.drawable.ic_weather_wind, Modifier.fillMaxWidth())
            WeatherMetric("Sensación", info.apparentTemperature?.let { "${it.roundToInt()} °C" }, R.drawable.ic_weather_temperature, Modifier.fillMaxWidth())
        }
    } else {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            WeatherMetric("Humedad", info.relativeHumidity?.let { "$it %" }, R.drawable.ic_weather_humidity, Modifier.weight(1f))
            WeatherMetric("Viento", info.windSpeed?.let { "${it.roundToInt()} km/h" }, R.drawable.ic_weather_wind, Modifier.weight(1f))
            WeatherMetric("Sensación", info.apparentTemperature?.let { "${it.roundToInt()} °C" }, R.drawable.ic_weather_temperature, Modifier.weight(1f))
        }
    }
}

@Composable
private fun WeatherMetric(label: String, value: String?, @DrawableRes icon: Int, modifier: Modifier) {
    Surface(modifier, shape = RoundedCornerShape(18.dp), color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)) {
        Column(Modifier.padding(horizontal = 10.dp, vertical = 14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Icon(painterResource(icon), null, Modifier.size(22.dp), tint = MaterialTheme.colorScheme.primary)
            Text(value ?: "Sin datos", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
            Text(label, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun TemperaturePill(text: String) {
    Surface(shape = RoundedCornerShape(50), color = MaterialTheme.colorScheme.surface.copy(alpha = 0.75f)) {
        Text(text, Modifier.padding(horizontal = 12.dp, vertical = 6.dp), style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.primary)
    }
}

@Composable
private fun WeatherIllustration(modifier: Modifier) {
    Image(painterResource(R.drawable.weather_sun_cloud), null, modifier, contentScale = ContentScale.Fit)
}

sealed interface WeatherUiState {
    data object Loading : WeatherUiState
    data class Success(val data: WeatherInfo) : WeatherUiState
    data class Error(val message: String) : WeatherUiState
}
