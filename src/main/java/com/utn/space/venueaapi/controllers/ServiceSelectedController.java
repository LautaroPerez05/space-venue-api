package com.utn.space.venueaapi.controllers;

import com.utn.space.venueaapi.model.records.ServiceSelectedDTO;
import com.utn.space.venueaapi.service.ServiceSelectedService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/servicesselected")
public class ServiceSelectedController {

    @Autowired
    private ServiceSelectedService service;

    @GetMapping("/reservation/{idReservation}")
    public ResponseEntity<List<ServiceSelectedDTO>> getServicesSelected(@PathVariable Integer idReservation){
        return ResponseEntity.ok(service.getServicesSelectedOfReservation(idReservation));
    }

    //Este un metodo que inserta servicios sin una reserva?
    /*
    @PostMapping("/insert")
    public void selectOneServiceForAReservation(@RequestBody ServiceSelectedDTO serviceSelectedDTO){
        service.insertServiceForAReservation(serviceSelectedDTO);
    }
    */


    //Este es un metodo que usaría un Consumer para seleccionar los servicios de un espacio que quiere para una reserva
    @PostMapping("/insert/list/{idReservation}")
    public void selectListOfServicesOnReservation(@PathVariable Integer idReservation, @RequestBody List<ServiceSelectedDTO> servicesSelectedDTO){
        service.insertListOfServicesSelectedInAReservation(idReservation, servicesSelectedDTO);
    }

    @DeleteMapping("/delete/{id}")
    public void deselectOneServiceForAReservation(@PathVariable Integer id){
        service.deleteServiceSelectedForAReservation(id);
    }
}
