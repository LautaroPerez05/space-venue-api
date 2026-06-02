package com.utn.space.venueaapi.controllers;

import com.utn.space.venueaapi.model.records.SpaceServiceItemDTO;
import com.utn.space.venueaapi.service.SpaceServiceItemService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/services")
public class SpaceServiceItemController {

    @Autowired
    SpaceServiceItemService service;

    @GetMapping("/space/{idSpace}")
    public ResponseEntity<List<SpaceServiceItemDTO>> listServicesFromSpace(@PathVariable Long idSpace){
        return ResponseEntity.ok(service.listOfServicesFromSpace(idSpace));
    }


}
