package com.utn.space.venueaapi.service;

import com.utn.space.venueaapi.model.records.ServiceSelectedDTO;
import com.utn.space.venueaapi.repository.ReservationRepository;
import com.utn.space.venueaapi.repository.ServiceSelectedRepository;
import com.utn.space.venueaapi.repository.SpaceServiceItemRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ServiceSelectedService {
    private final ServiceSelectedRepository serviceSelectedRepository;
    private final ReservationRepository reservationRepository;
    private final SpaceServiceItemRepository spaceServiceItemRepository;

    public ServiceSelectedService(ServiceSelectedRepository repository, ReservationRepository reservationRepository, SpaceServiceItemRepository spaceServiceItemRepository) {
        this.serviceSelectedRepository = repository;
        this.reservationRepository = reservationRepository;
        this.spaceServiceItemRepository = spaceServiceItemRepository;
    }

    public List<ServiceSelectedDTO> getServicesSelectedOfReservation(Integer idReservation){
        return serviceSelectedRepository.findServiceSelectedByIdReservation(idReservation);
    }

    //Borra todos los Selected Services por id_reserva
    public void deleteSelectedServiceByReserveId (Integer id_reserva){
        serviceSelectedRepository.deleteSelectedServiceByReserveId(id_reserva);
    }
/*
    // Este metodo será usado para la inserción de reservas del lado del front.
    // Una vez que se envían los datos de la reserva y los servicios seleccionados
    // El botón de envío apuntaríá primero a los endpoints que validaran e ingresaran la reserva
    // Luego, con ese nuevo id de reserva se utilizaría este metodo de inserción de servicios seleccionados
    @Transactional
    public void insertServiceSelectedForAReservation(ServiceSelectedDTO serviceSelectedDTO){
        Reservation reservation = reservationRepository.findById(serviceSelectedDTO.idReservation()).orElseThrow(() -> new NotFoundException("No se ha encontrado la reserva cuyo servicio quiere seleccionarse"));
        SpaceServiceItem serviceItem = spaceServiceItemRepository.findById(serviceSelectedDTO.idService()).orElseThrow(() -> new NotFoundException("No se ha encontrado el servicio que se quiere seleccionar en una reserva"));

        repository.save(ServiceSelectedMapper.toEntity(serviceSelectedDTO, serviceItem, reservation));
    }

    @Transactional
    public void insertListOfServicesSelectedInAReservation(Integer idReservation, List<ServiceSelectedWithoutReservationDTO> servicesSelectedDTO){
        Reservation reservation = reservationRepository.findById(idReservation).orElseThrow(() -> new NotFoundException("No se ha encontrado la reserva cuyo servicio quiere seleccionarse"));
        for(ServiceSelectedWithoutReservationDTO serviceSelectedDTO : servicesSelectedDTO){
            SpaceServiceItem serviceItem = spaceServiceItemRepository.findById(serviceSelectedDTO.idService()).orElseThrow(() -> new NotFoundException("No se ha encontrado el servicio que se quiere seleccionar en una reserva"));

            repository.save(ServiceSelectedMapper.toEntity(serviceSelectedDTO, serviceItem, reservation));

        }
    }

    @Transactional
    public void deleteServiceSelectedForAReservation(Integer id){
        if(!repository.existsById(id)) throw new NotFoundException("No se encontró el servicio seleccionado para eliminarlo");

        repository.deleteById(id);
    }*/
}
