package com.nutriagricola.app.data.mapper

import com.nutriagricola.app.data.local.entity.FoliarDataEntity
import com.nutriagricola.app.domain.model.record.FoliarData

fun FoliarDataEntity.toDomain(
    photos: List<String> = emptyList()
): FoliarData {
    return FoliarData(
        nitrogen = nitrogen,
        phosphorus = phosphorus,
        potassium = potassium,
        calcium = calcium,
        magnesium = magnesium,
        symptoms = symptoms,
        photos = photos
    )
}

fun FoliarData.toEntity(recordId: String): FoliarDataEntity {
    return FoliarDataEntity(
        recordId = recordId,
        nitrogen = nitrogen,
        phosphorus = phosphorus,
        potassium = potassium,
        calcium = calcium,
        magnesium = magnesium,
        symptoms = symptoms
    )
}