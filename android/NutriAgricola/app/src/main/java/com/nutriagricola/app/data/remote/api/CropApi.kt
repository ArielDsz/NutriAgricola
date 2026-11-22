package com.nutriagricola.app.data.remote.api

import com.nutriagricola.app.data.remote.dto.CropResponseDto
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Path

interface CropApi {
    @GET("api/v1/crops/{id}")
    suspend fun getById(@Path("id") id: String, @Header("Authorization") authorization: String): CropResponseDto

    @GET("api/v1/crops/farm/{farmId}")
    suspend fun getByFarm(@Path("farmId") farmId: String, @Header("Authorization") authorization: String): List<CropResponseDto>
}
