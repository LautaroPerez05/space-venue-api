package com.utn.space.venueaapi.service;

import com.utn.space.venueaapi.exceptions.InvalidDataException;
import com.utn.space.venueaapi.exceptions.NotFoundException;
import com.utn.space.venueaapi.model.records.SpaceDTO;
import com.utn.space.venueaapi.model.Space;
import com.utn.space.venueaapi.model.records.SpaceFilterDTO;
import com.utn.space.venueaapi.repository.CancellationPolicyRepository;
import com.utn.space.venueaapi.repository.ConsumerRepository;
import com.utn.space.venueaapi.repository.LocationRepository;
import com.utn.space.venueaapi.repository.SpaceRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service
public class SpaceService {
    @Autowired
    SpaceRepository spaceRepository;
    @Autowired
    ConsumerRepository consumerRepository;
    @Autowired
    LocationRepository locationRepository;
    @Autowired
    CancellationPolicyRepository cancellationPolicyRepository;


    public List<Space> findAll(){
        return spaceRepository.findAll();
    }

    public Space findById(Integer id){
        return spaceRepository.findById(id).orElseThrow(()-> new NotFoundException("No se encontro el espacio buscado"));
    }

    public void deleteById(Integer id){
        if(!spaceRepository.existsById(id)){
            throw new NotFoundException("No se encontro el espacio a eliminar");
        }
        spaceRepository.deleteById(id);
    }

    public void insertSpace(SpaceDTO spaceDTO){
        if(spaceDTO.name_space().isBlank()){
            throw new InvalidDataException("Por favor ingrese un nombre para su espacio");
        }

        if(spaceDTO.description().isBlank()){
            throw new InvalidDataException("Por favor ingrese una descripcion para su espacio");
        }

        if(spaceDTO.base_price().compareTo(BigDecimal.ZERO) <= 0){
            throw new InvalidDataException("Por favor ingrese un precio valido");
        }

        Space spaceToInsert = new Space(
                null,
                consumerRepository.findById(spaceDTO.id_consumer_owner()).orElseThrow(() -> new NotFoundException("No se encontro el consumidor duenio del servicio")),
                locationRepository.findById(spaceDTO.id_location()).orElseThrow(()-> new NotFoundException("No se encontro la ubicacion asociada al espacio")),
                cancellationPolicyRepository.findById(spaceDTO.id_cancellation_policies()).orElseThrow(()->new NotFoundException("No se encontraron las politicas de cancelacion asociadas al servicio")),
                spaceDTO.google_calendar_id(),
                spaceDTO.name_space(),
                spaceDTO.description(),
                spaceDTO.base_price(),
                spaceDTO.publication_date(),
                spaceDTO.buffer_time(),
                spaceDTO.isActive());

        spaceRepository.save(spaceToInsert);
    }


    public void modifySpace(Integer id, SpaceDTO spaceDTO){
        if(!spaceRepository.existsById(id)){
            throw new NotFoundException("No se encontro el espacio a actualizar");
        }

        if(spaceDTO.name_space().isBlank()){
            throw new InvalidDataException("Por favor ingrese un nombre para el espacio");
        }

        if(spaceDTO.description().isBlank()){
            throw new InvalidDataException("Por favor ingrese una descripcion para el espacio");
        }

        if(spaceDTO.base_price().compareTo(BigDecimal.ZERO) <= 0){
            throw new InvalidDataException("Por favor ingrese un precio valido");
        }

        Space spaceToInsert = new Space(
                id,
                consumerRepository.findById(spaceDTO.id_consumer_owner()).orElseThrow(() -> new NotFoundException("No se encontro el consumidor duenio del servicio")),
                locationRepository.findById(spaceDTO.id_location()).orElseThrow(()-> new NotFoundException("No se encontro la ubicacion asociada al espacio")),
                cancellationPolicyRepository.findById(spaceDTO.id_cancellation_policies()).orElseThrow(()->new NotFoundException("No se encontraron las politicas de cancelacion asociadas al servicio")),
                spaceDTO.google_calendar_id(),
                spaceDTO.name_space(),
                spaceDTO.description(),
                spaceDTO.base_price(),
                spaceDTO.publication_date(),
                spaceDTO.buffer_time(),
                spaceDTO.isActive());

        spaceRepository.save(spaceToInsert);
    }

    /*
    public List<Space> findAllByConsumerOwner(Long id){
        if (!consumerRepository.existsById(id)){
            throw new NotFoundException("No se encontro el owner del cual se quieren ver los espacios");
        }
        return spaceRepository.findAllByConsumerOwner_IdConsumer(id);
    }

    public List<Space> findAllByLocation(Long id){
        if(!consumerRepository.existsById(id)){
            throw new NotFoundException("No se encontro la ubicacion de la cual se quieren ver los espacios");
        }
        return spaceRepository.findAllByLocation_IdLocation(id);
    }

    public List<Space> findAllByNameSpace(String nameSpace){
        return spaceRepository.findAllByNameSpace(nameSpace);
    }

    public List<Space> findAllByBasePrice(Double minPrice, Double maxPrice){
        return spaceRepository.findAllByBasePriceBetween(minPrice,maxPrice);
    }
    */

    public List<Space> findAllByFields(SpaceFilterDTO spaceFilterDTO){
        if((spaceFilterDTO.id_consumer_owner() != null) && !consumerRepository.existsById(spaceFilterDTO.id_consumer_owner())){
            throw new NotFoundException("No se encontro el owner del cual se quieren ver los espacios");
        }

        if((spaceFilterDTO.id_location() != null) && !locationRepository.existsById(spaceFilterDTO.id_location())){
            throw new NotFoundException("No se encontro la ubicacion de la cual se quieren ver los espacios");
        }

        return spaceRepository.findAllByFields(spaceFilterDTO.id_consumer_owner(), spaceFilterDTO.minPrice(),spaceFilterDTO.maxPrice(),spaceFilterDTO.name_space(), spaceFilterDTO.id_location());
    }
}