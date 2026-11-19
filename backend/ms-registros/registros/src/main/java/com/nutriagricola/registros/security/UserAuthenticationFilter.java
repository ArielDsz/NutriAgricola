package com.nutriagricola.registros.security;

import com.nutriagricola.registros.client.UserClientResponse;
import com.nutriagricola.registros.client.UsersClient;
import feign.FeignException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

public class UserAuthenticationFilter extends OncePerRequestFilter {
    private final UsersClient usersClient;

    public UserAuthenticationFilter(UsersClient usersClient) {
        this.usersClient = usersClient;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String authorization = request.getHeader("Authorization");
        if (authorization == null || !authorization.startsWith("Bearer ")
                || authorization.substring(7).isBlank()) {
            response.sendError(401, "Debes iniciar sesión");
            return;
        }

        UserClientResponse user;
        try {
            user = usersClient.getCurrentUser(authorization);
        } catch (FeignException ex) {
            if (ex.status() == 401 || ex.status() == 403) {
                response.sendError(401, "Token inválido o vencido");
            } else {
                response.sendError(503, "El servicio de usuarios no está disponible");
            }
            return;
        }

        if (user == null || !Boolean.TRUE.equals(user.active()) || user.id() == null
                || !("FARMER".equals(user.role()) || "ADVISOR".equals(user.role()))) {
            response.sendError(401, "Usuario no autorizado");
            return;
        }
        try {
            UUID.fromString(user.id());
        } catch (IllegalArgumentException ex) {
            response.sendError(401, "Usuario no autorizado");
            return;
        }

        var authentication = new UsernamePasswordAuthenticationToken(user, null,
                List.of(new SimpleGrantedAuthority("ROLE_" + user.role())));
        var context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(authentication);
        SecurityContextHolder.setContext(context);
        filterChain.doFilter(request, response);
    }
}
