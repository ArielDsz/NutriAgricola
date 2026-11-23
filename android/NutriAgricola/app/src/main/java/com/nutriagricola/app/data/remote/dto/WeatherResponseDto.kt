package com.nutriagricola.app.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class WeatherResponseDto(
    val latitude: Double? = null,
    val longitude: Double? = null,
    val timezone: String? = null,
    val current: CurrentWeatherDto? = null,
    val daily: DailyWeatherDto? = null
)

@Serializable
data class CurrentWeatherDto(
    val time: String? = null,
    @SerialName("temperature_2m")
    val temperature2m: Double? = null,
    @SerialName("relative_humidity_2m")
    val relativeHumidity2m: Int? = null,
    @SerialName("apparent_temperature")
    val apparentTemperature: Double? = null,
    val precipitation: Double? = null,
    val rain: Double? = null,
    @SerialName("weather_code")
    val weatherCode: Int? = null,
    @SerialName("wind_speed_10m")
    val windSpeed10m: Double? = null
)

@Serializable
data class DailyWeatherDto(
    val time: List<String>? = null,
    @SerialName("temperature_2m_max")
    val temperature2mMax: List<Double>? = null,
    @SerialName("temperature_2m_min")
    val temperature2mMin: List<Double>? = null,
    @SerialName("precipitation_sum")
    val precipitationSum: List<Double>? = null,
    @SerialName("precipitation_probability_max")
    val precipitationProbabilityMax: List<Int>? = null
)
