package com.nutriagricola.usuarios.dto;

import com.nutriagricola.usuarios.model.UserRole;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserResponse {
    private UUID id;
    private String run;
    private String firstName;
    private String lastName;
    private String email;
    private UserRole role;
    private boolean active;
    private LocalDate birthDate;
    private LocalDateTime createdAt;
    private String phoneNumber;
    private String region;
    private String comuna;
}
