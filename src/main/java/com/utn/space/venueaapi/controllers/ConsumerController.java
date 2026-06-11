package com.utn.space.venueaapi.controllers;

import com.utn.space.venueaapi.model.Consumer;
import com.utn.space.venueaapi.model.Credential;
import com.utn.space.venueaapi.model.ERoles;
import com.utn.space.venueaapi.model.records.ConsumerFilterDTO;

import com.utn.space.venueaapi.service.ConsumerService;
import com.utn.space.venueaapi.service.CredentialService;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@AllArgsConstructor
@RestController
@RequestMapping("/api/")
public class ConsumerController {
    @Autowired
    private final ConsumerService consumerService;
    @Autowired
    private final CredentialService credentialService;
    @Autowired
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
        credentialService.saveCredential(credential);

        return ResponseEntity.status(HttpStatus.CREATED).body("Usuario creado exitosamente" + credential.getPassword());
    }

    @GetMapping("/usuarios") //Borre el admin de la URL. Es suficiente el @PreAuthorize no?
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<Credential>> listAllUsers() {
        return ResponseEntity.ok(credentialService.findAll());
    }

    // Lógica sin desarrollar
    @PutMapping("/admin/usuarios/{id}/status")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<String> toggleUserStatus(@PathVariable Integer id, @RequestParam Boolean active) {
        // Lógica para activar/desactivar el usuario mediante service
        return ResponseEntity.ok("Estado del usuario actualizado");
    }

    //Es para para que el admin filtre consumers
    @GetMapping("/usuarios/byfields")
    @PreAuthorize("hasroles('ADMIN')")
    public ResponseEntity<List<Consumer>> findAllByFields(@RequestBody ConsumerFilterDTO consumerFilterDTO){
        return ResponseEntity.ok(consumerService.findAllByfields(consumerFilterDTO));
    }

    @DeleteMapping("/usuarios/{id}")
    @PreAuthorize("hasroles('ADMIN')")
    public ResponseEntity<String> deleteById(@PathVariable Integer id){
        consumerService.deleteById(id);
        return ResponseEntity.ok("Usuario eliminado con exito");
    }
}