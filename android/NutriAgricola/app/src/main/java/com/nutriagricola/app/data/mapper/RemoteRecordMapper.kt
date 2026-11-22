package com.nutriagricola.app.data.mapper

import com.nutriagricola.app.data.remote.dto.RecordDetailDto
import com.nutriagricola.app.domain.model.record.*
import java.time.LocalDate

fun RecordDetailDto.toDomain(): Record {
    val recordType = RecordType.valueOf(type)
    val data = when (recordType) {
        RecordType.SOIL -> requireNotNull(soilData).let {
            SoilData(it.ph, it.electricalConductivity, it.nitrogen, it.phosphorus,
                it.potassium, it.calcium, it.magnesium, it.organicMatter)
        }
        RecordType.WATER -> requireNotNull(waterData).let {
            WaterData(it.ph, it.electricalConductivity, it.nitrogen, it.phosphorus,
                it.potassium, it.calcium, it.magnesium, it.waterSource)
        }
        RecordType.FOLIAR -> requireNotNull(foliarData).let {
            FoliarData(it.nitrogen, it.phosphorus, it.potassium, it.calcium, it.magnesium, it.symptoms)
        }
    }
    // Server photo paths belong to another device; they are not local image files.
    return Record(id, cropId, userId, LocalDate.parse(date), recordType,
        RecordStatus.valueOf(status), observations, data)
}
