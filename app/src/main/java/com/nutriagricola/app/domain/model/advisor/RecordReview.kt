package com.nutriagricola.app.domain.model.advisor

import java.time.LocalDateTime

data class RecordReview(
    val id: String,
    val recordId: String,
    val advisorId: String,
    val advisorName: String,
    val date: LocalDateTime,
    val result: ReviewResult,
    val comment: String
)
