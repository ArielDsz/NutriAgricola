package com.nutriagricola.app.domain.model.advisor

import com.nutriagricola.app.domain.model.record.RecordStatus
import java.time.LocalDateTime

data class RecordReview(
    val id: String,
    val recordId: String,
    val advisorId: String,
    val advisorName: String,
    val date: LocalDateTime,
    val result: RecordStatus,
    val comment: String
)
