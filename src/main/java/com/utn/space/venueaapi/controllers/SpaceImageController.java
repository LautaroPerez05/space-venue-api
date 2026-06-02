package com.utn.space.venueaapi.controllers;

import com.utn.space.venueaapi.model.records.SpaceImageDTO;
import com.utn.space.venueaapi.model.SpaceImage;
import com.utn.space.venueaapi.service.SpaceImagesService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/s&v/spaceimages")
public class SpaceImageController {
    @Autowired
    SpaceImagesService spaceImagesService;

    @GetMapping
    public ResponseEntity<List<SpaceImage>> listSpaceImages(){
        return ResponseEntity.ok(spaceImagesService.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<SpaceImage> findSpaceImageById(@PathVariable Long id){
        return ResponseEntity.ok(spaceImagesService.findById(id));
    }

    @DeleteMapping("/{id}")
    public void deleteSapceImageById(@PathVariable Long id){
        spaceImagesService.deleteById(id);
    }

    @PostMapping()
    public void insertSpace(@RequestBody SpaceImageDTO spaceImageDTO){
        spaceImagesService.insertSpaceImage(spaceImageDTO);
    }

    @PutMapping("/{id}")
    public void modifySpaceImage(@PathVariable Long id, @RequestBody SpaceImageDTO spaceImageDTO){
        spaceImagesService.modifySpaceImage(id,spaceImageDTO);
    }

    @GetMapping("/byspaceid/{id}")
    public ResponseEntity<List<SpaceImage>> findAllBySpaceId(@PathVariable Long id){
        return ResponseEntity.ok(spaceImagesService.findAllBySpaceId(id));
    }
}
