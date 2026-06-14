package com.utn.space.venueaapi.controllers;

import com.utn.space.venueaapi.model.Consumer;
import com.utn.space.venueaapi.model.Credential;
import com.utn.space.venueaapi.model.ERoles;
import com.utn.space.venueaapi.security.JwtUtil;
import com.utn.space.venueaapi.service.ConsumerService;
import com.utn.space.venueaapi.service.CredentialService;
import com.utn.space.venueaapi.service.TokenBlacklistService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@AllArgsConstructor
@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;
    private final PasswordEncoder passwordEncoder;
    private final CredentialService credentialService;
    private final ConsumerService consumerService;

    @Autowired
    private TokenBlacklistService blacklistService;

    @PostMapping("/logout")
    public ResponseEntity<String> logout(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            String jwt = bearerToken.substring(7);

            // Extraer el tiempo de expiración restante del JWT para optimizar la memoria
            long remainingTime = blacklistService.getRemainingExpirationTime(jwt);

            // Guardar en la lista negra
            blacklistService.blacklistToken(jwt, remainingTime);

            return ResponseEntity.ok("Sesión cerrada exitosamente y token invalidado.");
        }
        return ResponseEntity.badRequest().body("Token no provisto o inválido.");
    }

    @PostMapping("/login")
    public ResponseEntity<String> login(@RequestBody java.util.Map<String, String> loginRequest) {
        try {
            String username = loginRequest.get("username");
            String password = loginRequest.get("password");

            Authentication auth = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(username, password)
            );

            // 2. Extraemos el rol REAL que Spring Security cargó desde tu base de datos
            String rol = auth.getAuthorities().stream()
                    .map(org.springframework.security.core.GrantedAuthority::getAuthority)
                    .findFirst()
                    .orElse("ROLE_CLIENT"); // Rol por defecto si el usuario no tuviera ninguno

            String token = jwtUtil.generarToken(username,rol);
            return ResponseEntity.ok("Bearer " + token);
        } catch (BadCredentialsException e) {
            return ResponseEntity.status(401).body("Credenciales inválidas");
        }
    }

    // Cambio el @RequestBody para que reciba record RegistroDTO
    @PostMapping("/register")
    public ResponseEntity<String> register(@RequestBody com.utn.space.venueaapi.model.records.RegistroDTO dto) {

        // 1. Validar que no vengan datos vacíos esenciales (usando los métodos del record)
        if (dto.username() == null || dto.username().trim().isEmpty() ||
                dto.password() == null || dto.password().trim().isEmpty()) {
            return ResponseEntity.badRequest().body("Faltan los datos de usuario o contraseña");
        }

        // 2. Validar si el usuario ya existe
        if (credentialService.existsByUsername(dto.username())) {
            return ResponseEntity.badRequest().body("El nombre de usuario ya está en uso");
        }

        // 3. Creamos la Credencial limpia accediendo con dto.username() y dto.password()
        Credential credential = new Credential();
        credential.setUsername(dto.username());
        credential.setPassword(passwordEncoder.encode(dto.password())); // Encriptamos
        credential.setIsActive(true);
        credential.setRol(ERoles.ROLE_CLIENT);

        // 4. Creamos el Consumer con el resto de los componentes del record
        Consumer consumer = new Consumer();
        consumer.setFirstname(dto.firstname());
        consumer.setLastname(dto.lastname());
        consumer.setEmail(dto.email());
        consumer.setPhone(dto.phone());

        // 5. Vinculamos la credencial al consumidor
        consumer.setCredentials(credential);

        // 6. Guardamos en cascada a través de tu servicio
        consumerService.saveConsumer(consumer);

        return ResponseEntity.status(201).body("Usuario y perfil registrados con éxito");
    }
}
