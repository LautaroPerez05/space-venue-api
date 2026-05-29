package com.utn.space.venueaapi.SegurityConfig;

import org.springframework.context.annotation.*;
import org.springframework.http.*;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.*;
import org.springframework.security.config.http.*;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.*;

@Configuration
public class SegurityConfig {
    // NOTA CLAVE: El método userDetailsService(PasswordEncoder encoder) que estaba aquí SE ELIMINÓ.
    // Spring usará automáticamente el CustomUserDetailsService que busca en la base de datos.

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http
                // Desactiva la protección CSRF (necesario para probar POST en Postman sin tokens) [cite: 124]
                .csrf(csrf -> csrf.disable())

                // Definición de las reglas de autorización de arriba hacia abajo [cite: 125]
                .authorizeHttpRequests(auth -> auth
                        // 1. La ruta "/publico" permite el acceso a cualquiera sin loguearse [cite: 126]
                        .requestMatchers("/public").permitAll()
                        // 2. Solo los usuarios cuyo registro en la BD tenga el rol "ADMIN" pueden hacer GET a "/api/admin" [cite: 127]
                        .requestMatchers(HttpMethod.GET, "/api/admin").hasRole("ADMIN")

                        // 3. Cualquier otra ruta no especificada arriba exige que el usuario esté autenticado [cite: 128]
                        .anyRequest().authenticated()
                )

                // Activa la autenticación básica HTTP. Postman leerá las credenciales desde la pestaña Authorization [cite: 129]
                .httpBasic(Customizer.withDefaults())

                // Indicamos que la API no guardará sesiones en el servidor (Cada petición debe enviar las credenciales) [cite: 130]
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                .build(); // Construye y retorna la cadena de filtros configurada [cite: 132]
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        // Mantenemos BCrypt. Cuando el usuario intente loguearse, Spring Security usará este bean
        // para encriptar la clave recibida de Postman y compararla con el hash guardado en la BD
        return new BCryptPasswordEncoder();
    }
}
