package com.nutriagricola.registros.dto;

// Resultado interno: permite distinguir una inserción de la confirmación de un reenvío.
public record RecordCreationResult(RecordCreateResponse response, boolean created) { }
