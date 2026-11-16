package com.nutriagricola.app.domain.model.farm

import java.util.UUID

data class Farm(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val location: String? = null,
    val active: Boolean = true,
    val imagePath: String? = null
)