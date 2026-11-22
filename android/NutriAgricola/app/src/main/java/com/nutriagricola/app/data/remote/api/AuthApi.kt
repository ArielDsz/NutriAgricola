package com.nutriagricola.app.data.remote.api

import com.nutriagricola.app.data.remote.dto.AuthResponseDto
import com.nutriagricola.app.data.remote.dto.LoginRequestDto
import com.nutriagricola.app.data.remote.dto.UserResponseDto
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST

interface AuthApi {
    @POST("api/v1/auth/login")
    suspend fun login(@Body request: LoginRequestDto): AuthResponseDto

    @GET("api/v1/users/me")
    suspend fun getCurrentUser(@Header("Authorization") authorization: String): UserResponseDto
}
