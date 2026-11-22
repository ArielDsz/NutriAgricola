package com.nutriagricola.app.data.remote.api

import com.nutriagricola.app.data.remote.dto.ReviewCreateRequestDto
import com.nutriagricola.app.data.remote.dto.ReviewCreateResponseDto
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST

interface ReviewApi {
    @POST("api/v1/reviews")
    suspend fun create(@Header("Authorization") authorization: String,
                       @Body request: ReviewCreateRequestDto): ReviewCreateResponseDto
}
