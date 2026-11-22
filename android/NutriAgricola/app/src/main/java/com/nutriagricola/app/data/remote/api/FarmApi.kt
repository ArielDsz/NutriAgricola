package com.nutriagricola.app.data.remote.api

import com.nutriagricola.app.data.remote.dto.FarmResponseDto
import retrofit2.http.GET
import retrofit2.http.Header

interface FarmApi {
    @GET("api/v1/farms")
    suspend fun getFarms(@Header("Authorization") authorization: String): List<FarmResponseDto>
}
