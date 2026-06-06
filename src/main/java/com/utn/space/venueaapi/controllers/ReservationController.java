package com.utn.space.venueaapi.controllers;

import com.utn.space.venueaapi.model.Consumer;
import com.utn.space.venueaapi.model.Reservation;
import com.utn.space.venueaapi.model.Space;
import com.utn.space.venueaapi.model.flags.Create;
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
@RequestMapping("/s&v/Reservaciones")
public class ReservationController {
    @Autowired
    private ReservationService reservationService;

    private final GoogleCalendarService googleCalendarService;

    public ReservationController(GoogleCalendarService googleCalendarService) {
        this.googleCalendarService = googleCalendarService;
    }

    @GetMapping
    public List<Reservation> findAll (){
        return reservationService.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Reservation> findById (@PathVariable Integer id){
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(reservationService.findById(id));
    }

    @PostMapping
    public ResponseEntity<Reservation> createReservation(@Validated(Create.class) @RequestBody ReservationDTO dto) throws IOException {
        // Ejecuta el metodo de servicio adaptado, el cual calcula precios, impacta Google Calendar y persiste en BD local
        Reservation nuevaReserva = reservationService.create(dto);

        return ResponseEntity
                .status(HttpStatus.CREATED) // Cambiado a HTTP 201 CREATED que es el estándar REST correcto para operaciones POST exitosas
                .body(nuevaReserva);
    }


}
