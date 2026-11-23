package com.nutriagricola.app.domain.model.weather

data class WeatherInfo(
    val temperature: Double?,
    val relativeHumidity: Int?,
    val apparentTemperature: Double?,
    val precipitation: Double?,
    val weatherCode: Int?,
    val windSpeed: Double?,
    val tempMax: Double?,
    val tempMin: Double?,
    val rainProbability: Int?
) {
    fun getWeatherDescription(): String {
        return when (weatherCode) {
            null -> "Sin descripción disponible"
            0 -> "Cielo despejado"
            1, 2, 3 -> "Parcialmente nublado"
            45, 48 -> "Neblina"
            51, 53, 55 -> "Llovizna ligera"
            61, 63, 65 -> "Lluvia"
            71, 73, 75 -> "Nieve"
            80, 81, 82 -> "Chubascos"
            95, 96, 99 -> "Tormenta eléctrica"
            else -> "Tiempo variable"
        }
    }

}
