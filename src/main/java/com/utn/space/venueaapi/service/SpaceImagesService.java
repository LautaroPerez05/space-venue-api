package com.utn.space.venueaapi.service;

import com.utn.space.venueaapi.exceptions.InvalidDataException;
import com.utn.space.venueaapi.exceptions.NotFoundException;
import com.utn.space.venueaapi.model.records.SpaceImageDTO;
import com.utn.space.venueaapi.model.SpaceImage;
import com.utn.space.venueaapi.repository.SpaceRepository;
import com.utn.space.venueaapi.repository.SpaceImageRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SpaceImagesService {
    @Autowired
    SpaceImageRepository spaceImageRepository;
    @Autowired
    SpaceRepository spaceRepository;

    public List<SpaceImage> findAll(){
        return spaceImageRepository.findAll();
    }

    public SpaceImage findById(Long id){
        return spaceImageRepository.findById(id).orElseThrow(()-> new NotFoundException("No se encontro la imagen buscada"));
    }

    public void deleteById(Long id){
        if(!spaceImageRepository.existsById(id)){
            throw new NotFoundException("No se encontro la imagen a eliminar");
        }
        spaceImageRepository.deleteById(id);
    }

    public void insertSpaceImage(SpaceImageDTO spaceImageDTO){
        if(spaceImageDTO.file_name().isBlank()){
            throw new InvalidDataException("Por favor ingrese un nombre para la imagen");
        }

        if(spaceImageDTO.url_image().isBlank()){
            throw new InvalidDataException("Por favor ingrese un URL para la imagen");
        }

        SpaceImage spaceImageToInsert = new SpaceImage(
                null,
                spaceRepository.findById(spaceImageDTO.id_space()).orElseThrow(()-> new NotFoundException("No se encontro el espacio asociado a la imagen")),
                spaceImageDTO.file_name(),
                spaceImageDTO.url_image(),
                spaceImageDTO.date_sent()
        );

        spaceImageRepository.save(spaceImageToInsert);
    }


    public void modifySpaceImage(Long id, SpaceImageDTO spaceImageDTO){
        if(!spaceImageRepository.existsById(id)){
            throw new NotFoundException("No se encontro la imagen a actualizar");
        }

        if(spaceImageDTO.file_name().isBlank()){
            throw new InvalidDataException("Por favor ingrese un nombre para la imagen");
        }

        if(spaceImageDTO.url_image().isBlank()){
            throw new InvalidDataException("Por favor ingrese un URL para la imagen");
        }

        SpaceImage spaceImageToInsert = new SpaceImage(
                id,
                spaceRepository.findById(spaceImageDTO.id_space()).orElseThrow(()-> new NotFoundException("No se encontro el espacio asociado a la imagen")),
                spaceImageDTO.file_name(),
                spaceImageDTO.url_image(),
                spaceImageDTO.date_sent()
        );

        spaceImageRepository.save(spaceImageToInsert);
    }

    public List<SpaceImage> findAllBySpaceId(Long spaceId){
        if(!spaceRepository.existsById(spaceId)){
            throw new NotFoundException("No se encontro el espacio del cual se quieren buscar imagenes");
        }
        return spaceImageRepository.findAllBySpaceIdSpace(spaceId);
    }
}
