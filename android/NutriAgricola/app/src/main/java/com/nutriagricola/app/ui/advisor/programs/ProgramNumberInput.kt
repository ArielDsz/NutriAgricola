package com.nutriagricola.app.ui.advisor.programs

internal fun parseProgramNumber(text: String): Double? = text.trim().replace(',', '.')
    .toDoubleOrNull()?.takeIf { it.isFinite() && it >= 0 }
