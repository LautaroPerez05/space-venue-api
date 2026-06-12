package com.utn.space.venueaapi.config;

import com.utn.space.venueaapi.security.JwtFilter;
import com.utn.space.venueaapi.security.JwtUtil;
import org.springframework.context.annotation.*;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity // CRUCIAL: Esto habilita @PreAuthorize("hasRole('...')") en tus controladores
public class SecurityConfig {

    private final JwtUtil jwtUtil;

    public SecurityConfig(JwtUtil jwtUtil) {this.jwtUtil = jwtUtil;}

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable()) // Desactiva la protección CSRF ya que al usar JWT la API es inherentemente inmune
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)) // Configura la API sin estado, no guarda sesiones en el servidor
                .authorizeHttpRequests(auth -> auth
                        // RUTAS PÚBLICAS (Visitantes sin loguear)
                        .requestMatchers("/api/auth/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/spaces/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/spaceimages/**").permitAll()
                        .requestMatchers("/api/spaces/search").permitAll()

                        // RUTAS EXCLUSIVAS DE ADMINISTRADOR
                        // Todas las rutas que empiecen con /api/admin requieren obligatoriamente ROLE_ADMIN
                        .requestMatchers("/api/admin/**").hasRole("ADMIN")

                        // CUALQUIER OTRA RUTA (Exige estar autenticado como CLIENT o ADMIN)
                        .anyRequest().authenticated()
                );

        // Agrega el filtro interceptor de JWT justo antes del filtro nativo de autenticación de Spring por usuario/contraseña
        http.addFilterBefore(new JwtFilter(jwtUtil), UsernamePasswordAuthenticationFilter.class);

        return http.build(); // Retorna la cadena de configuración construida
    }

    @Bean
    PasswordEncoder passwordEncoder(){
        return PasswordEncoderFactories.createDelegatingPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration) throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }
}
