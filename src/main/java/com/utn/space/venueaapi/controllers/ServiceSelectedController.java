package com.utn.space.venueaapi.controllers;

import com.utn.space.venueaapi.model.records.ServiceSelectedDTO;
import com.utn.space.venueaapi.model.records.ServiceSelectedWithoutReservationDTO;
import com.utn.space.venueaapi.service.ServiceSelectedService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Set;

@RestController
@RequestMapping("/api/servicesselected")
public class ServiceSelectedController {

    @Autowired
    private ServiceSelectedService service;

    @GetMapping("/reservation/{idReservation}")
    public ResponseEntity<List<ServiceSelectedDTO>> getServicesSelected(@PathVariable Long idReservation){
        return ResponseEntity.ok(service.getServicesSelectedOfReservation(idReservation));
    }

    @PostMapping("/insert")
    public void selectOneServiceForAReservation(@RequestBody ServiceSelectedDTO serviceSelectedDTO){
        service.insertServiceSelectedForAReservation(serviceSelectedDTO);
    }

    @PostMapping("/insert/list/{idReservation}")
    public void selectListOfServicesSelectedOnReservation(@PathVariable Long idReservation, @RequestBody List<ServiceSelectedWithoutReservationDTO> servicesSelectedDTO){
        service.insertListOfServicesSelectedInAReservation(idReservation, servicesSelectedDTO);
    }

    @DeleteMapping("/delete/{id}")
    public void deselectOneServiceForAReservation(@PathVariable Long id){
        service.deleteServiceSelectedForAReservation(id);
    }
}
