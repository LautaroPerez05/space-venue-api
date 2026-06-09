package com.utn.space.venueaapi.controllers;

import com.utn.space.venueaapi.model.Consumer;
import com.utn.space.venueaapi.model.Reservation;
import com.utn.space.venueaapi.model.Space;
import com.utn.space.venueaapi.model.flags.Create;
import com.utn.space.venueaapi.model.flags.Update;
import com.utn.space.venueaapi.model.records.ReservationDTO;
import com.utn.space.venueaapi.service.ConsumerService;
import com.utn.space.venueaapi.service.GoogleCalendarService;
import com.utn.space.venueaapi.service.ReservationService;
import com.utn.space.venueaapi.service.SpaceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/Reservaciones")
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

    @GetMapping ("/consumer/{id}")
    public ResponseEntity<List<Reservation>> findAllByIdConsumer (@PathVariable Long id){
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(reservationService.findAllByConsumerID(id));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Reservation> findById (@PathVariable Integer id){
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(reservationService.findById(id));
    }


    @PostMapping
    public ResponseEntity<Reservation> createReservation(@Validated(Create.class) @RequestBody ReservationDTO dto) throws IOException {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(reservationService.create(dto));
    }

    @PutMapping
    public ResponseEntity<Reservation> modifyReservation(@Validated(Update.class)@RequestBody ReservationDTO dto){
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(reservationService.modify(dto));
    }

    @PutMapping("/confirm/{id}")
    public ResponseEntity<Reservation> confirmReservation(@PathVariable Integer id){
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(reservationService.confirmReservation(id));
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
    public ResponseEntity <Reservation> softDelete (@PathVariable Integer id){
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(reservationService.softDelete(id));
    }


}
