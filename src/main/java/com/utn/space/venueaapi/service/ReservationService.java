package com.utn.space.venueaapi.service;

import com.utn.space.venueaapi.exceptions.ExceptionIdNotFound;
import com.utn.space.venueaapi.exceptions.ExceptionInvalidDate;
import com.utn.space.venueaapi.exceptions.ExceptionServiceOutOfPlace;
import com.utn.space.venueaapi.model.*;
import com.utn.space.venueaapi.model.records.ReservationDTO;
import com.utn.space.venueaapi.model.records.ServiceSelectedDTO;
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

        Consumer client = consumerService.findById(dto.getIdConsumer());
        aux.setConsumer(client);

        Space space = spaceService.findById(dto.getIdSpace());
        aux.setSpace(space);

        // Corrección: Validación contra el catálogo general y armado de seleccionados
        //Catalogo de servicios seleccionables para validar los elejidos
        List<ServiceSelectedDTO> servicesSelectedDTO = new ArrayList<>();
        BigDecimal totalServicios = BigDecimal.ZERO;

        for (Integer idService : dto.getIdServicesSelec()) {
            SpaceServiceItem servicioCatalogo = spaceServiceItemService.findById(idService);

            //Se verifica que cada uno de los servicios seleccionados de la reserva era efectivamente uno asociado al espacio de la reserva
            if (!servicioCatalogo.getSpace().getIdSpace().equals(space.getIdSpace())) {
                throw new ExceptionServiceOutOfPlace("El servicio con ID " + idService + " no corresponde al espacio seleccionado.");
            }

            // Crear DTO para el servicio seleccionado (con id=null porque se genera en la BDD)
            ServiceSelectedDTO ssDto = new ServiceSelectedDTO(
                    null,
                    servicioCatalogo.getPrice(),
                    null, // idReservation se asignará después de guardar
                    servicioCatalogo.getDescription()
            );
            servicesSelectedDTO.add(ssDto);
            totalServicios = totalServicios.add(servicioCatalogo.getPrice());
        }

        //Sumo al precio base el de los servicios
        aux.setFinalPrice(space.getBasePrice().add(totalServicios));

        // Extrae los datos dinámicos necesarios para configurar las invitaciones del evento
        String googleCalendarId = space.getGoogleCalendarId(); // ID del calendario guardado en el modelo Space
        String emailCliente = client.getEmail();                 // Email del consumidor para mandarle la invitación
        String emailOferente = space.getConsumerOwner().getEmail(); // Email del dueño/oferente del salón

        String tituloEvento = "Reserva: " + space.getNameSpace();
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

        //Guardamos la reserva
        Reservation saved = reservationRepository.save(aux);

        //Guardamos los servicios seleccionados
        serviceSelectedService.insertListOfServicesSelectedInAReservation(saved.getId(), servicesSelectedDTO);

        return saved;

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
        Reservation aux= reservationRepository.findById(dto.getId()).orElseThrow(() -> new ExceptionIdNotFound("Reservation", dto.getId()));
        aux.setConsumer(consumerService.findById(dto.getIdConsumer()));
        aux.setSpace(spaceService.findById(dto.getIdSpace()));


        //limpiar servicios seleccionados anteriores
        serviceSelectedService.deleteSelectedServiceByReserveId(dto.getId());

        //Cargo los servicios nuevos desde el catalogo
        List<ServiceSelectedDTO> servicesSelectedDTO = new ArrayList<>();
        BigDecimal totalServicios = BigDecimal.ZERO;

        for (Integer idService : dto.getIdServicesSelec()) {
            SpaceServiceItem item = spaceServiceItemService.findById(idService);
            if (!item.getSpace().getIdSpace().equals(aux.getSpace().getIdSpace())) {
                throw new ExceptionServiceOutOfPlace("El servicio con ID " + idService + " no corresponde al espacio seleccionado.");
            }

            ServiceSelectedDTO ssDto = new ServiceSelectedDTO(
                    null,
                    item.getPrice(),
                    dto.getId(),
                    item.getDescription()
            );
            servicesSelectedDTO.add(ssDto);
            totalServicios = totalServicios.add(item.getPrice());
        }

        //Recalculo el precio final
        aux.setFinalPrice(aux.getSpace().getBasePrice().add(totalServicios));
        //Guardo la reserva modificada
        Reservation updated = reservationRepository.save(aux);
        //Guardo la lista de servicios modificados de la reserva en la tabla de servicios
        serviceSelectedService.insertListOfServicesSelectedInAReservation(updated.getId(), servicesSelectedDTO);
        return updated;
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
