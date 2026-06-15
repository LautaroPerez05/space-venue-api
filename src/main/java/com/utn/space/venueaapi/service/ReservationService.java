package com.utn.space.venueaapi.service;

import com.google.api.client.util.DateTime;
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

    @Autowired
    private ReservationRepository reservationRepository;

    @Autowired
    private SpaceService spaceService;


    private final SpaceServiceItemRepository spaceServiceItemRepository;
    private final GoogleCalendarService googleCalendarService;

    public ReservationService( SpaceServiceItemRepository spaceServiceItemRepository, GoogleCalendarService googleCalendarService) {

        this.spaceServiceItemRepository = spaceServiceItemRepository;
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

    public List<Reservation> findAllForLoggedConsumer() {
        Integer loggedCustomerId = consumerService.getLoggedConsumerId();
        return reservationRepository.findAllByConsumer_IdConsumer(loggedCustomerId);
    }

    private boolean isSpaceAvailableBetweenDates(LocalDateTime from, LocalDateTime until, Integer spaceId){
        List<Reservation> reservationsForSpace = reservationRepository.findAllBySpace_IdSpace(spaceId);
        reservationsForSpace = reservationsForSpace.stream().
                filter(reservation -> reservation.getStatus().equals(ReservationStatus.CANCELLED)
                        || reservation.getStatus().equals(ReservationStatus.REJECTED)
                        || !reservation.getIsActive()).toList(); //filtro reservas canceladas rechazadas o inactivas

        //Busco si alguna de las reservas que quedan se solapa con la reserva actual
        return !reservationsForSpace.stream().anyMatch(reservation -> !reservation.getFromDate().isAfter(until) && !reservation.getUntilDate().isBefore(from));
    }

    @Transactional
    public Reservation create(ReservationDTO dto) throws IOException {
        if (dto.untilDate().isBefore(dto.fromDate())) {
            throw new InvalidDateException("La Fecha Final no puede ser antes que la Fecha de Inicio");
        }
        if (dto.fromDate().isBefore(LocalDateTime.now())) {
            throw new InvalidDateException("La Fecha de Inicio no puede ser en el pasado.");
        }

        Integer idLogueado = consumerService.getLoggedConsumerId();

        if (!isSpaceAvailableBetweenDates(dto.fromDate(),dto.untilDate(),dto.idSpace())){
            throw new InvalidReservationException("La reserva no esta disponible en la fechas seleccionadas");
        }

        Consumer client = consumerService.findById(dto.idConsumer());
        if(!idLogueado.equals(dto.idConsumer())) {
            client = consumerService.findById(idLogueado);
            /*throw new InvalidDataException("El id del usuario logeado no coresponde con el ingresado por el front: " +
                    "usLog=" + idLogueado + ", usDto=" + dto.idConsumer());*/
        }

        Space space = spaceService.findById(dto.idSpace());

        if (space.getConsumerOwner().getIdConsumer().equals(idLogueado)) {
            throw new SelfReservationException("Señor Administrador/Anfitrión: No puede reservar su propio espacio comercial.");
        }

        Reservation aux = reservationMapper.toEntity(dto);
        aux.setCreatedAt(LocalDateTime.now());
        aux.setStatus(ReservationStatus.TENTATIVE); // Aseguramos el estado inicial por defecto
        aux.setIsActive(true);

        aux.setConsumer(client);

        aux.setSpace(space);

        List<ServiceSelected> serviciosSeleccionados = new ArrayList<>();
        BigDecimal totalServicios = BigDecimal.ZERO;

        // Defensa contra nulos si el usuario no seleccionó ningún opcional
        if (dto.idServicesSelec() != null) {
            for (Integer idService : dto.idServicesSelec()) {
                SpaceServiceItem servicioCatalogo = spaceServiceItemRepository.findById(idService)
                        .orElseThrow(() -> new IdNotFoundException("Servicio Catálogo", idService));

                if (!servicioCatalogo.getSpace().getIdSpace().equals(space.getIdSpace())) {
                    throw new ServiceOutOfPlaceException("El servicio con ID " + idService + " no corresponde al espacio seleccionado.");
                }

                ServiceSelected selected = new ServiceSelected(servicioCatalogo, aux);
                totalServicios = totalServicios.add(servicioCatalogo.getPrice());
                serviciosSeleccionados.add(selected);
            }
        }

        aux.setServices(serviciosSeleccionados);
        aux.setFinalPrice(space.getBasePrice().add(totalServicios));

        // =================================================================
        // INTEGRACIÓN GOOGLE CALENDAR (IGNORA EL NULL DE FORMA SEGURA) Por si no quiero usar Google Calendar
        // =================================================================
        String googleCalendarId = space.getGoogleCalendarId();

        // 1. Filtro inteligente: Solo entra a Google si el ID NO es nulo ni está vacío
        if (googleCalendarId != null && !googleCalendarId.trim().isEmpty()) {
            String emailCliente = client.getEmail();
            String emailOferente = space.getConsumerOwner().getEmail();
            String tituloEvento = "Reserva: " + space.getNameSpace() + " - " + dto.title();
            String descriptionEvento = dto.description() + "\nOrganizador: " + client.getFirstname() + " " + client.getLastname();

            try {
                String idEventoGoogle = googleCalendarService.sincronizarReservaMultiplesCalendarios(
                        googleCalendarId,
                        tituloEvento,
                        descriptionEvento,
                        aux.getFromDate(),
                        aux.getUntilDate(),
                        emailCliente,
                        emailOferente,
                        dto.getSaveToMyCalendar()
                );
                aux.setGoogleEventCode(idEventoGoogle);
                System.out.println("✅ Sincronización con Google Calendar exitosa.");
            } catch (IOException e) {
                // Logueamos el error de red/credenciales, pero NO tiramos "throw e;"
                // Así, si Google falla, la reserva en tu BD local SE GUARDA IGUAL.
                System.err.println("⚠️ ALERTA: Falló la API de Google, pero la reserva se creó localmente.");
            }
        } else {
            // 2. Si el espacio se creó recién y está en NULL, el flujo pasa por acá limpiamente
            System.out.println("ℹ️ El espacio #" + space.getIdSpace() + " no tiene Google Calendar configurado. Saltando paso.");
        }

        // Se guarda en la BD local pase lo que pase con Google
        return reservationRepository.save(aux);
    }


    @Transactional
    public Reservation modify (ReservationDTO dto){
        if (dto.untilDate().isBefore(dto.fromDate())) {
            throw new InvalidDateException("La Fecha Final no puede ser antes que la Fecha de Inicio");
        }
        if (dto.fromDate().isBefore(LocalDateTime.now())) {
            throw new InvalidDateException("La Fecha Final no puede ser antes que la Fecha de Inicio");
        }
        if(!reservationRepository.existsById(dto.id())){
            throw new IdNotFoundException ("Reservation", dto.id());
        }
        Reservation nuevaReserva = reservationMapper.toEntity(dto);

        nuevaReserva.setConsumer(consumerService.findById(dto.idConsumer()));

        nuevaReserva.setSpace(spaceService.findById(dto.idSpace()));


        //limpiar servicios seleccionados anteriores
        serviceSelectedService.deleteSelectedServiceByReserveId(dto.id());

        //Los cargo de nuevo y actualizado
        List<ServiceSelected> list= new ArrayList<>();
        list= nuevaReserva.getSpace().getServices().stream()
                .filter(item->dto.idServicesSelec().contains(item.getId()))//filtro todos los serviceItem Seleccionados para la reserva
                .map(item-> new ServiceSelected(item, nuevaReserva))  //transformo los item en serviceSelected
                .toList();

        nuevaReserva.setServices(list);

        nuevaReserva.setFinalPrice(
                nuevaReserva.getSpace().getBasePrice().add( //el + no funciona con bigDecimal
                        nuevaReserva.getServices()
                                .stream()
                                .map(ServiceSelected::getPriceAtReservation)
                                .reduce(BigDecimal.ZERO, BigDecimal::add)
                )
        );
        return reservationRepository.save(nuevaReserva);
    }

    public Reservation confirmReservation(Integer id){
        Reservation aux= reservationRepository.findById(id).orElseThrow(()->new IdNotFoundException ("Reservation", id));
        aux.setStatus(ReservationStatus.CONFIRMED);
        return reservationRepository.save(aux);
    }

    public Reservation rejectReservation(Integer id){
        Reservation aux= reservationRepository.findById(id).orElseThrow(()->new IdNotFoundException ("Reservation", id));
        aux.setStatus(ReservationStatus.REJECTED);
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
