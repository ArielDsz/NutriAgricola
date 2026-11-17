package com.nutriagricola.usuarios.service;

import com.nutriagricola.usuarios.dto.AuthResponse;
import com.nutriagricola.usuarios.dto.LoginRequest;
import com.nutriagricola.usuarios.mapper.UserMapper;
import com.nutriagricola.usuarios.model.User;
import com.nutriagricola.usuarios.repository.UserRepository;
import com.nutriagricola.usuarios.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Locale;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    @Transactional(readOnly = true)
    public AuthResponse login(LoginRequest request) {
        String email = request.getEmail()
                .strip()
                .toLowerCase(Locale.ROOT);

        User user = userRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() ->
                        new BadCredentialsException(
                                "Correo o contraseña incorrectos"
                        )
                );

        boolean validPassword = passwordEncoder.matches(
                request.getPassword(),
                user.getPasswordHash()
        );

        if (!validPassword || !user.isActive()) {
            throw new BadCredentialsException(
                    "Correo o contraseña incorrectos"
            );
        }

        String accessToken = jwtUtil.generateToken(user);

        return new AuthResponse(
                accessToken,
                userMapper.toResponse(user)
        );
    }
}