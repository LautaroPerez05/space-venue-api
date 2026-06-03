package com.utn.space.venueaapi.service.mappers;

import com.utn.space.venueaapi.model.Consumer;
import com.utn.space.venueaapi.model.Reservation;
import com.utn.space.venueaapi.model.Space;
import com.utn.space.venueaapi.model.records.ReservationDTO;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface ReservationMapper {

    @org.mapstruct.Mapping(target= "consumer",ignore = true)
    @org.mapstruct.Mapping(target= "space",ignore = true)
    ReservationDTO toDTO(Reservation reservation);

    @org.mapstruct.Mapping(target= "id_consumer",ignore = true)
    @org.mapstruct.Mapping(target= "id_space",ignore = true)
            //(source = "id_space", target = "space")
    Reservation toEntity(ReservationDTO dto);

    default Consumer mapConsumer(Long id){
        if(id == null) return null;

        Consumer c = new Consumer();
        c.setId_consumer(id);
        return c;
    }

    default Space mapSpace(Long id){
        if(id == null) return null;

        Space s = new Space();
        s.setId_space(id);
        return s;
    }
}