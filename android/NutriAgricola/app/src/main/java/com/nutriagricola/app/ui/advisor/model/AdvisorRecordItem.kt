package com.nutriagricola.app.ui.advisor.model

import com.nutriagricola.app.domain.model.record.Record
import com.nutriagricola.app.domain.model.advisor.ReviewResult

data class AdvisorRecordItem(
    val record: Record,
    val farmName: String,
    val cropName: String,
    val farmerName: String,
    val farmId: String? = null,
    val reviewResult: ReviewResult? = null
)
