package com.nutriagricola.app.data.remote.api

import com.nutriagricola.app.data.remote.dto.*
import retrofit2.http.*

interface ProgramApi {
    @GET("api/v1/programs")
    suspend fun list(@Header("Authorization") authorization: String, @Query("page") page: Int,
                     @Query("size") size: Int = 20): ProgramPageDto
    @GET("api/v1/programs/{id}")
    suspend fun get(@Header("Authorization") authorization: String, @Path("id") id: String): ProgramResponseDto
    @POST("api/v1/programs")
    suspend fun create(@Header("Authorization") authorization: String, @Body request: ProgramRequestDto): ProgramResponseDto

    @PUT("api/v1/programs/{id}")
    suspend fun update(@Header("Authorization") authorization: String, @Path("id") id: String,
                       @Body request: ProgramRequestDto): ProgramResponseDto

    @DELETE("api/v1/programs/{id}")
    suspend fun archive(@Header("Authorization") authorization: String, @Path("id") id: String)
}
