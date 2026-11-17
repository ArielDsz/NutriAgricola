package com.nutriagricola.usuarios.dto;

import jakarta.validation.constraints.NotBlank;

public class LoginRequest {
    @NotBlank(message = "email es obligatorio")
    private String email;
    @NotBlank(message = "contraseña es obligatoria")
    private String password;
}
