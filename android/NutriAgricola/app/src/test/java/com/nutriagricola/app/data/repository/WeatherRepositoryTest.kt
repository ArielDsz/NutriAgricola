package com.nutriagricola.app.data.repository

import com.nutriagricola.app.data.remote.api.WeatherApi
import com.nutriagricola.app.data.remote.dto.CurrentWeatherDto
import com.nutriagricola.app.data.remote.dto.WeatherResponseDto
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Test

class WeatherRepositoryTest {
    @Test fun `missing weather values remain absent while actual zeros stay valid`() = runBlocking {
        val repository = WeatherRepository(api { WeatherResponseDto(current = CurrentWeatherDto(temperature2m = 0.0)) })
        val result = repository.getWeather().getOrThrow()
        assertEquals(0.0, result.temperature!!, 0.0)
        assertNull(result.relativeHumidity)
        assertNull(result.apparentTemperature)
        assertNull(result.weatherCode)
        assertEquals("Sin descripción disponible", result.getWeatherDescription())
    }

    @Test fun `absent current weather is reported as failure`() = runBlocking {
        assertTrue(WeatherRepository(api { WeatherResponseDto() }).getWeather().isFailure)
    }

    @Test fun `cancelled requests are not converted into weather errors`() = runBlocking {
        try {
            WeatherRepository(api { throw CancellationException("Cancelada") }).getWeather()
            fail("Debe propagarse la cancelación")
        } catch (expected: CancellationException) {
            assertEquals("Cancelada", expected.message)
        }
    }

    private fun api(response: suspend () -> WeatherResponseDto) = object : WeatherApi {
        override suspend fun getWeatherForecast(latitude: Double, longitude: Double, current: String,
                                                daily: String, timezone: String) = response()
    }
}
