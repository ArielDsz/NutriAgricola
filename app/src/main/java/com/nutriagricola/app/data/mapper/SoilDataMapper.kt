package com.nutriagricola.app.data.mapper

import com.nutriagricola.app.data.local.entity.SoilDataEntity
import com.nutriagricola.app.domain.model.record.SoilData

fun SoilDataEntity.toDomain(): SoilData {
    return SoilData(
        ph = ph,
        electricalConductivity = electricalConductivity,
        nitrogen = nitrogen,
        phosphorus = phosphorus,
        potassium = potassium,
        calcium = calcium,
        magnesium = magnesium,
        organicMatter = organicMatter
    )
}

fun SoilData.toEntity(recordId: String): SoilDataEntity {
    return SoilDataEntity(
        recordId = recordId,
        ph = ph,
        electricalConductivity = electricalConductivity,
        nitrogen = nitrogen,
        phosphorus = phosphorus,
        potassium = potassium,
        calcium = calcium,
        magnesium = magnesium,
        organicMatter = organicMatter
    )
}