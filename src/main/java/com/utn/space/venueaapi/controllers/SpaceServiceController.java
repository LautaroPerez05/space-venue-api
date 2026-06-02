package com.utn.space.venueaapi.controllers;

import com.utn.space.venueaapi.model.records.SpaceServiceDTO;
import com.utn.space.venueaapi.service.SpaceServiceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/services")
public class SpaceServiceController {

    @Autowired
    SpaceServiceService service;

    @GetMapping("/space/{idSpace}")
    public ResponseEntity<List<SpaceServiceDTO>> listServicesFromSpace(@PathVariable Long idSpace){
        return ResponseEntity.ok(service.listOfServicesFromSpace(idSpace));
    }


}
