package com.nutriagricola.app.data.mapper

import com.nutriagricola.app.data.local.entity.WaterDataEntity
import com.nutriagricola.app.domain.model.record.WaterData

fun WaterDataEntity.toDomain(): WaterData {
    return WaterData(
        ph = ph,
        electricalConductivity = electricalConductivity,
        nitrogen = nitrogen,
        phosphorus = phosphorus,
        potassium = potassium,
        calcium = calcium,
        magnesium = magnesium,
        waterSource = waterSource
    )
}

fun WaterData.toEntity(recordId: String): WaterDataEntity {
    return WaterDataEntity(
        recordId = recordId,
        ph = ph,
        electricalConductivity = electricalConductivity,
        nitrogen = nitrogen,
        phosphorus = phosphorus,
        potassium = potassium,
        calcium = calcium,
        magnesium = magnesium,
        waterSource = waterSource
    )
}