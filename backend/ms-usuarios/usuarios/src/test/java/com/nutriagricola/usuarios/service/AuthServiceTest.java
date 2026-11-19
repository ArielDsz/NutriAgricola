package com.nutriagricola.usuarios.service;

import com.nutriagricola.usuarios.dto.AuthResponse;
import com.nutriagricola.usuarios.dto.LoginRequest;
import com.nutriagricola.usuarios.mapper.UserMapper;
import com.nutriagricola.usuarios.model.User;
import com.nutriagricola.usuarios.model.UserRole;
import com.nutriagricola.usuarios.repository.UserRepository;
import com.nutriagricola.usuarios.security.JwtUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtUtil jwtUtil;

    private AuthService authService;
    private User user;

    @BeforeEach
    void setUp() {
        authService = new AuthService(
                userRepository, new UserMapper(), passwordEncoder, jwtUtil
        );

        user = new User();
        user.setId(UUID.fromString("11111111-1111-4111-8111-111111111111"));
        user.setEmail("farmer@test.cl");
        user.setRole(UserRole.FARMER);
        user.setActive(true);
        user.setPasswordHash("hash-de-prueba");
    }

    @Test
    void loginDevuelveTokenYPerfilConCorreoNormalizado() {
        when(userRepository.findByEmailIgnoreCase("farmer@test.cl"))
                .thenReturn(Optional.of(user));
        when(passwordEncoder.matches("farmer12", "hash-de-prueba"))
                .thenReturn(true);
        when(jwtUtil.generateToken(user)).thenReturn("token-de-prueba");

        AuthResponse response = authService.login(
                new LoginRequest("  FARMER@Test.CL  ", "farmer12")
        );

        assertEquals("token-de-prueba", response.getAccessToken());
        assertEquals(user.getId(), response.getUser().getId());
        assertEquals(UserRole.FARMER, response.getUser().getRole());
    }

    @Test
    void loginRechazaUsuarioInexistente() {
        when(userRepository.findByEmailIgnoreCase("missing@test.cl"))
                .thenReturn(Optional.empty());

        assertThrows(BadCredentialsException.class, () ->
                authService.login(new LoginRequest("missing@test.cl", "farmer12"))
        );

        verifyNoInteractions(passwordEncoder, jwtUtil);
    }

    @Test
    void loginRechazaContrasenaIncorrecta() {
        when(userRepository.findByEmailIgnoreCase("farmer@test.cl"))
                .thenReturn(Optional.of(user));
        when(passwordEncoder.matches("incorrecta", "hash-de-prueba"))
                .thenReturn(false);

        assertThrows(BadCredentialsException.class, () ->
                authService.login(new LoginRequest("farmer@test.cl", "incorrecta"))
        );

        verifyNoInteractions(jwtUtil);
    }

    @Test
    void loginRechazaCuentaInactivaAunqueLaContrasenaSeaCorrecta() {
        user.setActive(false);
        when(userRepository.findByEmailIgnoreCase("farmer@test.cl"))
                .thenReturn(Optional.of(user));
        when(passwordEncoder.matches("farmer12", "hash-de-prueba"))
                .thenReturn(true);

        assertThrows(BadCredentialsException.class, () ->
                authService.login(new LoginRequest("farmer@test.cl", "farmer12"))
        );

        verifyNoInteractions(jwtUtil);
    }
}
