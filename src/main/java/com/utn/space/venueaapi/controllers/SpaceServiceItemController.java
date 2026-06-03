package com.utn.space.venueaapi.controllers;

import com.utn.space.venueaapi.model.records.SpaceServiceItemDTO;
import com.utn.space.venueaapi.service.SpaceServiceItemService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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

    @PostMapping("/insert")
    public void insertServiceItem(@RequestBody SpaceServiceItemDTO serviceItemDTO){
        service.insertServiceItem(serviceItemDTO);
    }

    @PutMapping("/update/{id}")
    public void updateServiceItem(@PathVariable Long id, @RequestBody SpaceServiceItemDTO serviceItemDTO){
        service.updateServiceItem(id, serviceItemDTO);
    }

    @DeleteMapping("/delete/{id}")
    public void deleteServiceFromSpace(@PathVariable Long id){
        service.deleteServiceItem(id);
    }
}
