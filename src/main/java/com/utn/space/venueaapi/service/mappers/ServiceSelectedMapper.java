package com.utn.space.venueaapi.service.mappers;

import com.utn.space.venueaapi.model.Reservation;
import com.utn.space.venueaapi.model.ServiceSelected;
import com.utn.space.venueaapi.model.Space;
import com.utn.space.venueaapi.model.SpaceServiceItem;
import com.utn.space.venueaapi.model.records.ServiceSelectedDTO;
import com.utn.space.venueaapi.model.records.ServiceSelectedWithoutReservationDTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface ServiceSelectedMapper {

    @Mapping(target = "id_service_selected", ignore = true)
    ServiceSelected fromItemToSelect (SpaceServiceItem item);

}
