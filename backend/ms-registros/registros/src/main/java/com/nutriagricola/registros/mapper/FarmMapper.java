package com.nutriagricola.registros.mapper;

import com.nutriagricola.registros.dto.FarmResponse;
import com.nutriagricola.registros.model.Farm;
import org.springframework.stereotype.Component;

@Component
public class FarmMapper {
    public FarmResponse toResponse(Farm farm) {
        return new FarmResponse(farm.getId(), farm.getName(), farm.getLocation(),
                farm.getImagePath(), farm.getActive());
    }
}
