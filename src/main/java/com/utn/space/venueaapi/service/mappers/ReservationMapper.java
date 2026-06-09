package com.utn.space.venueaapi.service.mappers;

import com.utn.space.venueaapi.model.Consumer;
import com.utn.space.venueaapi.model.Reservation;
import com.utn.space.venueaapi.model.Space;
import com.utn.space.venueaapi.model.records.ReservationDTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface ReservationMapper {

    // Corrección de Mappings por errores de compilación (señalaban atributos inexistentes)
    @Mapping(target = "idConsumer", source = "consumer.idConsumer")
    @Mapping(target = "idSpace", source = "space.idSpace")
    @Mapping(target = "id_servicesSelec", ignore = true)
    ReservationDTO toDTO(Reservation reservation);

    @Mapping(target = "consumer", ignore = true)
    @Mapping(target = "space", ignore = true)
    @Mapping(target = "services", ignore = true)
    @Mapping(target = "googleEventCode", ignore = true)
    Reservation toEntity(ReservationDTO dto);

    default Consumer mapConsumer(Integer id){
        if(id == null) return null;

        Consumer c = new Consumer();
        c.setId_consumer(id);
        return c;
    }

    default Space mapSpace(Integer id){
        if(id == null) return null;

        Space s = new Space();
        s.setId_space(id);
        return s;
    }
}