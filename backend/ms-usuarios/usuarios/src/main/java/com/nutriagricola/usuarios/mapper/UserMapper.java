package com.nutriagricola.usuarios.mapper;

import com.nutriagricola.usuarios.dto.UserResponse;
import com.nutriagricola.usuarios.model.User;
import org.springframework.stereotype.Component;

@Component
public class UserMapper {

    public UserResponse toResponse(User user) {
        return new UserResponse(
                user.getId(),
                user.getRun(),
                user.getFirstName(),
                user.getLastName(),
                user.getEmail(),
                user.getRole(),
                user.isActive(),
                user.getBirthDate(),
                user.getCreatedAt(),
                user.getPhoneNumber(),
                user.getRegion(),
                user.getComuna()
        );
    }
}
