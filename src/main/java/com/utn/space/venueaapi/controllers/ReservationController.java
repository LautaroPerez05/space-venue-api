package com.utn.space.venueaapi.controllers;

import com.mercadopago.exceptions.MPApiException;
import com.mercadopago.exceptions.MPException;
import com.utn.space.venueaapi.model.Reservation;
import com.utn.space.venueaapi.model.flags.Create;
import com.utn.space.venueaapi.model.flags.Update;
import com.utn.space.venueaapi.model.records.ReservationDTO;
import com.utn.space.venueaapi.service.GoogleCalendarService;
import com.utn.space.venueaapi.service.IPaymentService;
import com.utn.space.venueaapi.service.ReservationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/reservations")
public class ReservationController {
    @Autowired
    private ReservationService reservationService;
    @Autowired
    private final IPaymentService paymentService;

    private final GoogleCalendarService googleCalendarService;

    public ReservationController(IPaymentService paymentService, GoogleCalendarService googleCalendarService) {
        this.paymentService = paymentService;
        this.googleCalendarService = googleCalendarService;
    }


    @PostMapping("/{id}/checkout")
    @PreAuthorize("hasRole('CLIENT') and @securityUtils.isReservationOwner(#id, authentication.name)")
    public ResponseEntity<Map<String, String>> createPaymentPreference(@PathVariable Integer id) {
        try {
            Reservation reservation = reservationService.findById(id);

            // Validar que la reserva esté en un estado apto para ser pagada
            if (!"TENTATIVE".equals(reservation.getStatus().toString())) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(Collections.singletonMap("error", "Solo se pueden pagar reservas en estado TENTATIVE."));
            }

            // Invocar al servicio de Mercado Pago para generar el link
            String initPointUrl = paymentService.createPreference(reservation);

            // Retornar la URL envuelta en un JSON estructurado para el Frontend
            return ResponseEntity.ok(Collections.singletonMap("initPoint", initPointUrl));

        } catch (MPException | MPApiException e) {
            // Errores específicos del SDK de Mercado Pago (credenciales inválidas, problemas de API, etc.)
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Collections.singletonMap("error", "Error al comunicarse con la pasarela de pagos: " + e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Collections.singletonMap("error", "Error interno del sistema: " + e.getMessage()));
        }
    }

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
                .body(reservationService.create(dto));
    }

    @PutMapping
    public ResponseEntity<Reservation> modifyReservation(@Validated(Update.class)@RequestBody ReservationDTO dto){
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(reservationService.modify(dto));
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
        return ResponseEntity.ok(reservationService.cancelReservation(id));
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
