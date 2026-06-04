package com.utn.space.venueaapi.controllers;

import com.utn.space.venueaapi.model.records.SpaceDTO;
import com.utn.space.venueaapi.model.SpaceServiceItem;
import com.utn.space.venueaapi.model.Space;
import com.utn.space.venueaapi.model.records.SpaceFilterDTO;
import com.utn.space.venueaapi.service.SpaceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/s&v/spaces")
public class SpaceController {
    @Autowired
    SpaceService spaceService;

    @GetMapping
    public ResponseEntity<List<Space>> listSpaces(){
        return ResponseEntity.ok(spaceService.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Space> findSpaceById(@PathVariable Long id){
        return ResponseEntity.ok(spaceService.findById(id));
    }

    @DeleteMapping("/{id}")
    public void deleteSpaceById(@PathVariable Long id){
        spaceService.deleteById(id);
    }

    @PostMapping()
    public void insertSpace(@RequestBody SpaceDTO spaceDTO){
        spaceService.insertSpace(spaceDTO);
    }

    @PutMapping("/{id}")
    public void modifySpace(@PathVariable Long id, @RequestBody SpaceDTO spaceDTO){
        spaceService.modifySpace(id,spaceDTO);
    }

    @GetMapping("/byfields")
    public void findAllByFields(@RequestBody SpaceFilterDTO spaceFilterDTO){
        spaceService.findAllByFields(spaceFilterDTO);
    }
}
