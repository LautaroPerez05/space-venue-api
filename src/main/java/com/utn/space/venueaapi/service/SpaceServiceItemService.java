package com.utn.space.venueaapi.service;

import com.utn.space.venueaapi.exceptions.InvalidDataException;
import com.utn.space.venueaapi.exceptions.NotFoundException;
import com.utn.space.venueaapi.model.Space;
import com.utn.space.venueaapi.model.SpaceServiceItem;
import com.utn.space.venueaapi.model.records.SpaceServiceItemDTO;
import com.utn.space.venueaapi.repository.SpaceRepository;
import com.utn.space.venueaapi.repository.SpaceServiceItemRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.List;


@Service
public class SpaceServiceItemService {
    private final SpaceServiceItemRepository repository;
    private final SpaceRepository spaceRepository;

    public SpaceServiceItemService(SpaceServiceItemRepository repository, SpaceRepository spaceRepository) {
        this.repository = repository;
        this.spaceRepository = spaceRepository;
    }

    public List<SpaceServiceItemDTO> listOfServicesFromSpace(Long id){
        return repository.findAllSpaceServicesBySpaceId(id);
    }

    @Transactional
    public void insertServiceItem(SpaceServiceItemDTO serviceItemDTO){
        if(serviceItemDTO.price() <= 0) throw new InvalidDataException("No se permiten numeros negativos en el precio de un servicio");

        Space space = spaceRepository.findById(serviceItemDTO.idSpace()).orElseThrow(() -> new NotFoundException("No se ha encontrado el espacio al que se le quiere asociar un servicio"));

        SpaceServiceItem serviceItem = new SpaceServiceItem(
                serviceItemDTO.id(),
                serviceItemDTO.description(),
                serviceItemDTO.price(),
                space
        );

        repository.save(serviceItem);
    }
}
