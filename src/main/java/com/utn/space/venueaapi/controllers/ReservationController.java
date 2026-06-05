package com.utn.space.venueaapi.controllers;

import com.utn.space.venueaapi.model.Reservation;
import com.utn.space.venueaapi.model.flags.Create;
import com.utn.space.venueaapi.model.flags.Update;
import com.utn.space.venueaapi.model.records.ReservationDTO;
import com.utn.space.venueaapi.service.ReservationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/s&v/Reservaciones")
public class ReservationController {
    @Autowired
    private ReservationService reservationService;

    /// ---------------------Metodos-----------------------------------------------------

    @GetMapping
    public List<Reservation> findAll (){
        return reservationService.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Reservation> findById (@PathVariable Long id){
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(reservationService.findById(id));
    }


    @PostMapping
    public ResponseEntity<Reservation> createReservation(@Validated(Create.class) @RequestBody ReservationDTO dto){
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
    public ResponseEntity<Reservation> confirmReservation(@PathVariable Long id){
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(reservationService.confirmReservation(id));
    }

    @PutMapping("/complete/{id}")
    public ResponseEntity<Reservation> completeReservation(@PathVariable Long id){
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(reservationService.completeReservation(id));
    }

    @PutMapping("/cancel/{id}")
    public ResponseEntity<Reservation> cancelReservation(@PathVariable Long id){
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(reservationService.cancelReservation(id));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity <Reservation> softDelete (@PathVariable Long id){
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(reservationService.softDelete(id));
    }
}
