package com.nutriagricola.app.data.mapper

import com.nutriagricola.app.data.local.entity.FoliarPhotoEntity
import com.nutriagricola.app.data.remote.dto.*
import com.nutriagricola.app.domain.model.record.*

fun Record.toCreateRequest(photos: List<FoliarPhotoEntity>): RecordCreateDto {
    val soil = (data as? SoilData)?.let {
        SoilDataDto(it.ph, it.electricalConductivity, it.nitrogen, it.phosphorus,
            it.potassium, it.calcium, it.magnesium, it.organicMatter)
    }
    val water = (data as? WaterData)?.let {
        WaterDataDto(it.ph, it.electricalConductivity, it.nitrogen, it.phosphorus,
            it.potassium, it.calcium, it.magnesium, it.waterSource)
    }
    val foliar = (data as? FoliarData)?.let {
        FoliarDataDto(it.nitrogen, it.phosphorus, it.potassium, it.calcium, it.magnesium, it.symptoms)
    }
    return RecordCreateDto(id, cropId, date.toString(), type.name, observations,
        soil, water, foliar, photos.map { PhotoCreateDto(it.id, it.path) })
}

fun RecordCreateDto.toUpdateRequest() =
    RecordUpdateDto(date, observations, soilData, waterData, foliarData)
