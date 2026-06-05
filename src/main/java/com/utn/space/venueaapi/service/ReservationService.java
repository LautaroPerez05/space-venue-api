package com.utn.space.venueaapi.service;

import com.utn.space.venueaapi.exceptions.ExceptionIdNotFound;
import com.utn.space.venueaapi.exceptions.ExceptionInvalidDate;
import com.utn.space.venueaapi.model.*;
import com.utn.space.venueaapi.model.records.ReservationDTO;
import com.utn.space.venueaapi.repository.ConsumerRepository;
import com.utn.space.venueaapi.repository.SpaceRepository;
import com.utn.space.venueaapi.repository.SpaceServiceItemRepository;
import com.utn.space.venueaapi.service.mappers.ReservationMapper;
import com.utn.space.venueaapi.repository.ReservationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class ReservationService {
    private ReservationMapper reservationMapper;

    private final ReservationRepository reservationRepository;
    private final ConsumerRepository consumerRepository;
    private final SpaceRepository spaceRepository;
    private final SpaceServiceItemRepository spaceServiceItemRepository;
    private final GoogleCalendarService googleCalendarService;

    public ReservationService(ReservationRepository reservationRepository, ConsumerRepository consumerRepository, SpaceRepository spaceRepository, SpaceServiceItemRepository spaceServiceItemRepository, GoogleCalendarService googleCalendarService) {
        this.reservationRepository = reservationRepository;
        this.consumerRepository = consumerRepository;
        this.spaceServiceItemRepository = spaceServiceItemRepository;
        this.spaceRepository = spaceRepository;
        this.googleCalendarService = googleCalendarService;
    }

    
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
            throw new ExceptionInvalidDate("La Fecha Final no puede ser antes que la Fecha de Inicio");
        }

        Reservation aux = reservationMapper.toEntity(dto);
        aux.setCreatedAt(LocalDateTime.now());

        Consumer client = consumerRepository.findById(dto.getId_consumer())
                .orElseThrow(() -> new ExceptionIdNotFound("Consumer", dto.getId_consumer()));
        aux.setConsumer(client);

        Space space = spaceRepository.findById(dto.getId_space())
                .orElseThrow(() -> new ExceptionIdNotFound("Space", dto.getId_space()));
        aux.setSpace(space);

        // Corrección: Validación contra el catálogo general y armado de seleccionados
        List<ServiceSelected> serviciosSeleccionados = new ArrayList<>();
        BigDecimal totalServicios = BigDecimal.ZERO;

        for (Integer idService : dto.getId_servicesSelec()) {
            SpaceServiceItem servicioCatalogo = spaceServiceItemRepository.findById(idService)
                    .orElseThrow(() -> new ExceptionIdNotFound("Servicio Catálogo", idService));

            if (!servicioCatalogo.getSpace().getId_space().equals(space.getId_space())) {
                throw new RuntimeException("El servicio con ID " + idService + " no corresponde al espacio seleccionado.");
            }

            ServiceSelected selected = new ServiceSelected();
            selected.setReservation(aux);
            selected.setService(servicioCatalogo);
            selected.setPriceAtReservation(servicioCatalogo.getPrice());

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


  //  hola

}
