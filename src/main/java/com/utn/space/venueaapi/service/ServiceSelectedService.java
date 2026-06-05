package com.utn.space.venueaapi.service;

import com.utn.space.venueaapi.exceptions.NotFoundException;
import com.utn.space.venueaapi.model.Reservation;
import com.utn.space.venueaapi.model.SpaceServiceItem;
import com.utn.space.venueaapi.model.records.ServiceSelectedDTO;
import com.utn.space.venueaapi.model.records.ServiceSelectedWithoutReservationDTO;
import com.utn.space.venueaapi.repository.ServiceSelectedRepository;
import com.utn.space.venueaapi.service.mappers.ServiceSelectedMapper;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
@AllArgsConstructor
@Service
public class ServiceSelectedService {
    @Autowired
    private final ServiceSelectedRepository serviceSelectedRepository;
    @Autowired
    private final ReservationService reservationService;
    @Autowired
    private final SpaceServiceItemService spaceServiceItemService;

    public List<ServiceSelectedDTO> getServicesSelectedOfReservation(Integer idReservation){
        return serviceSelectedRepository.findServiceSelectedByIdReservation(idReservation);
    }

    // Este metodo será usado para la inserción de reservas del lado del front.
    // Una vez que se envían los datos de la reserva y los servicios seleccionados
    // El botón de envío apuntaríá primero a los endpoints que validaran e ingresaran la reserva
    // Luego, con ese nuevo id de reserva se utilizaría este metodo de inserción de servicios seleccionados
    @Transactional
    public void insertServiceSelectedForAReservation(ServiceSelectedDTO serviceSelectedDTO){
        Reservation reservation = reservationService.findById(serviceSelectedDTO.idReservation());
        SpaceServiceItem serviceItem = spaceServiceItemService.findById(serviceSelectedDTO.idService());

        serviceSelectedRepository.save(ServiceSelectedMapper.toEntity(serviceSelectedDTO, serviceItem, reservation));
    }

    @Transactional
    public void insertListOfServicesSelectedInAReservation(Integer idReservation, List<ServiceSelectedWithoutReservationDTO> servicesSelectedDTO){
        Reservation reservation = reservationService.findById(idReservation);
        for(ServiceSelectedWithoutReservationDTO serviceSelectedDTO : servicesSelectedDTO){
            SpaceServiceItem serviceItem = spaceServiceItemService.findById(serviceSelectedDTO.idService());

            serviceSelectedRepository.save(ServiceSelectedMapper.toEntity(serviceSelectedDTO, serviceItem, reservation));

        }
    }

    @Transactional
    public void deleteServiceSelectedForAReservation(Integer id){
        if(!serviceSelectedRepository.existsById(id)) throw new NotFoundException("No se encontró el servicio seleccionado para eliminarlo");

        serviceSelectedRepository.deleteById(id);
    }
}
