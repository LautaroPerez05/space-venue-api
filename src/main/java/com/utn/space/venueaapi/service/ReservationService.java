package com.utn.space.venueaapi.service;

import com.utn.space.venueaapi.exceptions.ExceptionIdNotFound;
import com.utn.space.venueaapi.exceptions.ExceptionInvalidDate;
import com.utn.space.venueaapi.exceptions.ExceptionServiceOutOfPlace;
import com.utn.space.venueaapi.model.*;
import com.utn.space.venueaapi.model.records.ReservationDTO;
import com.utn.space.venueaapi.service.mappers.ReservationMapper;
import com.utn.space.venueaapi.repository.ReservationRepository;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
@AllArgsConstructor
@Service
public class ReservationService {
    @Autowired
    private final ReservationMapper reservationMapper;
    @Autowired
    private ServiceSelectedService serviceSelectedService;
    @Autowired
    private final ReservationRepository reservationRepository;
    @Autowired
    private final ConsumerService consumerService;
    @Autowired
    private final SpaceService spaceService;
    @Autowired
    private final SpaceServiceItemService spaceServiceItemService;
    @Autowired
    private final GoogleCalendarService googleCalendarService;


    ///--------------------------------------------Metodos------------------------------------------------------------------------------

    public List<Reservation> findAll (){
        return reservationRepository.findAll();
    }

    public Reservation findById (Integer id){
        return reservationRepository.findById(id).orElseThrow(()-> new ExceptionIdNotFound("Reservacion",id));
    }

    public Reservation create (ReservationDTO dto) throws IOException {
        if (dto.getUntilDate().isBefore(dto.getFromDate())) {
            throw new ExceptionInvalidDate("La Fecha Final no puede ser antes que la Fecha de Inicio");
        }
        if (dto.getFromDate().isBefore(LocalDateTime.now())) {
            throw new ExceptionInvalidDate("La fecha de Inicio no puede del pasado");
        }

        Reservation aux = reservationMapper.toEntity(dto);
        aux.setCreatedAt(LocalDateTime.now());

        Consumer client = consumerService.findById(dto.getId_consumer());
        aux.setConsumer(client);

        Space space = spaceService.findById(dto.getId_space());
        aux.setSpace(space);

        // Corrección: Validación contra el catálogo general y armado de seleccionados
        List<ServiceSelected> serviciosSeleccionados = new ArrayList<>();
        BigDecimal totalServicios = BigDecimal.ZERO;

        for (Integer idService : dto.getId_servicesSelec()) {
            SpaceServiceItem servicioCatalogo = spaceServiceItemService.findById(idService);

            //Se verifica que cada uno de los servicios seleccionados de la reserva era efectivamente uno asociado al espacio de la reserva
            if (!servicioCatalogo.getSpace().getId_space().equals(space.getId_space())) {
                throw new ExceptionServiceOutOfPlace("El servicio con ID " + idService + " no corresponde al espacio seleccionado.");
            }

            //Se crea un objeto de tipo ServiceSelected que guardara la info del servicio exacto que fue asociado a la reserva
            ServiceSelected selected = new ServiceSelected();
            selected.setReservation(aux);
            selected.setPriceAtReservation(servicioCatalogo.getPrice());
            selected.setDescriptionFrozen(aux.getDescription());

            totalServicios = totalServicios.add(servicioCatalogo.getPrice());

            serviciosSeleccionados.add(selected);
        }

        aux.setServices(serviciosSeleccionados);

        aux.setFinalPrice(space.getBase_price().add(totalServicios));

        // Extrae los datos dinámicos necesarios para configurar las invitaciones del evento
        String googleCalendarId = space.getGoogleCalendarId(); // ID del calendario guardado en el modelo Space
        String emailCliente = client.getEmail();                 // Email del consumidor para mandarle la invitación
        String emailOferente = space.getConsumer_owner().getEmail(); // Email del dueño/oferente del salón

        String tituloEvento = "Reserva: " + space.getName_space();
        String descripcionEvento = "Reserva confirmada de espacio. Cliente: " + client.getFirstname() + " " + client.getLastname();

        // Invoca el metodo remoto de sincronización múltiple pasando los parámetros correspondientes
        String idEventoGoogle = googleCalendarService.sincronizarReservaMultiplesCalendarios(
                googleCalendarId,
                tituloEvento,
                descripcionEvento,
                aux.getFromDate(),
                aux.getUntilDate(),
                emailCliente,
                emailOferente,
                dto.getSaveToMyCalendar() // Campo booleano provisto por el DTO para respetar la decisión del usuario
        );

        // 3. Setea el código hash único devuelto por Google en tu atributo de base de datos local
        aux.setGoogleEventCode(idEventoGoogle);

        return reservationRepository.save(aux);

    }

    public Reservation modify (ReservationDTO dto){
        if (dto.getUntilDate().isBefore(dto.getFromDate())) {
            throw new ExceptionInvalidDate("La Fecha Final no puede ser antes que la Fecha de Inicio");
        }
        if (dto.getFromDate().isBefore(LocalDateTime.now())) {
            throw new ExceptionInvalidDate("La Fecha Final no puede ser antes que la Fecha de Inicio");
        }
        if(!reservationRepository.existsById(dto.getId())){
            throw new ExceptionIdNotFound ("Reservation", dto.getId());
        }
        Reservation aux= reservationMapper.toEntity(dto);

        aux.setConsumer(consumerService.findById(dto.getId_consumer()));

        aux.setSpace(spaceService.findById(dto.getId_space()));


        //limpiar servicios seleccionados anteriores
        serviceSelectedService.deleteSelectedServiceByReserveId(dto.getId());

        //Los cargo de nuevo y actualizado
        List<ServiceSelected> list= new ArrayList<>();
        list= aux.getSpace().getServices().stream()
                .filter(item->dto.getId_servicesSelec().contains(item.getId()))//filtro todos los serviceItem Seleccionados para la reserva
                .map(item-> new ServiceSelected(item,aux))  //transformo los item en serviceSelected
                .toList();
        aux.setServices(list);

        aux.setFinalPrice(
                aux.getSpace().getBase_price().add( //el + no funciona con bigDecimal
                        aux.getServices()
                                .stream()
                                .map(ServiceSelected::getPriceAtReservation)
                                .reduce(BigDecimal.ZERO, BigDecimal::add)
                )
        );
        return reservationRepository.save(aux);
    }


    public Reservation confirmReservation(Integer id){
        Reservation aux= reservationRepository.findById(id).orElseThrow(()->new ExceptionIdNotFound ("Reservation", id));
        aux.setStatus(ReservationStatus.CONFIRMED);
        return reservationRepository.save(aux);
    }

    public Reservation cancelReservation(Integer id){
        Reservation aux= reservationRepository.findById(id).orElseThrow(()->new ExceptionIdNotFound ("Reservation", id));
        aux.setStatus(ReservationStatus.CANCELLED);
        return reservationRepository.save(aux);
    }

    public Reservation completeReservation(Integer id){
        Reservation aux= reservationRepository.findById(id).orElseThrow(()->new ExceptionIdNotFound ("Reservation", id));
        aux.setStatus(ReservationStatus.COMPLETED);
        //falta sacarlo de googlecalendar
        return reservationRepository.save(aux);
    }

    public Reservation softDelete(Integer id){
        Reservation aux= reservationRepository.findById(id).orElseThrow(()->new ExceptionIdNotFound ("Reservation", id));
        aux.setIsActive(false);
        return reservationRepository.save(aux);

    }
}
