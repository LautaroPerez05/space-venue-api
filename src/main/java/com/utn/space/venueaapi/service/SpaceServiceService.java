package com.utn.space.venueaapi.service;

import com.utn.space.venueaapi.model.records.SpaceServiceDTO;
import com.utn.space.venueaapi.repository.SpaceServiceRepository;
import org.springframework.stereotype.Service;

import java.util.List;


@Service
public class SpaceServiceService {
    private final SpaceServiceRepository repository;

    public SpaceServiceService(SpaceServiceRepository repository) {
        this.repository = repository;
    }

    public List<SpaceServiceDTO> listOfServicesFromSpace(Long id){
        return repository.findAllSpaceServicesBySpaceId(id);
    }
}
