package com.utn.space.venueaapi.controllers;

import com.utn.space.venueaapi.model.Notification;
import com.utn.space.venueaapi.service.NotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/notifications")
public class NotificationController {
    @Autowired
    private NotificationService notificationService;


    @GetMapping
    public List<Notification> listAll (){
        return notificationService.listAll();
    }

    @GetMapping("/consumer/{id}")
    @PreAuthorize("hasAnyRole('CLIENT', 'ADMIN')")
    public ResponseEntity<List<Notification>>findAllByIdConsumer(@PathVariable Integer id, Authentication authentication){
        if (authentication.getAuthorities().stream().anyMatch(r -> r.getAuthority().equals("ROLE_ADMIN"))){
            //Logica si es un Admin
            return ResponseEntity.ok(notificationService.listAllByIdConsumer(id));
        }

        //El cliente solo puede ver sus propias notificaciones
        return ResponseEntity.ok(notificationService.listAllByIdConsumerForConsumer());
    }

    @GetMapping("/consumer/onlyunseen")
    @PreAuthorize("hasRole('CLIENT')")
    public ResponseEntity<List<Notification>> findAllUnseenByIdconsumer(@PathVariable Integer id){
        return ResponseEntity.ok(notificationService.listAllUnseenForConsumer());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Notification> findById(@PathVariable Integer id){
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(notificationService.findById(id));
    }


    @PostMapping("/{id}")
    public ResponseEntity<Notification> markAsSeen (@PathVariable Integer id){
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(notificationService.markAsSeen(id));
    }
}
