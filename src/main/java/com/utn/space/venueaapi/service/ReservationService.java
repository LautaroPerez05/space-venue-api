package com.utn.space.venueaapi.service;

import com.utn.space.venueaapi.exceptions.ExceptionIdNotFound;
import com.utn.space.venueaapi.exceptions.ExceptionInvalidDate;
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
    private ReservationMapper reservationMapper;
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

        Consumer client = consumerService.findById(dto.getId_consumer());
        aux.setConsumer(client);

        Space space = spaceService.findById(dto.getId_space());
        aux.setSpace(space);

        // Corrección: Validación contra el catálogo general y armado de seleccionados
        List<ServiceSelected> serviciosSeleccionados = new ArrayList<>();
        BigDecimal totalServicios = BigDecimal.ZERO;

        for (Integer idService : dto.getId_servicesSelec()) {
            SpaceServiceItem servicioCatalogo = spaceServiceItemService.findById(idService);

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
}
