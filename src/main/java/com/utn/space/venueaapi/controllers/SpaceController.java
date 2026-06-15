package com.utn.space.venueaapi.controllers;

import com.utn.space.venueaapi.model.flags.Create;
import com.utn.space.venueaapi.model.records.SpaceDTO;
import com.utn.space.venueaapi.model.Space;
import com.utn.space.venueaapi.model.records.SpaceFilterDTO;
import com.utn.space.venueaapi.service.SpaceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/spaces")
public class SpaceController {
    @Autowired
    SpaceService spaceService;

    //Este listado incluye espacios inactivos
    @GetMapping("/showinactives")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<Space>> listSpaces(){
        return ResponseEntity.ok(spaceService.findAll());
    }

    //Este listado no incluye espacios inactivos
    @GetMapping()
    public ResponseEntity<List<Space>> listActiveSpaces(){
        return ResponseEntity.ok(spaceService.findAllActives());
    }

    //Muestro todos los espacios de un owner, incluyendo inactivos
    @GetMapping("/ownedspaces")
    @PreAuthorize("hasAnyRole('CLIENT', 'ADMIN')")
    public ResponseEntity<List<Space>> findAllForOwner(){
        return ResponseEntity.ok(spaceService.findAllForOwner());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Space> findSpaceById(@PathVariable Integer id){
        return ResponseEntity.ok(spaceService.findById(id));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public void deleteAnySpaceById(@PathVariable Integer id){
        spaceService.deleteById(id);
    }

    @PostMapping()
    @PreAuthorize("hasRole('ADMIN')")
    public void insertSpace(@RequestBody SpaceDTO spaceDTO){
        spaceService.insertSpace(spaceDTO);
    }

    //Lo que hace @Validated es no permitir que se mande un DTO incompleto
    @PostMapping("/ownedspace")
    @PreAuthorize("hasAnyRole('CLIENT', 'ADMIN')")
    public void insertOwnedSpace(@Validated(Create.class)@RequestBody SpaceDTO spaceDTO){
        spaceService.insertOwnedSpace(spaceDTO);
    }

    @PutMapping("/ownedspace/{id}")
    @PreAuthorize("hasAnyRole('CLIENT', 'ADMIN')")
    public void modifyOwnedSpace(@PathVariable Integer id, @RequestBody SpaceDTO spaceDTO){
        spaceService.modifyOwnedSpace(id,spaceDTO);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public void modifySpace(@PathVariable Integer id, @RequestBody SpaceDTO spaceDTO){
        spaceService.modifySpace(id,spaceDTO);
    }

    //Se modifico de GET a POST porque en los navegadores los GET no permiten mandar un Body
    @PostMapping("/byfields")
    public ResponseEntity<List<Space>> findAllByFields(@RequestBody SpaceFilterDTO spaceFilterDTO){
        return ResponseEntity.ok(spaceService.findAllByFields(spaceFilterDTO));
    }

    //Este metodo si va a mostrar los espacios no disponibles
    @GetMapping("/byfields/showinactives")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<Space>> findAllByFieldsWithInactives(@RequestBody SpaceFilterDTO spaceFilterDTO){
        return ResponseEntity.ok(spaceService.findAllByFieldsWithInactives(spaceFilterDTO));
    }

    @GetMapping("/ownedspaces/byfields")
    @PreAuthorize("hasAnyRole('CLIENT', 'ADMIN')")
    public ResponseEntity<List<Space>> findAllOwnedSpacesbyFields(@RequestBody SpaceFilterDTO spaceFilterDTO){
        return ResponseEntity.ok(spaceService.findAllByFieldsForOwner(spaceFilterDTO));
    }

    @DeleteMapping("/ownedspace/{id}")
    @PreAuthorize("hasAnyRole('CLIENT', 'ADMIN')")
    public ResponseEntity<String> deleteOwnedSpace(@PathVariable Integer id){
        spaceService.deleteOwnedSpace(id);
        return ResponseEntity.ok("Espacio eliminado con exito");
    }

}
