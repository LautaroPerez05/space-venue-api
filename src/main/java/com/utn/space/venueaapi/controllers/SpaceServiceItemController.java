package com.utn.space.venueaapi.controllers;

import com.utn.space.venueaapi.model.records.SpaceDTO;
import com.utn.space.venueaapi.model.records.SpaceServiceItemDTO;
import com.utn.space.venueaapi.service.SpaceServiceItemService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Objects;

@RestController
@RequestMapping("/api/services")
public class SpaceServiceItemController {

    @Autowired
    private SpaceServiceItemService service;

    //Con este metodo listamos los servicios de un espacio para que un consumer pueda seleccionar los que guste
    @GetMapping("/space/{idSpace}")
    public ResponseEntity<List<SpaceServiceItemDTO>> listServicesFromSpace(@PathVariable Integer idSpace, Authentication authentication){
        if (authentication.getAuthorities().stream().anyMatch(r -> Objects.equals(r.getAuthority(), "ROLE_ADMIN"))){
            //Logica si es un Admin
            return ResponseEntity.ok(service.listOfServicesFromSpace(idSpace));
        }

        //Logica si no es Admin y no es duenio del spacio, oculta algunos datos
        return ResponseEntity.ok(service.ConsumerlistOfServicesFromSpace(idSpace));
    }

    //Este es el metodo que un Owner usaria para insertar servicios en uno de sus Espacios
    @PostMapping("/insert")
    public void insertServiceItem(@RequestBody SpaceServiceItemDTO serviceItemDTO, Authentication authentication){
        if (authentication.getAuthorities().stream().anyMatch(r -> Objects.equals(r.getAuthority(), "ROLE_ADMIN"))){
            //Logica si es un Admin
            service.insertServiceItem(serviceItemDTO);
        }else {
            //Logica si no es Admin
            service.insertServiceItemOwner(serviceItemDTO);
        }
    }

    @PutMapping("/update/{id}")
    public void updateServiceItem(@PathVariable Integer id, @RequestBody SpaceServiceItemDTO serviceItemDTO, Authentication authentication){
        if (authentication.getAuthorities().stream().anyMatch(r -> Objects.equals(r.getAuthority(), "ROLE_ADMIN"))){
            //Logica si es un Admin
            service.updateServiceItem(id, serviceItemDTO);
        }else {
            //Logica si no es Admin
            service.updateServiceItemOwner(id,serviceItemDTO);
        }
    }

    @DeleteMapping("/delete/{id}")
    public void deleteServiceFromSpace(@PathVariable Integer id, @RequestBody SpaceDTO spaceDTO, Authentication authentication){
        if (authentication.getAuthorities().stream().anyMatch(r -> Objects.equals(r.getAuthority(), "ROLE_ADMIN"))){
            //Logica si es un Admin
            service.deleteServiceItem(id, spaceDTO.idSpace());
        }else {
            //Logica si no es Admin
            service.deleteServiceItemOwner(id,spaceDTO.idSpace());
        }
    }
}
