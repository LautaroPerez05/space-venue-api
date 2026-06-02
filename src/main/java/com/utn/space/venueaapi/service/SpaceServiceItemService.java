package com.utn.space.venueaapi.service;

import com.utn.space.venueaapi.model.records.SpaceServiceItemDTO;
import com.utn.space.venueaapi.repository.SpaceServiceItemRepository;
import org.springframework.stereotype.Service;

import java.util.List;


@Service
public class SpaceServiceItemService {
    private final SpaceServiceItemRepository repository;

    public SpaceServiceItemService(SpaceServiceItemRepository repository) {
        this.repository = repository;
    }

    public List<SpaceServiceItemDTO> listOfServicesFromSpace(Long id){
        return repository.findAllSpaceServicesBySpaceId(id);
    }
}
