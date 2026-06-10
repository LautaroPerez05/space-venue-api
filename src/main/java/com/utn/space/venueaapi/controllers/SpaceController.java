package com.utn.space.venueaapi.controllers;

import com.utn.space.venueaapi.model.records.SpaceDTO;
import com.utn.space.venueaapi.model.SpaceServiceItem;
import com.utn.space.venueaapi.model.Space;
import com.utn.space.venueaapi.model.records.SpaceFilterDTO;
import com.utn.space.venueaapi.service.SpaceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/spaces")
public class SpaceController {
    @Autowired
    SpaceService spaceService;

    @GetMapping
    public ResponseEntity<List<Space>> listSpaces(){
        return ResponseEntity.ok(spaceService.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Space> findSpaceById(@PathVariable Integer id){
        return ResponseEntity.ok(spaceService.findById(id));
    }

    @DeleteMapping("/{id}")
    public void deleteSpaceById(@PathVariable Integer id){
        spaceService.deleteById(id);
    }

    @PostMapping()
    @PreAuthorize("hasAnyRole('CLIENT', 'ADMIN')")
    public void insertSpace(@RequestBody SpaceDTO spaceDTO){
        spaceService.insertSpace(spaceDTO);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('CLIENT')")
    public void modifySpace(@PathVariable Integer id, @RequestBody SpaceDTO spaceDTO){
        spaceService.modifySpace(id,spaceDTO);
    }

    @GetMapping("/byfields")
    public ResponseEntity<List<Space>> findAllByFields(@RequestBody SpaceFilterDTO spaceFilterDTO){
        return ResponseEntity.ok(spaceService.findAllByFields(spaceFilterDTO));
    }
}
