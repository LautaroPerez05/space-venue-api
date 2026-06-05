package com.utn.space.venueaapi.security;

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

    // Constructor que permite inyectar el utilitario de manejo de tokens
    public JwtFilter(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        // Lee el contenido de la cabecera HTTP estándar llamada "Authorization"
        String cabeceraAuth = request.getHeader("Authorization");
        String username = null;
        String jwt = null;

        // Comprueba que la cabecera exista y que cumpla con el prefijo internacional de tokens "Bearer "
        if (cabeceraAuth != null && cabeceraAuth.startsWith("Bearer ")) {
            jwt = cabeceraAuth.substring(7); // Corta la cadena para aislar el token puro (remueve "Bearer ")
            try {
                username = jwtUtil.extraerUsername(jwt); // Intenta descifrar el username del token
            } catch (Exception e) {
                logger.error("Error al procesar el token JWT: " + e.getMessage());
            }
        }

        // Si se halló un usuario válido pero este aún no cuenta con una sesión activa en el hilo de ejecución actual
        if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            if (jwtUtil.validarToken(jwt, username)) {
                // Crea un objeto de autenticación interna con el usuario y una lista vacía de permisos/roles
                UsernamePasswordAuthenticationToken authToken =
                        new UsernamePasswordAuthenticationToken(username, null, new ArrayList<>());

                // Extrae y asocia detalles del origen de la petición de red (como la IP o sesión remota)
                authToken.setDetails(new org.springframework.security.web.authentication.WebAuthenticationDetailsSource().buildDetails(request));

                // Setea formalmente la autenticación en el contexto global de Spring Security para esta petición
                SecurityContextHolder.getContext().setAuthentication(authToken);
            }
        }

        // Cede el control de la petición para que continúe hacia el siguiente filtro o controlador
        filterChain.doFilter(request, response);
    }
}
