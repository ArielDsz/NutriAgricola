package com.nutriagricola.app.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class ReviewCreateRequestDto(val id: String, val recordId: String, val date: String,
                                  val result: String, val comment: String)

@Serializable
data class ReviewCreateResponseDto(val id: String, val recordId: String, val advisorId: String,
    val advisorName: String, val date: String, val result: String, val comment: String,
    val recordStatus: String)
