package com.utn.space.venueaapi.service.mappers;

import com.utn.space.venueaapi.model.Reservation;
import com.utn.space.venueaapi.model.ServiceSelected;
import com.utn.space.venueaapi.model.SpaceServiceItem;
import com.utn.space.venueaapi.model.records.ServiceSelectedDTO;
import com.utn.space.venueaapi.model.records.ServiceSelectedWithoutReservationDTO;

public class ServiceSelectedMapper {

    // Con reserva
    public static ServiceSelectedDTO toDto(ServiceSelected serviceSelected){
        return new ServiceSelectedDTO(
          serviceSelected.getPriceAtReservation(),
          serviceSelected.getService().getId(),
          serviceSelected.getReservation().getId()
        );
    }

    public static ServiceSelectedDTO toDto(ServiceSelected serviceSelected, Integer id){
        return new ServiceSelectedDTO(
            id,
            serviceSelected.getPriceAtReservation(),
            serviceSelected.getService().getId(),
            serviceSelected.getReservation().getId()
        );
    }

    public static ServiceSelected toEntity(ServiceSelectedDTO serviceSelectedDTO, SpaceServiceItem spaceServiceItem, Reservation reservation){
        return new ServiceSelected(
          serviceSelectedDTO.priceAtReservation(),
          spaceServiceItem,
          reservation
        );
    }

    public static ServiceSelected toEntity(Integer id, ServiceSelectedDTO serviceSelectedDTO, SpaceServiceItem spaceServiceItem, Reservation reservation){
        return new ServiceSelected(
            id,
            serviceSelectedDTO.priceAtReservation(),
            spaceServiceItem,
            reservation
        );
    }

    // Sin reserva

    public static ServiceSelected toEntity(ServiceSelectedWithoutReservationDTO serviceSelectedDTO, SpaceServiceItem spaceServiceItem, Reservation reservation){
        return new ServiceSelected(
                serviceSelectedDTO.priceAtReservation(),
                spaceServiceItem,
                reservation
        );
    }

    public static ServiceSelected toEntity(Integer id, ServiceSelectedWithoutReservationDTO serviceSelectedDTO, SpaceServiceItem spaceServiceItem, Reservation reservation){
        return new ServiceSelected(
                id,
                serviceSelectedDTO.priceAtReservation(),
                spaceServiceItem,
                reservation
        );
    }
}
