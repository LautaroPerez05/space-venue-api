package com.utn.space.venueaapi.config;

import com.utn.space.venueaapi.security.JwtFilter;
import com.utn.space.venueaapi.security.JwtUtil;
import org.springframework.context.annotation.*;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.JdbcUserDetailsManager;
import org.springframework.security.provisioning.UserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import javax.sql.DataSource;

@Configuration
public class SecurityConfig {

    private final JwtUtil jwtUtil;

    public SecurityConfig(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable()) // Desactiva la protección CSRF ya que al usar JWT la API es inherentemente inmune
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)) // Configura la API sin estado, no guarda sesiones en el servidor
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api/**").permitAll() // Permite el paso libre sin token al login y registro de usuarios
                        .anyRequest().authenticated() // Exige token JWT obligatorio para ingresar a cualquier otra ruta del sistema
                );

        // Agrega el filtro interceptor de JWT justo antes del filtro nativo de autenticación de Spring por usuario/contraseña
        http.addFilterBefore(new JwtFilter(jwtUtil), UsernamePasswordAuthenticationFilter.class);

        return http.build(); // Retorna la cadena de configuración construida
    }

   @Bean
   UserDetailsManager users(DataSource dataSource){
        return new JdbcUserDetailsManager(dataSource);
   }

    @Bean
    PasswordEncoder passwordEncoder(){
        return PasswordEncoderFactories.createDelegatingPasswordEncoder();
    }

}
