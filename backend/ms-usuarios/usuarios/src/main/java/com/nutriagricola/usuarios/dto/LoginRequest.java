package com.nutriagricola.usuarios.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class LoginRequest {
    @NotBlank(message = "email es obligatorio")
    private String email;
    @NotBlank(message = "contraseña es obligatoria")
    private String password;
}
