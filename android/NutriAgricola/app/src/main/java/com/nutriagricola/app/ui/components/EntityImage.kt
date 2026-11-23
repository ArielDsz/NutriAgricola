package com.nutriagricola.app.ui.components

import com.nutriagricola.app.R

/** Referencias explícitas del backend a imágenes incluidas en la app. */
fun entityImageModel(path: String?): Any? = when (path) {
    "drawable/farm_el_maiten" -> R.drawable.farm_el_maiten
    "drawable/farm_los_aromos" -> R.drawable.farm_los_aromos
    "drawable/farm_santa_elena" -> R.drawable.farm_santa_elena
    "drawable/crop_grape" -> R.drawable.crop_grape
    "drawable/crop_avocado" -> R.drawable.crop_avocado
    "drawable/crop_lemon" -> R.drawable.crop_lemon
    "drawable/crop_mandarin" -> R.drawable.crop_mandarin
    "drawable/crop_orange" -> R.drawable.crop_orange
    "drawable/crop_tomato" -> R.drawable.crop_tomato
    else -> path?.takeIf { it.isNotBlank() }
}
