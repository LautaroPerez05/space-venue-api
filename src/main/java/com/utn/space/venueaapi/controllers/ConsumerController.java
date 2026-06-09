package com.utn.space.venueaapi.controllers;

import com.utn.space.venueaapi.model.Credential;
import com.utn.space.venueaapi.model.ERoles;
import com.utn.space.venueaapi.repository.CredentialRepository;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.UserDetailsManager;
import org.springframework.web.bind.annotation.*;

@AllArgsConstructor
@RestController
@RequestMapping("/api/")
public class ConsumerController {

    // Cambiamos el manager por tu repositorio real
    private final CredentialRepository credentialRepository;
    private final PasswordEncoder passwordEncoder;

    @PostMapping("/usuarios")
    public ResponseEntity<String> createUser(@RequestBody Credential credential){
        // 1. Encriptas la contraseña recibida y la asignas de nuevo al objeto
        String passwordEncriptada = passwordEncoder.encode(credential.getPasswordHash());
        credential.setPasswordHash(passwordEncriptada);

        // 2. Te aseguras de que el rol por defecto esté asignado si viene nulo
        if (credential.getRol() == null) {
            credential.setRol(com.utn.space.venueaapi.model.ERoles.ROLE_CLIENT);
        }

        // 3. Guardas directamente en la tabla 'credentials' usando JPA
        credential.setRol(ERoles.ROLE_CLIENT);
        credential.setIsActive(Boolean.TRUE);
        credentialRepository.save(credential);

        return ResponseEntity.status(HttpStatus.CREATED).body("Usuario creado exitosamente" + credential.getPassword());
    }
}