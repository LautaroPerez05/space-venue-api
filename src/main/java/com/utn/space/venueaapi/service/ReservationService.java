package com.utn.space.venueaapi.service;

import com.utn.space.venueaapi.exceptions.*;
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
    @Autowired
    private ReservationMapper reservationMapper;

    @Autowired
    private ServiceSelectedService serviceSelectedService;

    @Autowired
    private ConsumerService consumerService;

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

    ///--------------------------------------------Metodos------------------------------------------------------------------------------

    public List<Reservation> findAll (){
        return reservationRepository.findAll();
    }

    public Reservation findById (Integer id){
        return reservationRepository.findById(id).orElseThrow(()-> new IdNotFoundException("Reservacion",id));
    }

    public List<Reservation> findByIdConsumer(Integer id){
        return reservationRepository.findAllByConsumer_IdConsumer(id);
    }

    public Reservation create (ReservationDTO dto) throws IOException {
        if (dto.getUntilDate().isBefore(dto.getFromDate())) {
            throw new InvalidDateException("La Fecha Final no puede ser antes que la Fecha de Inicio");
        }
        if (dto.getFromDate().isBefore(LocalDateTime.now())) {
            throw new InvalidDateException("La Fecha Final no puede ser antes que la Fecha de Inicio");
        }

        Reservation aux = reservationMapper.toEntity(dto);
        aux.setCreatedAt(LocalDateTime.now());

        Consumer client = consumerRepository.findById(dto.getIdConsumer())
                .orElseThrow(() -> new IdNotFoundException("Consumer", dto.getIdConsumer()));
        aux.setConsumer(client);

        Space space = spaceRepository.findById(dto.getIdSpace())
                .orElseThrow(() -> new IdNotFoundException("Space", dto.getIdSpace()));
        aux.setSpace(space);

        // Corrección: Validación contra el catálogo general y armado de seleccionados
        List<ServiceSelected> serviciosSeleccionados = new ArrayList<>();
        BigDecimal totalServicios = BigDecimal.ZERO;

        for (Integer idService : dto.getIdServicesSelec()) {
            SpaceServiceItem servicioCatalogo = spaceServiceItemRepository.findById(idService)
                    .orElseThrow(() -> new IdNotFoundException("Servicio Catálogo", idService));

            if (!servicioCatalogo.getSpace().getIdSpace().equals(space.getIdSpace())) {
                throw new ServiceOutOfPlaceException("El servicio con ID " + idService + " no corresponde al espacio seleccionado.");
            }

            ServiceSelected selected = new ServiceSelected();
            selected.setReservation(aux);
            selected.setDescriptionFrozen(servicioCatalogo.getDescription());
            selected.setPriceAtReservation(servicioCatalogo.getPrice());

            totalServicios = totalServicios.add(servicioCatalogo.getPrice());

            serviciosSeleccionados.add(selected);
        }

        aux.setServices(serviciosSeleccionados);

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

        return reservationRepository.save(aux);

    }

    public Reservation modify (ReservationDTO dto){
        if (dto.getUntilDate().isBefore(dto.getFromDate())) {
            throw new InvalidDateException("La Fecha Final no puede ser antes que la Fecha de Inicio");
        }
        if (dto.getFromDate().isBefore(LocalDateTime.now())) {
            throw new InvalidDateException("La Fecha Final no puede ser antes que la Fecha de Inicio");
        }
        if(!reservationRepository.existsById(dto.getId())){
            throw new IdNotFoundException ("Reservation", dto.getId());
        }
        Reservation aux= reservationMapper.toEntity(dto);

        aux.setConsumer(consumerRepository.findById(dto.getIdConsumer())
                .orElseThrow(()->new IdNotFoundException("Consumer", dto.getIdConsumer())));

        aux.setSpace(spaceRepository.findById(dto.getIdSpace())
                .orElseThrow(()->new IdNotFoundException("Space",dto.getIdSpace())));


        //limpiar servicios seleccionados anteriores
        serviceSelectedService.deleteSelectedServiceByReserveId(dto.getId());

        //Los cargo de nuevo y actualizado
        List<ServiceSelected> list= new ArrayList<>();
        list= aux.getSpace().getServices().stream()
                .filter(item->dto.getIdServicesSelec().contains(item.getId()))//filtro todos los serviceItem Seleccionados para la reserva
                .map(item-> new ServiceSelected(item,aux))  //transformo los item en serviceSelected
                .toList();
        aux.setServices(list);

        aux.setFinalPrice(
                aux.getSpace().getBasePrice().add( //el + no funciona con bigDecimal
                        aux.getServices()
                                .stream()
                                .map(ServiceSelected::getPriceAtReservation)
                                .reduce(BigDecimal.ZERO, BigDecimal::add)
                )
        );
        return reservationRepository.save(aux);
    }

    public Reservation confirmReservation(Integer id){
        Reservation aux= reservationRepository.findById(id).orElseThrow(()->new IdNotFoundException ("Reservation", id));
        aux.setStatus(ReservationStatus.CONFIRMED);
        return reservationRepository.save(aux);
    }

    public Reservation cancelReservation(Integer id){
        Reservation aux= reservationRepository.findById(id).orElseThrow(()->new IdNotFoundException ("Reservation", id));
        aux.setStatus(ReservationStatus.CANCELLED);
        return reservationRepository.save(aux);
    }

    public Reservation completeReservation(Integer id){
        Reservation aux= reservationRepository.findById(id).orElseThrow(()->new IdNotFoundException ("Reservation", id));
        aux.setStatus(ReservationStatus.COMPLETED);
        //falta sacarlo de googlecalendar
        return reservationRepository.save(aux);
    }

    public Reservation softDelete(Integer id){
        Reservation aux= reservationRepository.findById(id).orElseThrow(()->new IdNotFoundException ("Reservation", id));
        aux.setIsActive(false);
        return reservationRepository.save(aux);

    }
}
