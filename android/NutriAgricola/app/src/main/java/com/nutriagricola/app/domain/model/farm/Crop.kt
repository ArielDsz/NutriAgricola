package com.nutriagricola.app.domain.model.farm

import java.util.UUID

data class Crop(
    val id: String = UUID.randomUUID().toString(),
    val farmId: String,
    val name: String,
    val variety: String? = null,
    val areaHectares: Double? = null,
    val active: Boolean = true,
    val imagePath: String? = null,
    val phenologicalStage: String? = null
)