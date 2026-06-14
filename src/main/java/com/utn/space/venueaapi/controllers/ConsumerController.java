package com.utn.space.venueaapi.controllers;

import com.utn.space.venueaapi.model.Consumer;
import com.utn.space.venueaapi.model.Credential;
import com.utn.space.venueaapi.model.ERoles;
import com.utn.space.venueaapi.model.records.ConsumerFilterDTO;

import com.utn.space.venueaapi.model.records.ReservationDTO;
import com.utn.space.venueaapi.service.ConsumerService;
import com.utn.space.venueaapi.service.CredentialService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@AllArgsConstructor
@RestController
@Tag(name = "Usuarios", description = "Operaciones sobre Consumer.")

@RequestMapping("/api/")
public class ConsumerController {
    @Autowired
    private final ConsumerService consumerService;
    @Autowired
    private final CredentialService credentialService;
    @Autowired
    private final PasswordEncoder passwordEncoder;

    @PostMapping("/usuarios")
    @Operation(
            summary = "Crea un Consummer.",
            description = "Crea un nuevo usuario."
    )
    public ResponseEntity<String> createUser(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Entra los datos obligatorios de la creacion de una nueva Reserva",
                    required = true,
                    content = @Content(
                            schema = @Schema(
                                    implementation = Credential.class),
                            examples = @ExampleObject(
                                    name = "Ejemplo",
                                    value = """
                                    {
                                      "username":"Pepe",
                                      "isActive": true,
                                      "passwordHash":"fatiga"}
                                    """)
                    )
            )
            @RequestBody Credential credential) {
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
    @Operation(
            summary = "Busca todos los usuarios.",
            description = "Devuelve una lista de todos los usuario."
    )
    public ResponseEntity<List<Credential>> listAllUsers() {
        return ResponseEntity.ok(credentialService.findAll());
    }

    @GetMapping("/usuarios/{id}")
    //publico porque asi podes entrar al perfil de cualquier usuario como si fuese una red social, pero autorizado asi no cualquiera puede entrar a perfiles
    @PreAuthorize("hasrole('CLIENT')")
    @Operation(
            summary = "Busca un Usuario",
            description = "Busca un usuarios usando su ID."
    )
    public Consumer listById(@PathVariable Integer id) {
        return consumerService.findById(id);
    }

    // Lógica sin desarrollar
    @PutMapping("/admin/usuarios/{id}/status")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
            summary = "Cambia el estado de un Usuario.",
            description = "Cambia el estado Activo de un usuario por ID."
    )
    public ResponseEntity<String> toggleUserStatus(
            @PathVariable Integer id, @RequestParam Boolean active) {
        // Lógica para activar/desactivar el usuario mediante service
        return ResponseEntity.ok("Estado del usuario actualizado");
    }

    //Es para para que el admin filtre consumers
    @GetMapping("/usuarios/byfields")
    @PreAuthorize("hasroles('ADMIN')")
    @Operation(
            summary = "Busca los Usuarios por atributos.",
            description = "Crea una lista de usuario que cumplen con los atributos dados."
    )
    public ResponseEntity<List<Consumer>> findAllByFields(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Entra los datos obligatorios de la creacion de una nueva Reserva",
                    required = true,
                    content = @Content(
                            schema = @Schema(
                                    implementation = Credential.class),
                            examples = @ExampleObject(
                                    name = "Ejemplo",
                                    value = """
                                    {
                                      "username":"Pepe",
                                      "isActive": true,
                                      "passwordHash":"fatiga"}
                                    """)
                    )
            )
            @RequestBody ConsumerFilterDTO consumerFilterDTO){
        return ResponseEntity.ok(consumerService.findAllByfields(consumerFilterDTO));
    }

    @DeleteMapping("/usuarios/{id}")
    @PreAuthorize("hasroles('ADMIN')")
    @Operation(
            summary = "Elimina un usuario por ID."
    )
    public ResponseEntity<String> deleteById(@PathVariable Integer id){
        consumerService.deleteById(id);
        return ResponseEntity.ok("Usuario eliminado con exito");
    }

    @PutMapping("/usuario")
    @Operation(
            summary = "Actualiza un Usuario."
    )
    public ResponseEntity<String> updateUser(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Entra los datos obligatorios de la creacion de una nueva Reserva",
                    required = true,
                    content = @Content(
                            schema = @Schema(
                                    implementation = Credential.class),
                            examples = @ExampleObject(
                                    name = "Ejemplo",
                                    value = """
                                    {
                                      "username":"Pepe",
                                      "isActive": true,
                                      "passwordHash":"fatiga"}
                                    """)
                    )
            )
            @RequestBody ConsumerFilterDTO updateData,
            Principal principal) {
        String username = principal.getName();
        Consumer consumerExistente = consumerService.findByUsername(username);
        if (updateData.firstname() != null) consumerExistente.setFirstname(updateData.firstname());
        if (updateData.lastname() != null) consumerExistente.setLastname(updateData.lastname());

        if (updateData.email() != null && !updateData.email().equals(consumerExistente.getEmail())) {
            if (consumerService.existByEmail(updateData.email())) {
                throw new RuntimeException("El correo electrónico ya se encuentra registrado por otro usuario.");
            }
            consumerExistente.setEmail(updateData.email()); // <- Se asigna solo si pasa la validación
        }
        if (updateData.phone() != null && !updateData.phone().equals(consumerExistente.getPhone())) {
            if (consumerService.existsByPhone(updateData.phone())) {
                throw new RuntimeException("El telefono ya se encuentra registrado por otro usuario.");
            }
            consumerExistente.setPhone(updateData.phone()); // <- Se asigna solo si pasa la validación
        }
        consumerService.updateUser(consumerExistente);
        return ResponseEntity.ok("Se ha actualizado correctamente tu perfil");
    }

    @DeleteMapping("/usuario")
    @Operation(
            summary = "Elimina un Usuario.",
            description = "Hace un SoftDelete de un usuario."
    )
    public ResponseEntity<String> deleteUser(Principal principal) {
        String username = principal.getName();
        // Ejecuta la baja lógica en el servicio
        consumerService.deleteUserLogically(username);
        return ResponseEntity.ok("Tu cuenta ha sido desactivada correctamente.");
    }




}