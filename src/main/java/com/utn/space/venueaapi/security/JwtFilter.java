package com.utn.space.venueaapi.security;

import com.utn.space.venueaapi.service.TokenBlacklistService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;
import java.io.IOException;
import java.util.ArrayList;

// OncePerRequestFilter garantiza que este filtro se ejecute exactamente una vez por cada petición HTTP
public class JwtFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private final TokenBlacklistService blacklistService;

    // Constructor que permite inyectar el utilitario de manejo de tokens
    public JwtFilter(JwtUtil jwtUtil, TokenBlacklistService blacklistService) {
        this.jwtUtil = jwtUtil;
        this.blacklistService = blacklistService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        // Lee el contenido de la cabecera HTTP estándar llamada "Authorization"
        String cabeceraAuth = request.getHeader("Authorization");
        String username = null;
        String jwt = null;

        //Para evitar errores con JWT
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            response.setStatus(HttpServletResponse.SC_OK);
            filterChain.doFilter(request, response);
            return;
        }

        // Comprueba que la cabecera exista y que cumpla con el prefijo internacional de tokens "Bearer "
        if (cabeceraAuth != null && cabeceraAuth.startsWith("Bearer ")) {
            jwt = cabeceraAuth.substring(7); // Corta la cadena para aislar el token puro (remueve "Bearer ")
            try {
                username = jwtUtil.extraerUsername(jwt); // Intenta descifrar el username del token
            } catch (Exception e) {
                logger.error("Error al procesar el token JWT: " + e.getMessage());
            }
        }


        // Nueva versión corregida para manejar los roles y atrapar fallas de validación
        if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            boolean esValido = jwtUtil.validarToken(jwt, username);
            boolean estaEnBlacklist = blacklistService.isTokenBlacklisted(jwt);

            if (esValido && !estaEnBlacklist) {
                // 1. Extraemos el rol real que guardamos en el token
                String rol = jwtUtil.extraerRol(jwt);

                // 2. Convertimos ese String en una autoridad que Spring Security entienda
                var authority = new org.springframework.security.core.authority.SimpleGrantedAuthority(rol);
                java.util.List<org.springframework.security.core.authority.SimpleGrantedAuthority> authorities = java.util.List.of(authority);

                // 3. Creamos el token de autenticación formal
                UsernamePasswordAuthenticationToken authToken =
                        new UsernamePasswordAuthenticationToken(username, null, authorities);

                authToken.setDetails(new org.springframework.security.web.authentication.WebAuthenticationDetailsSource().buildDetails(request));

                // Setea formalmente la autenticación en el contexto global
                SecurityContextHolder.getContext().setAuthentication(authToken);
                logger.info("🟢 JWT Válido. Usuario autenticado localmente: '" + username + "' con rol: " + rol);

            } else {
                // Si el token llegó pero no pasó las validaciones, imprimimos el porqué exacto en tu consola de IntelliJ/VS Code
                if (estaEnBlacklist) {
                    logger.warn("Intento de acceso denegado: El token de '" + username + "' está en la Blacklist (Salió por Logout).");
                    response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Token invalidado por Logout.");
                    return; // Corta la petición acá, no va al controlador
                }
                if (!esValido) {
                    logger.error("¡FALLÓ LA VALIDACIÓN DEL JWT! El jwtUtil determinó que el token para '" + username + "' NO es válido en este entorno local.");
                    response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Token JWT inválido o expirado en entorno local.");
                    return; // Corta la petición acá, no va al controlador
                }
            }
        }

        // Cede el control de la petición para que continúe si todo salio bien
        filterChain.doFilter(request, response);
    }
}

