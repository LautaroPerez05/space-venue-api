package com.utn.space.venueaapi.service;

import com.utn.space.venueaapi.exceptions.ExceptionIdNotFound;
import com.utn.space.venueaapi.exceptions.ExceptionNameNotFound;
import com.utn.space.venueaapi.exceptions.InvalidDataException;
import com.utn.space.venueaapi.model.Space;
import com.utn.space.venueaapi.model.SpaceServiceItem;
import com.utn.space.venueaapi.model.records.SpaceServiceItemDTO;
import com.utn.space.venueaapi.repository.SpaceRepository;
import com.utn.space.venueaapi.repository.SpaceServiceItemRepository;
import com.utn.space.venueaapi.service.mappers.SpaceServiceItemMapper;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
@AllArgsConstructor
@Service
public class SpaceServiceItemService {
    @Autowired
    private final SpaceServiceItemRepository spaceServiceItemRepository;
    @Autowired
    private final SpaceService spaceService;

    public SpaceServiceItem findById(Integer id){
        return spaceServiceItemRepository.findById(id).orElseThrow(() -> new ExceptionIdNotFound("Servicio Catálogo", id));
    }

    public List<SpaceServiceItemDTO> listOfServicesFromSpace(Integer id){
        return spaceServiceItemRepository.findAllSpaceServicesBySpaceId(id);
    }

    @Transactional
    public void insertServiceItem(SpaceServiceItemDTO serviceItemDTO){
        if(serviceItemDTO.price().compareTo(BigDecimal.ZERO) <= 0) throw new InvalidDataException("No se permiten numeros negativos en el precio de un servicio");

        Space space = spaceService.findById(serviceItemDTO.idSpace());

        SpaceServiceItem serviceItem = SpaceServiceItemMapper.toEntity(serviceItemDTO, space);

        spaceServiceItemRepository.save(serviceItem);
    }

    @Transactional
    public void updateServiceItem(Integer id, SpaceServiceItemDTO serviceItemDTO){
        if(!spaceServiceItemRepository.existsServiceItemInSpace(id, serviceItemDTO.idSpace())){
            throw new ExceptionNameNotFound("No se ha encontrado el servicio a modificar en el espacio");
        }

        if(serviceItemDTO.price().compareTo(BigDecimal.ZERO) <= 0) throw new InvalidDataException("No se permiten numeros negativos en modificacion del precio de un servicio");



        SpaceServiceItem serviceItem = SpaceServiceItemMapper.toEntity(
                serviceItemDTO,
                id,
                spaceService.findById(serviceItemDTO.idSpace()));

        spaceServiceItemRepository.save(serviceItem);
    }

    @Transactional
    public void deleteServiceItem(Integer id, Integer idSpace){
        if(!spaceServiceItemRepository.existsServiceItemInSpace(id, idSpace)) throw new ExceptionNameNotFound("No se ha encontrado el servicio a eiminar");
        spaceServiceItemRepository.deleteById(id);
    }
}
