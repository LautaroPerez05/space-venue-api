package com.utn.space.venueaapi.controllers;

import com.utn.space.venueaapi.exceptions.ExceptionIdNotFound;
import com.utn.space.venueaapi.model.Reservation;
import com.utn.space.venueaapi.service.ReservationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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

    @ExceptionHandler(ExceptionIdNotFound.class)
    public ResponseEntity<String> idNotFound (ExceptionIdNotFound e){
        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(e.getMessage());
    }
    /*
    @PostMapping
    public Reservations createReservation(@RequestBody )
    */
}
