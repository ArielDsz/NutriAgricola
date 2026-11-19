package com.nutriagricola.registros.config;

import com.nutriagricola.registros.client.UsersClient;
import com.nutriagricola.registros.security.UserAuthenticationFilter;
import jakarta.servlet.DispatcherType;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
public class SecurityConfig {
    @Bean
    SecurityFilterChain apiSecurity(HttpSecurity http, UsersClient usersClient) throws Exception {
        return http
                .csrf(AbstractHttpConfigurer::disable)
                .formLogin(AbstractHttpConfigurer::disable)
                .httpBasic(AbstractHttpConfigurer::disable)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .exceptionHandling(errors -> errors
                        .authenticationEntryPoint((request, response, ex) ->
                                response.sendError(401, "Debes iniciar sesión"))
                        .accessDeniedHandler((request, response, ex) ->
                                response.sendError(403, "No tienes permiso para realizar esta operación")))
                .authorizeHttpRequests(authorize -> authorize
                        .dispatcherTypeMatchers(DispatcherType.ERROR).permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/v1/farms", "/api/v1/farms/*", "/api/v1/crops/farm/*", "/api/v1/crops/*",
                                "/api/v1/records/status/*").hasAnyRole("FARMER", "ADVISOR")
                        .requestMatchers(HttpMethod.POST, "/api/v1/farms").hasRole("FARMER")
                        .requestMatchers(HttpMethod.PUT, "/api/v1/farms/*").hasRole("FARMER")
                        .requestMatchers(HttpMethod.DELETE, "/api/v1/farms/*").hasRole("FARMER")
                        .requestMatchers(HttpMethod.POST, "/api/v1/crops").hasRole("FARMER")
                        .requestMatchers(HttpMethod.PUT, "/api/v1/crops/*").hasRole("FARMER")
                        .requestMatchers(HttpMethod.DELETE, "/api/v1/crops/*").hasRole("FARMER")
                        .requestMatchers(HttpMethod.POST, "/api/v1/records").hasRole("FARMER")
                        .requestMatchers(HttpMethod.POST, "/api/v1/reviews").hasRole("ADVISOR")
                        .anyRequest().denyAll())
                .addFilterBefore(new UserAuthenticationFilter(usersClient),
                        UsernamePasswordAuthenticationFilter.class)
                .build();
    }
}
