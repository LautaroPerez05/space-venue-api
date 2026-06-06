package com.utn.space.venueaapi.controllers;

import com.utn.space.venueaapi.model.records.SpaceDTO;
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

    //Con este metodo listamos los servicios de un espacio para que un consumer pueda seleccionar los que guste
    @GetMapping("/space/{idSpace}")
    public ResponseEntity<List<SpaceServiceItemDTO>> listServicesFromSpace(@PathVariable Integer idSpace){
        return ResponseEntity.ok(service.listOfServicesFromSpace(idSpace));
    }

    //Este es el metodo que un Owner usaria para insertar servicios en uno de sus Espacios
    @PostMapping("/insert")
    public void insertServiceItem(@RequestBody SpaceServiceItemDTO serviceItemDTO){
        service.insertServiceItem(serviceItemDTO);
    }

    @PutMapping("/update/{id}")
    public void updateServiceItem(@PathVariable Integer id, @RequestBody SpaceServiceItemDTO serviceItemDTO){
        service.updateServiceItem(id, serviceItemDTO);
    }

    @DeleteMapping("/delete/{id}")
    public void deleteServiceFromSpace(@PathVariable Integer id, @RequestBody SpaceDTO spaceDTO){
        service.deleteServiceItem(id, spaceDTO.id_space());
    }
}
