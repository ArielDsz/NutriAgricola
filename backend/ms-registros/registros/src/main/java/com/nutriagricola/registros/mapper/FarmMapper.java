package com.nutriagricola.registros.mapper;

import com.nutriagricola.registros.dto.FarmResponse;
import com.nutriagricola.registros.dto.FarmCreateRequest;
import com.nutriagricola.registros.dto.FarmUpdateRequest;
import com.nutriagricola.registros.model.Farm;
import org.springframework.stereotype.Component;

@Component
public class FarmMapper {
    public Farm toEntity(FarmCreateRequest request) {
        Farm farm = new Farm();
        farm.setId(request.id());
        farm.setName(request.name().strip());
        farm.setLocation(request.location().strip());
        farm.setImagePath(request.imagePath());
        return farm;
    }

    public void updateEntity(FarmUpdateRequest request, Farm farm) {
        farm.setName(request.name().strip());
        farm.setLocation(request.location().strip());
        farm.setImagePath(request.imagePath());
    }

    public FarmResponse toResponse(Farm farm) {
        return new FarmResponse(farm.getId(), farm.getName(), farm.getLocation(),
                farm.getImagePath(), farm.getActive());
    }
}
