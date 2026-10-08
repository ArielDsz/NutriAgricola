package com.nutriagricola.app.domain.model.record

data class SoilData(
    val ph: Double? = null,
    val electricalConductivity: Double? = null,
    val nitrogen: Double? = null,
    val phosphorus: Double? = null,
    val potassium: Double? = null,
    val calcium: Double? = null,
    val magnesium: Double? = null,
    val organicMatter: Double? = null
) : RecordData