package com.nutriagricola.app.domain.model.weather

data class WeatherInfo(
    val temperature: Double,
    val relativeHumidity: Int,
    val apparentTemperature: Double,
    val precipitation: Double,
    val weatherCode: Int,
    val windSpeed: Double,
    val tempMax: Double?,
    val tempMin: Double?,
    val rainProbability: Int?
) {
    fun getWeatherDescription(): String {
        return when (weatherCode) {
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

    fun getAgriculturalAdvice(): String {
        return when {
            precipitation > 1.0 || (rainProbability ?: 0) >= 60 ->
                "⚠️ Alta probabilidad de lluvia. Se recomienda posponer la aplicación de fertilizantes foliares para evitar lavado."
            windSpeed > 20.0 ->
                "⚠️ Vientos fuertes (>20 km/h). No se recomienda pulverización foliar por deriva."
            temperature > 30.0 ->
                "🌡️ Alta temperatura. Realizar riegos temprano en la mañana o al atardecer."
            else ->
                "✅ Condiciones meteorológicas favorables para labores agrícolas y aplicación de foliares."
        }
    }
}
