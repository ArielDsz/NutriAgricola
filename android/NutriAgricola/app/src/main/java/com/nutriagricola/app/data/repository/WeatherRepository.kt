package com.nutriagricola.app.data.repository

import com.nutriagricola.app.data.remote.WeatherApiClient
import com.nutriagricola.app.data.remote.api.WeatherApi
import com.nutriagricola.app.domain.model.weather.WeatherInfo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class WeatherRepository(
    private val weatherApi: WeatherApi = WeatherApiClient.api
) {
    suspend fun getWeather(
        latitude: Double = -33.45,
        longitude: Double = -70.66
    ): Result<WeatherInfo> = withContext(Dispatchers.IO) {
        try {
            val response = weatherApi.getWeatherForecast(
                latitude = latitude,
                longitude = longitude
            )
            val current = response.current
            val daily = response.daily

            if (current == null) {
                return@withContext Result.failure(Exception("Datos climáticos no disponibles"))
            }

            val weatherInfo = WeatherInfo(
                temperature = current.temperature2m ?: 0.0,
                relativeHumidity = current.relativeHumidity2m ?: 0,
                apparentTemperature = current.apparentTemperature ?: current.temperature2m ?: 0.0,
                precipitation = current.precipitation ?: 0.0,
                weatherCode = current.weatherCode ?: 0,
                windSpeed = current.windSpeed10m ?: 0.0,
                tempMax = daily?.temperature2mMax?.firstOrNull(),
                tempMin = daily?.temperature2mMin?.firstOrNull(),
                rainProbability = daily?.precipitationProbabilityMax?.firstOrNull()
            )

            Result.success(weatherInfo)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
