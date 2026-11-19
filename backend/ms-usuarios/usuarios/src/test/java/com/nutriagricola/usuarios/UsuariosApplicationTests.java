package com.nutriagricola.usuarios;

import com.jayway.jsonpath.JsonPath;
import com.nutriagricola.usuarios.model.User;
import com.nutriagricola.usuarios.repository.UserRepository;
import com.nutriagricola.usuarios.security.JwtUtil;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.util.Base64;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class UsuariosApplicationTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JwtUtil jwtUtil;

    @Value("${jwt.secret}")
    private String testSecret;

    @Test
    void loginPermiteConsultarElPerfilConElTokenRecibido() throws Exception {
        String response = mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email":"farmer@test.cl","password":"farmer12"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").isNotEmpty())
                .andExpect(jsonPath("$.user.role").value("FARMER"))
                .andExpect(jsonPath("$.user.passwordHash").doesNotExist())
                .andReturn().getResponse().getContentAsString();

        String token = JsonPath.read(response, "$.accessToken");
        User user = userRepository.findByEmailIgnoreCase("farmer@test.cl").orElseThrow();
        assertEquals(user.getId().toString(), jwtUtil.validateAndGetClaims(token).getSubject());

        mockMvc.perform(get("/api/v1/users/me")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(user.getId().toString()))
                .andExpect(jsonPath("$.email").value("farmer@test.cl"))
                .andExpect(jsonPath("$.passwordHash").doesNotExist());
    }

    @Test
    void loginDevuelveErroresPorCampoCuandoEstanVacios() throws Exception {
        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email":"","password":""}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.email").exists())
                .andExpect(jsonPath("$.errors.password").exists());
    }

    @Test
    void perfilRechazaPeticionSinToken() throws Exception {
        mockMvc.perform(get("/api/v1/users/me"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void perfilRechazaTokenFirmadoConOtraClave() throws Exception {
        User user = userRepository.findByEmailIgnoreCase("farmer@test.cl").orElseThrow();
        String differentSecret = Base64.getEncoder().encodeToString(new byte[32]);
        String token = new JwtUtil(differentSecret, 60).generateToken(user);

        mockMvc.perform(get("/api/v1/users/me")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void perfilRechazaTokenVencido() throws Exception {
        User user = userRepository.findByEmailIgnoreCase("farmer@test.cl").orElseThrow();
        String expiredToken = new JwtUtil(testSecret, -1).generateToken(user);

        mockMvc.perform(get("/api/v1/users/me")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + expiredToken))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void cuentaDesactivadaRechazaLoginYTokenEmitidoAntes() throws Exception {
        User user = userRepository.findByEmailIgnoreCase("farmer@test.cl").orElseThrow();
        String token = jwtUtil.generateToken(user);
        user.setActive(false);
        userRepository.saveAndFlush(user);

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email":"farmer@test.cl","password":"farmer12"}
                                """))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Correo o contraseña incorrectos"));

        mockMvc.perform(get("/api/v1/users/me")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
                .andExpect(status().isUnauthorized());
    }
}
