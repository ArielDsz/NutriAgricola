package com.nutriagricola.app.domain.model.record

import java.time.LocalDate
import java.util.UUID

data class Record(
    val id: String = UUID.randomUUID().toString(),
    val cropId: String,
    val userId: String,
    val date: LocalDate = LocalDate.now(),
    val type: RecordType,
    val status: RecordStatus = RecordStatus.DRAFT,
    val observations: String? = null,
    val data: RecordData
)