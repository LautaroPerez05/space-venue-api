package com.utn.space.venueaapi.controllers;

import com.utn.space.venueaapi.model.Credential;
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
    private final UserDetailsManager userDetailsManager;
    private final PasswordEncoder passwordEncoder;

    @PostMapping("/usuarios")
    public ResponseEntity<String> createUser(@RequestBody Credential credential){
        UserDetails user = User
                .withUsername(credential.getUsername())
                .password(passwordEncoder.encode(credential.getPasswordHash()))
                .roles("CLIENT")
                .build();
        userDetailsManager.createUser(user);
        return ResponseEntity.status(HttpStatus.CREATED).body("Usuario creado exitosamente");
    }
}