package com.nutriagricola.registros.validation;

import com.nutriagricola.registros.dto.RecordCreateRequest;
import com.nutriagricola.registros.exception.InvalidRecordException;
import org.springframework.stereotype.Component;

import java.util.HashSet;

@Component
public class RecordValidator {
    public void validate(String type, RecordCreateRequest request) {
        boolean hasPhotos = request.foliarPhotos() != null && !request.foliarPhotos().isEmpty();
        boolean matchingData = switch (type) {
            case "SOIL" -> request.soilData() != null && request.waterData() == null
                    && request.foliarData() == null && !hasPhotos;
            case "WATER" -> request.waterData() != null && request.soilData() == null
                    && request.foliarData() == null && !hasPhotos;
            case "FOLIAR" -> request.foliarData() != null && request.soilData() == null
                    && request.waterData() == null;
            default -> false;
        };
        if (!matchingData) {
            throw new InvalidRecordException("Envía únicamente el bloque de datos que corresponde a SOIL, WATER o FOLIAR");
        }

        switch (type) {
            case "SOIL" -> {
                var data = request.soilData();
                validateMeasurements(false, data.ph(), data.electricalConductivity(), data.nitrogen(),
                        data.phosphorus(), data.potassium(), data.calcium(), data.magnesium(), data.organicMatter());
            }
            case "WATER" -> {
                var data = request.waterData();
                validateMeasurements(hasText(data.waterSource()), data.ph(), data.electricalConductivity(),
                        data.nitrogen(), data.phosphorus(), data.potassium(), data.calcium(), data.magnesium());
            }
            case "FOLIAR" -> {
                var data = request.foliarData();
                validateMeasurements(hasText(data.symptoms()) || hasPhotos, data.nitrogen(), data.phosphorus(),
                        data.potassium(), data.calcium(), data.magnesium());
                if (hasPhotos) {
                    var ids = new HashSet<String>();
                    for (var photo : request.foliarPhotos()) {
                        if (!ids.add(photo.id())) {
                            throw new InvalidRecordException("No repitas identificadores de fotografías");
                        }
                    }
                }
            }
        }
    }

    private void validateMeasurements(boolean hasAdditionalData, Double... values) {
        boolean hasMeasurement = false;
        for (Double value : values) {
            if (value != null) {
                if (!Double.isFinite(value)) {
                    throw new InvalidRecordException("Las mediciones deben ser números finitos");
                }
                hasMeasurement = true;
            }
        }
        if (!hasMeasurement && !hasAdditionalData) {
            throw new InvalidRecordException("Ingresa al menos un dato del análisis antes de guardar");
        }
    }

    private boolean hasText(String value) {
        return value != null && !value.isBlank();
    }
}
