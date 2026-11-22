package com.nutriagricola.app.data.remote

import android.os.Build

import com.nutriagricola.app.data.remote.api.AuthApi
import com.nutriagricola.app.data.remote.api.FarmApi
import com.nutriagricola.app.data.remote.api.CropApi
import com.nutriagricola.app.data.remote.api.RecordApi
import com.nutriagricola.app.data.remote.api.ReviewApi
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory
import java.util.concurrent.TimeUnit

class ApiClient(baseUrl: String = defaultBaseUrl()) {
    companion object {
        private fun defaultBaseUrl(): String =
            if (Build.HARDWARE in listOf("ranchu", "goldfish")) {
                // Android Studio emulator: direct access to the development computer.
                "http://10.0.2.2:8080/"
            } else {
                // Physical phone over USB: adb reverse tcp:8080 tcp:8080.
                "http://127.0.0.1:8080/"
            }
    }

    private val json = Json { ignoreUnknownKeys = true }

    private val httpClient = OkHttpClient.Builder()
        .connectTimeout(10, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS)
        .callTimeout(20, TimeUnit.SECONDS)
        .build()

    private val retrofit = Retrofit.Builder()
        .baseUrl(baseUrl)
        .client(httpClient)
        .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
        .build()

    val authApi: AuthApi = retrofit.create(AuthApi::class.java)
    val farmApi: FarmApi = retrofit.create(FarmApi::class.java)
    val cropApi: CropApi = retrofit.create(CropApi::class.java)
    val recordApi: RecordApi = retrofit.create(RecordApi::class.java)
    val reviewApi: ReviewApi = retrofit.create(ReviewApi::class.java)
}
