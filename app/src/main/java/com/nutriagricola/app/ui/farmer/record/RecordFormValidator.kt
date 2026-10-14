package com.nutriagricola.app.ui.farmer.record

data class NumericValidationResult(
    val values: Map<String, Double?>,
    val errors: Map<String, String>
)

object RecordFormValidator {
    private val decimalPattern = Regex("[+-]?(?:[0-9]+(?:[.,][0-9]*)?|[.,][0-9]+)")

    fun validateNumbers(fields: Map<String, String>): NumericValidationResult {
        val values = mutableMapOf<String, Double?>()
        val errors = mutableMapOf<String, String>()

        fields.forEach { (field, input) ->
            val text = input.trim()
            if (text.isEmpty()) {
                values[field] = null
            } else {
                val value = if (decimalPattern.matches(text)) {
                    text.replace(',', '.').toDoubleOrNull()?.takeIf { it.isFinite() }
                } else null

                if (value == null) {
                    errors[field] = "Ingresa un número válido"
                } else {
                    values[field] = value
                }
            }
        }

        return NumericValidationResult(values, errors)
    }

    fun optionalText(value: String): String? = value.trim().takeIf { it.isNotEmpty() }

    fun validateMinimumData(measurements: Collection<Double?>, text: String? = null): String? {
        return if (measurements.any { it != null } || !text.isNullOrBlank()) null
        else "Ingresa al menos un dato antes de guardar"
    }
}
