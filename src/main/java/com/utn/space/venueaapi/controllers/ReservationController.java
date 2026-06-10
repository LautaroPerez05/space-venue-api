package com.utn.space.venueaapi.controllers;

import com.utn.space.venueaapi.model.Reservation;
import com.utn.space.venueaapi.model.flags.Create;
import com.utn.space.venueaapi.model.flags.Update;
import com.utn.space.venueaapi.model.records.ReservationDTO;
import com.utn.space.venueaapi.service.GoogleCalendarService;
import com.utn.space.venueaapi.service.ReservationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/reservations")
public class ReservationController {
    @Autowired
    private ReservationService reservationService;

    private final GoogleCalendarService googleCalendarService;

    public ReservationController(GoogleCalendarService googleCalendarService) {
        this.googleCalendarService = googleCalendarService;
    }

    /// ------------------------------METODOS----------------------------------------------------


    @GetMapping
    public ResponseEntity<List<Reservation>> findAll (){
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(reservationService.findAll());
    }

    /* @GetMapping ("/consumer/{id}")
    public ResponseEntity<List<Reservation>> findAllByIdConsumer (@PathVariable Long id){
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(reservationService.findAllByConsumerID(id));
    } <-------------- findAllByConsumerID no está implementado en ReservationService
    */

    @GetMapping("/{id}")
    public ResponseEntity<Reservation> findById (@PathVariable Integer id){
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(reservationService.findById(id));
    }


    @PostMapping
    @PreAuthorize("hasRole('CLIENT')")
    public ResponseEntity<Reservation> createReservation(@Validated(Create.class) @RequestBody ReservationDTO dto) throws IOException {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(reservationService.createReservation(dto));
    }

    @PutMapping
    public ResponseEntity<Reservation> modifyReservation(@Validated(Update.class)@RequestBody ReservationDTO dto){
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(reservationService.modifyReservation(dto));
    }

    @PutMapping("/confirm/{id}")
    @PreAuthorize("hasRole('CLIENT')")
    public ResponseEntity<Reservation> confirmReservation(@PathVariable Integer id){
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(reservationService.confirmReservation(id));
    }

    // Caso de uso: El Dueño rechaza la solicitud de reserva
    @PutMapping("/reject/{id}")
    public ResponseEntity<Reservation> rejectReservation(@PathVariable Integer id){
        return ResponseEntity.ok(reservationService.rejectReservation(id));
    }

    @PutMapping("/complete/{id}")
    public ResponseEntity<Reservation> completeReservation(@PathVariable Integer id){
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(reservationService.completeReservation(id));
    }

    @PutMapping("/cancel/{id}")
    public ResponseEntity<Reservation> cancelReservation(@PathVariable Integer id){
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(reservationService.cancelReservation(id));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity <Reservation> softDelete (@PathVariable Integer id){
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(reservationService.softDelete(id));
    }


}
