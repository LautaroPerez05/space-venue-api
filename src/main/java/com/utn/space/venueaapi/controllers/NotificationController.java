package com.utn.space.venueaapi.controllers;

import com.utn.space.venueaapi.model.Notification;
import com.utn.space.venueaapi.service.NotificationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Objects;

@Tag(name = "Notificaciones", description = "Operaciones sobre Notificaciones.")

@RestController
@RequestMapping("/api/notifications")
public class NotificationController {
    @Autowired
    private NotificationService notificationService;

///------------------------------------------------Metodos--------------------------------------------------------------------
    @GetMapping
    @Operation(
            summary = "Busca todas las notificaciones.",
            description = "Devuelve la lista de TODAS las Notificaciones."
    )
    public List<Notification> listAll (){
        return notificationService.listAll();
    }

    @GetMapping("/consumer/{id}")
    @Operation(
            summary = "Busca todas las notificaciones de un Usuario.",
            description = "Devuelve la lista de las Notificaciones un Usuario."
    )
    @PreAuthorize("hasAnyRole('CLIENT', 'ADMIN')")
    public ResponseEntity<List<Notification>>findAllByIdConsumer(@Parameter(description = "ID del Usuario") @PathVariable Integer id, Authentication authentication){
        if (authentication.getAuthorities().stream().anyMatch(r -> Objects.equals(r.getAuthority(), "ROLE_ADMIN"))){
            //Logica si es un Admin
            return ResponseEntity.ok(notificationService.listAllByIdConsumer(id));
        }

        //El cliente solo puede ver sus propias notificaciones
        return ResponseEntity.ok(notificationService.listAllByIdConsumerForConsumer());
    }

    @GetMapping("/consumer/onlyunseen")
    @Operation(
            summary = "Busca todas las notificaciones, no vistas, de un Usuario.",
            description = "Devuelve la lista de las Notificaciones con el Atributo (isSeen = false) un Usuario."
    )
    @PreAuthorize("hasRole('CLIENT')")
    public ResponseEntity<List<Notification>> findAllUnseenByIdconsumer(@Parameter(description = "ID del Usuario") @PathVariable Integer id){
        return ResponseEntity.ok(notificationService.listAllUnseenForConsumer());
    }

    @GetMapping("/{id}")
    @Operation(
            summary = "Busca una notificación en particular.",
            description = "Busca una notificación por su ID."
    )
    public ResponseEntity<Notification> findById(@Parameter(description = "ID de la notificación") @PathVariable Integer id){
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(notificationService.findById(id));
    }


    @PostMapping("/{id}")

    @Operation(
            summary = "Marca como vista a una notificación",
            description = "Busca una notificación por su ID y la marca como vista."
    )
    public ResponseEntity<Notification> markAsSeen (@Parameter(description = "ID del notificación") @PathVariable Integer id){
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(notificationService.markAsSeen(id));
    }
}
