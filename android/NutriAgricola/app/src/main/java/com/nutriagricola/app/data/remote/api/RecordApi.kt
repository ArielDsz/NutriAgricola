package com.nutriagricola.app.data.remote.api

import com.nutriagricola.app.data.remote.dto.RecordDetailDto
import com.nutriagricola.app.data.remote.dto.RecordPageDto
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Path
import retrofit2.http.Query

interface RecordApi {
    @GET("api/v1/records")
    suspend fun getHistory(@Header("Authorization") authorization: String,
                          @Query("page") page: Int, @Query("size") size: Int = 20): RecordPageDto

    @GET("api/v1/records/{id}")
    suspend fun getDetail(@Path("id") id: String,
                         @Header("Authorization") authorization: String): RecordDetailDto
}
