package com.utn.space.venueaapi.service;

import com.utn.space.venueaapi.exceptions.NotFoundException;
import com.utn.space.venueaapi.model.Location;
import com.utn.space.venueaapi.repository.LocationRepository;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
@AllArgsConstructor
@Service
public class LocationService {
    @Autowired
    LocationRepository locationRepository;

    public Location findById(Integer id){
        return locationRepository.findById(id).orElseThrow(()-> new NotFoundException("No se encontro la ubicacion"));
    }

    public boolean existsById(Integer id){
        return locationRepository.existsById(id);
    }
}
