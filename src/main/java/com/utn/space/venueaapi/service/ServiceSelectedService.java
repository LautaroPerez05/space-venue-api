package com.utn.space.venueaapi.service;

import com.utn.space.venueaapi.exceptions.IdNotFoundException;
import com.utn.space.venueaapi.model.records.ServiceSelectedDTO;
import com.utn.space.venueaapi.repository.ReservationRepository;
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
    private final ReservationRepository reservationRepository;
    @Autowired
    private final SpaceServiceItemService spaceServiceItemService;

    public List<ServiceSelectedDTO> getServicesSelectedOfReservation(Integer idReservation){
        return serviceSelectedRepository.findServiceSelectedByIdReservation(idReservation);
    }

    // Este metodo será usado para la inserción de reservas del lado del front.
    // Una vez que se envían los datos de la reserva y los servicios seleccionados
    // El botón de envío apuntaríá primero a los endpoints que validaran e ingresaran la reserva
    // Luego, con ese nuevo id de reserva se utilizaría este metodo de inserción de servicios seleccionados
    /*
    @Transactional
    public void insertServiceForAReservation(ServiceSelectedDTO serviceSelectedDTO){
        //Este metodo recibe 1 servicio seleccionado para la reserva por DTO y lo guarda en la tabla de ServicesSelected
        Reservation reservation = reservationService.findById(serviceSelectedDTO.idReservation());
        serviceSelectedRepository.save(ServiceSelectedMapper.toEntity(serviceSelectedDTO, reservation));
    }
    */

    // Yo usaría solo un metod que inserta una lista de servicios para una reserva
    @Transactional
    public void insertListOfServicesSelectedInAReservation(Integer idReservation, List<ServiceSelectedDTO> servicesSelectedDTO){
        //Este metodo recibe el id de una reserva y una lista de servicios seleccionados (por servicesSelectedDTO) y los mete en la tabla ServicesSelected
        for(ServiceSelectedDTO serviceSelectedDTO : servicesSelectedDTO){
            serviceSelectedRepository.save(ServiceSelectedMapper.toEntity(serviceSelectedDTO,
                    reservationRepository.findById(idReservation)
                            .orElseThrow(() -> new IdNotFoundException(
                                    "No se ha encontrado la reserva para insertar lista de servicios seleccionados: ", idReservation))));
        }
    }

    @Transactional
    public void deleteServiceSelectedForAReservation(Integer id){
        if(!serviceSelectedRepository.existsById(id)) throw new IdNotFoundException("No se encontró el servicio seleccionado para eliminarlo: ", id);

        serviceSelectedRepository.deleteById(id);
    }

    //Borra todos los Selected Services por id_reserva
    @Transactional
    public void deleteSelectedServiceByReserveId (Integer id_reserva){
        serviceSelectedRepository.deleteSelectedServiceByReserveId(id_reserva);
    }
}
