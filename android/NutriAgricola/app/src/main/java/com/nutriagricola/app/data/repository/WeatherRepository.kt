package com.nutriagricola.app.data.repository

import com.nutriagricola.app.data.remote.WeatherApiClient
import com.nutriagricola.app.data.remote.api.WeatherApi
import com.nutriagricola.app.domain.model.weather.WeatherInfo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.CancellationException

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
                temperature = current.temperature2m,
                relativeHumidity = current.relativeHumidity2m,
                apparentTemperature = current.apparentTemperature,
                precipitation = current.precipitation,
                weatherCode = current.weatherCode,
                windSpeed = current.windSpeed10m,
                tempMax = daily?.temperature2mMax?.firstOrNull(),
                tempMin = daily?.temperature2mMin?.firstOrNull(),
                rainProbability = daily?.precipitationProbabilityMax?.firstOrNull()
            )

            Result.success(weatherInfo)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
