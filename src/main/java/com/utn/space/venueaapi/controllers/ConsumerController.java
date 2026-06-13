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
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@AllArgsConstructor
@RestController
@RequestMapping("/api") // CORREGIDO: Se remueve la barra final para evitar URLs del tipo /api//usuarios
public class ConsumerController {

    @Autowired
    private final ConsumerService consumerService;

    @Autowired
    private final CredentialService credentialService;

    @PostMapping("/usuarios")
    @Transactional // Sigue siendo crítico para que todo ocurra en un solo bloque seguro
    public ResponseEntity<String> createUser(@RequestBody Credential credential) {

        // 1. Validar duplicados
        if (credentialService.existsByUsername(credential.getUsername())) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("{\"error\": \"El nombre de usuario ya se encuentra registrado.\"}");
        }

        // 2. Configurar la credencial de forma explícita
        credential.setRol(ERoles.ROLE_CLIENT);
        credential.setIsActive(Boolean.TRUE);

        // 3. Guardamos la credencial en su repositorio
        credentialService.saveCredential(credential);

        // 4. CREACIÓN ESPEJO MANUAL: Forzamos la asignación del ID en el objeto Consumer
        Consumer nuevoConsumer = new Consumer();

        // IMPORTANTE: Al ser una relación donde compartes el ID (o se mapea por String),
        // vinculamos la credencial que ya tiene el ID asignado y en limpio
        nuevoConsumer.setCredentials(credential);

        // Inicializamos las cadenas vacías obligatorias de tu DTO/Modelo
        nuevoConsumer.setFirstname("");
        nuevoConsumer.setLastname("");
        nuevoConsumer.setEmail("");
        nuevoConsumer.setPhone("");

        // 5. Guardamos y flusheamos en la tabla 'consumers'
        consumerService.saveConsumer(nuevoConsumer);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body("{\"mensaje\": \"Usuario creado exitosamente\"}");
    }

    @GetMapping("/usuarios")
    @PreAuthorize("hasRole('ADMIN')") // CORREGIDO: URL limpia. Suficiente y seguro.
    public ResponseEntity<List<Credential>> listAllUsers() {
        return ResponseEntity.ok(credentialService.findAll());
    }

    @GetMapping("/usuarios/{id}")
    @PreAuthorize("hasRole('CLIENT') or hasRole('ADMIN')") // CORREGIDO: Sintaxis 'hasRole' estricta
    public ResponseEntity<Consumer> listById(@PathVariable Integer id) {
        return ResponseEntity.ok(consumerService.findById(id));
    }

    @PutMapping("/usuarios/{id}/status") // CORREGIDO: Estandarizado a /api/usuarios/...
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<String> toggleUserStatus(@PathVariable Integer id, @RequestParam Boolean active) {
        // Tu lógica pendiente de service
        return ResponseEntity.ok("Estado del usuario actualizado correctamente");
    }

    @GetMapping("/usuarios/byfields")
    @PreAuthorize("hasRole('ADMIN')") // CORREGIDO: de 'hasroles' a 'hasRole'
    public ResponseEntity<List<Consumer>> findAllByFields(@RequestBody ConsumerFilterDTO consumerFilterDTO){
        return ResponseEntity.ok(consumerService.findAllByfields(consumerFilterDTO));
    }

    @DeleteMapping("/usuarios/{id}")
    @PreAuthorize("hasRole('ADMIN')") // CORREGIDO: de 'hasroles' a 'hasRole'
    public ResponseEntity<String> deleteById(@PathVariable Integer id){
        consumerService.deleteById(id);
        return ResponseEntity.ok("Usuario eliminado con exito");
    }

    @PutMapping("/usuario")
    @PreAuthorize("isAuthenticated()") // Asegura que solo usuarios con sesión activa editen su perfil
    public ResponseEntity<String> updateUser(@RequestBody ConsumerFilterDTO updateData, Principal principal) {
        String username = principal.getName();
        Consumer consumerExistente = consumerService.findByUsername(username);

        if (updateData.firstname() != null) consumerExistente.setFirstname(updateData.firstname());
        if (updateData.lastname() != null) consumerExistente.setLastname(updateData.lastname());

        if (updateData.email() != null && !updateData.email().equals(consumerExistente.getEmail())) {
            if (consumerService.existByEmail(updateData.email())) {
                throw new RuntimeException("El correo electrónico ya se encuentra registrado por otro usuario.");
            }
            consumerExistente.setEmail(updateData.email());
        }

        if (updateData.phone() != null && !updateData.phone().equals(consumerExistente.getPhone())) {
            if (consumerService.existsByPhone(updateData.phone())) {
                throw new RuntimeException("El telefono ya se encuentra registrado por otro usuario.");
            }
            consumerExistente.setPhone(updateData.phone());
        }

        consumerService.updateUser(consumerExistente);
        return ResponseEntity.ok("Se ha actualizado correctamente tu perfil");
    }

    @DeleteMapping("/usuario")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<String> deleteUser(Principal principal) {
        String username = principal.getName();
        consumerService.deleteUserLogically(username);
        return ResponseEntity.ok("Tu cuenta ha sido desactivada correctamente.");
    }
}