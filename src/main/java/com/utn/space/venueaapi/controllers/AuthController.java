package com.utn.space.venueaapi.controllers;

import com.utn.space.venueaapi.model.Credential;
import com.utn.space.venueaapi.security.JwtUtil;
import com.utn.space.venueaapi.service.CredentialService;
import lombok.AllArgsConstructor;
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

    @PostMapping("/login")
    public ResponseEntity<String> login(@RequestBody Credential credential) {
        try {
            Authentication auth = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            credential.getUsername(),
                            credential.getPasswordHash()
                    )
            );
            String token = jwtUtil.generarToken(credential.getUsername());
            return ResponseEntity.ok("Bearer " + token);
        } catch (BadCredentialsException e) {
            return ResponseEntity.status(401).body("Credenciales inválidas");
        }
    }

    @PostMapping("/register")
    public ResponseEntity<String> register(@RequestBody Credential credential) {
        // 1. Validar si el usuario ya existe para evitar duplicados
        if (credentialService.existsByUsername(credential.getUsername())) {
            return ResponseEntity.badRequest().body("El nombre de usuario ya está en uso");
        }

        // 2. Crear la nueva credencial e introducir la contraseña ENCRIPTADA
        Credential newCredential = new Credential();
        newCredential.setUsername(credential.getUsername());

        // Encriptamos usando BCrypt antes de guardar en la BD
        String encodedPassword = passwordEncoder.encode(credential.getPasswordHash());
        newCredential.setPasswordHash(encodedPassword);

        // 3. Guardar en la base de datos
        credentialService.saveCredential(newCredential);

        return ResponseEntity.status(201).body("Usuario registrado con éxito");
    }
}
