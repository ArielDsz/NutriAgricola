package com.nutriagricola.app.data.remote.api

import com.nutriagricola.app.data.remote.dto.RecordDetailDto
import com.nutriagricola.app.data.remote.dto.RecordPageDto
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Path
import retrofit2.http.Query
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Body
import retrofit2.http.Multipart
import retrofit2.http.Part
import retrofit2.http.Streaming
import okhttp3.MultipartBody
import okhttp3.ResponseBody
import com.nutriagricola.app.data.remote.dto.*

interface RecordApi {
    @POST("api/v1/records")
    suspend fun create(@Header("Authorization") authorization: String,
                       @Body request: RecordCreateDto): RecordCreateResponseDto

    @PUT("api/v1/records/{id}")
    suspend fun update(@Path("id") id: String, @Header("Authorization") authorization: String,
                       @Body request: RecordUpdateDto): RecordSummaryDto

    @Multipart
    @PUT("api/v1/records/{recordId}/photos/{photoId}")
    suspend fun uploadPhoto(@Path("recordId") recordId: String, @Path("photoId") photoId: String,
        @Header("Authorization") authorization: String, @Part file: MultipartBody.Part): PhotoResponseDto

    @Streaming
    @GET("api/v1/records/{recordId}/photos/{photoId}")
    suspend fun downloadPhoto(@Path("recordId") recordId: String, @Path("photoId") photoId: String,
        @Header("Authorization") authorization: String): ResponseBody

    @GET("api/v1/records")
    suspend fun getHistory(@Header("Authorization") authorization: String,
                          @Query("page") page: Int, @Query("size") size: Int = 20): RecordPageDto

    @GET("api/v1/records/{id}")
    suspend fun getDetail(@Path("id") id: String,
                         @Header("Authorization") authorization: String): RecordDetailDto
}
