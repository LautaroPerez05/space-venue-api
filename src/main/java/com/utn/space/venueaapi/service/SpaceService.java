package com.utn.space.venueaapi.service;

import com.utn.space.venueaapi.exceptions.InvalidDataException;
import com.utn.space.venueaapi.exceptions.NotFoundException;
import com.utn.space.venueaapi.model.records.SpaceDTO;
import com.utn.space.venueaapi.model.Space;
import com.utn.space.venueaapi.repository.CancellationPolicyRepository;
import com.utn.space.venueaapi.repository.ConsumerRepository;
import com.utn.space.venueaapi.repository.LocationRepository;
import com.utn.space.venueaapi.repository.SpaceRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

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

    public Space findById(Long id){
        return spaceRepository.findById(id).orElseThrow(()-> new NotFoundException("No se encontro el espacio buscado"));
    }

    public void deleteById(Long id){
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

        if(spaceDTO.base_price() <= 0){
            throw new InvalidDataException("Por favor ingrese un precio valido");
        }

        Space spaceToInsert = new Space(
                null,
                consumerRepository.findById(spaceDTO.id_consumer_owner()).orElseThrow(() -> new NotFoundException("No se encontro el consumidor duenio del servicio")),
                locationRepository.findById(spaceDTO.id_location()).orElseThrow(()-> new NotFoundException("No se encontro la ubicacion asociada al espacio")),
                cancellationPolicyRepository.findById(spaceDTO.id_cancellation_policies()).orElseThrow(()->new NotFoundException("No se encontraron las politicas de cancelacion asociadas al servicio")),
                spaceDTO.name_space(),
                spaceDTO.description(),
                spaceDTO.base_price(),
                spaceDTO.publication_date(),
                spaceDTO.buffer_time());

        spaceRepository.save(spaceToInsert);
    }


    public void modifySpace(Long id, SpaceDTO spaceDTO){
        if(!spaceRepository.existsById(id)){
            throw new NotFoundException("No se encontro el espacio a actualizar");
        }

        if(spaceDTO.name_space().isBlank()){
            throw new InvalidDataException("Por favor ingrese un nombre para el espacio");
        }

        if(spaceDTO.description().isBlank()){
            throw new InvalidDataException("Por favor ingrese una descripcion para el espacio");
        }

        if(spaceDTO.base_price() <= 0){
            throw new InvalidDataException("Por favor ingrese un precio valido");
        }

        Space spaceToInsert = new Space(
                id,
                consumerRepository.findById(spaceDTO.id_consumer_owner()).orElseThrow(() -> new NotFoundException("No se encontro el consumidor duenio del servicio")),
                locationRepository.findById(spaceDTO.id_location()).orElseThrow(()-> new NotFoundException("No se encontro la ubicacion asociada al espacio")),
                cancellationPolicyRepository.findById(spaceDTO.id_cancellation_policies()).orElseThrow(()->new NotFoundException("No se encontraron las politicas de cancelacion asociadas al servicio")),
                spaceDTO.name_space(),
                spaceDTO.description(),
                spaceDTO.base_price(),
                spaceDTO.publication_date(),
                spaceDTO.buffer_time());

        spaceRepository.save(spaceToInsert);
    }
}