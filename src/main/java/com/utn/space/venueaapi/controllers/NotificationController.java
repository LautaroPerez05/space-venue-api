package com.utn.space.venueaapi.controllers;

import com.utn.space.venueaapi.model.Notification;
import com.utn.space.venueaapi.service.NotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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
    public ResponseEntity<List<Notification>>findAllByIdConsumer(@PathVariable Integer id){
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(notificationService.listAllByIdConsumer(id));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Notification> findForId (@PathVariable Integer id){
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
