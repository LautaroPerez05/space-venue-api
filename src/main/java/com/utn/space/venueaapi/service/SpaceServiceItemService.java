package com.utn.space.venueaapi.service;

import com.utn.space.venueaapi.exceptions.InvalidDataException;
import com.utn.space.venueaapi.exceptions.NotFoundException;
import com.utn.space.venueaapi.model.Space;
import com.utn.space.venueaapi.model.SpaceServiceItem;
import com.utn.space.venueaapi.model.records.SpaceServiceItemDTO;
import com.utn.space.venueaapi.repository.SpaceRepository;
import com.utn.space.venueaapi.repository.SpaceServiceItemRepository;
import com.utn.space.venueaapi.service.mappers.SpaceServiceItemMapper;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service
public class SpaceServiceItemService {
    private final SpaceServiceItemRepository repository;
    private final SpaceRepository spaceRepository;

    public SpaceServiceItemService(SpaceServiceItemRepository repository, SpaceRepository spaceRepository) {
        this.repository = repository;
        this.spaceRepository = spaceRepository;
    }

    public List<SpaceServiceItemDTO> listOfServicesFromSpace(Integer id){
        return repository.findAllSpaceServicesBySpaceId(id);
    }

    @Transactional
    public void insertServiceItem(SpaceServiceItemDTO serviceItemDTO){
        if(serviceItemDTO.price().compareTo(BigDecimal.ZERO) <= 0) throw new InvalidDataException("No se permiten numeros negativos en el precio de un servicio");

        Space space = spaceRepository.findById(serviceItemDTO.idSpace()).orElseThrow(() -> new NotFoundException("No se ha encontrado el espacio al que se le quiere asociar un servicio"));

        SpaceServiceItem serviceItem = SpaceServiceItemMapper.toEntity(serviceItemDTO, space);

        repository.save(serviceItem);
    }

    @Transactional
    public void updateServiceItem(Integer id, SpaceServiceItemDTO serviceItemDTO){
        if(!repository.existsServiceItemInSpace(id, serviceItemDTO.idSpace())){
            throw new NotFoundException("No se ha encontrado el servicio a modificar en el espacio");
        }

        if(serviceItemDTO.price().compareTo(BigDecimal.ZERO) <= 0) throw new InvalidDataException("No se permiten numeros negativos en modificacion del precio de un servicio");



        SpaceServiceItem serviceItem = SpaceServiceItemMapper.toEntity(
                serviceItemDTO,
                id,
                spaceRepository.findById(serviceItemDTO.idSpace())
                        .orElseThrow(() -> new NotFoundException("No se ha encontrado el espaacio del servicio a modificar")));

        repository.save(serviceItem);
    }

    @Transactional
    public void deleteServiceItem(Integer id, Integer idSpace){
        if(!repository.existsServiceItemInSpace(id, idSpace)) throw new NotFoundException("No se ha encontrado el servicio a eiminar");
        repository.deleteById(id);
    }
}
