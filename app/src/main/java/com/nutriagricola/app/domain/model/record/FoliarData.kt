package com.nutriagricola.app.domain.model.record

data class FoliarData(
    val nitrogen: Double? = null,
    val phosphorus: Double? = null,
    val potassium: Double? = null,
    val calcium: Double? = null,
    val magnesium: Double? = null,
    val symptoms: String? = null,
    val photos: List<String> = emptyList()
) : RecordData