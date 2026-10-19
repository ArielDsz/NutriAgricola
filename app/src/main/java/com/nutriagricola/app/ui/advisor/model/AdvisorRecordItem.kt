package com.nutriagricola.app.ui.advisor.model

import com.nutriagricola.app.domain.model.record.Record

data class AdvisorRecordItem(
    val record: Record,
    val farmName: String,
    val cropName: String,
    val farmerName: String
)
