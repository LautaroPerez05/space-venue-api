package com.utn.space.venueaapi.service;

import com.utn.space.venueaapi.exceptions.*;
import com.utn.space.venueaapi.model.*;
import com.utn.space.venueaapi.model.records.ReservationDTO;
import com.utn.space.venueaapi.model.records.ServiceSelectedDTO;
import com.utn.space.venueaapi.repository.PaymentRepository;
import com.utn.space.venueaapi.service.mappers.ReservationMapper;
import com.utn.space.venueaapi.repository.ReservationRepository;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

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
    private final GoogleCalendarService googleCalendarService;
    @Autowired
    private final PaymentRepository paymentRepository;
    @Autowired
    private final RefundService refundService;


    public List<Reservation> findAll (){
        return reservationRepository.findAll();
    }

    public Reservation findById (Integer id){
        return reservationRepository.findById(id).orElseThrow(()-> new IdNotFoundException("Reservacion",id));
    }

    private void validateReservationDates(LocalDateTime from, LocalDateTime until) {
        if (from.isBefore(LocalDateTime.now())) {
            throw new InvalidDateException("La fecha de inicio no puede ser una fecha del pasado");
        }
        if (until.isBefore(from)) {
            throw new InvalidDateException("La fecha final no puede ser antes que la fecha de inicio");
        }
    }

    @PreAuthorize("@securityUtils.isConsumerOfReservation(#id, authentication.name)" +
            "and @securityUtils.isSpaceOwnerOfReservation(#id, authentication.name)")
    public Reservation cancelReservation(Integer id) {
        Reservation aux = reservationRepository.findById(id).orElseThrow(
                () -> new IdNotFoundException("Reservacion",id));

        // Si la reserva estaba confirmada, significa que hubo un pago asociado
        if (aux.getStatus().equals(ReservationStatus.CONFIRMED)) {
            // Buscamos el registro del pago asociado en nuestra tabla
            PaymentModel payment = paymentRepository.findByReservationId(aux.getId());

            // Ejecutamos el reembolso total usando el servicio adquirido
            refundService.refundPayment(payment.getIdPayment(), payment.getTransactionAmount());
        }

        aux.setStatus(ReservationStatus.CANCELLED);
        return reservationRepository.save(aux);
    }

    private BigDecimal calculateAndUpdateFinalPrice(Reservation reservation, List<ServiceSelectedDTO> services) {
        BigDecimal totalServices = services.stream()
                .map(ServiceSelectedDTO::priceAtReservation) // O .getPrice() según tu DTO
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal finalPrice = reservation.getSpace().getBasePrice().add(totalServices);
        reservation.setFinalPrice(finalPrice);
        return finalPrice;
    }

    private void validateSpaceAvailability(Space space, Integer idConsumer) {
        if (space.getConsumerOwner().getIdConsumer().equals(idConsumer)) {
            throw new InvalidReservationException("No puedes realizar una reserva sobre tu propio espacio disponible.");
        }

        if (!space.getIsActive()) {
            throw new SpaceUnavailableException("El espacio seleccionado no se encuentra activo o disponible para nuevas reservas.");
        }
    }

    private String infoAndSyncGoogleCalendar(Reservation reservation, Consumer client, Space space, Boolean saveToMyCalendar) throws IOException {
        String tituloEvento = "Reserva: " + space.getNameSpace();
        String descripcionEvento = "Reserva confirmada de espacio. Cliente: " + client.getFirstname() + " " + client.getLastname();

        return googleCalendarService.sincronizarReservaMultiplesCalendarios(
                space.getGoogleCalendarId(),
                tituloEvento,
                descripcionEvento,
                reservation.getFromDate(),
                reservation.getUntilDate(),
                client.getEmail(),
                space.getConsumerOwner().getEmail(),
                saveToMyCalendar
        );
    }

    @Transactional(rollbackFor = Exception.class)
    @PreAuthorize("hasRole('CLIENT') " +
            "and @securityUtils.isCurrentConsumer(#dto.getIdConsumer(), authentication.name)")
    public Reservation createReservation(ReservationDTO dto) throws IOException {
        validateReservationDates(dto.getFromDate(), dto.getUntilDate());

        if (reservationRepository.existsOverlappingReservation(dto.getIdSpace(), dto.getFromDate(), dto.getUntilDate())) {
            throw new SpaceUnavailableException("El espacio ya se encuentra reservado en el rango de fechas seleccionado.");
        }

        Consumer client = consumerService.findById(dto.getIdConsumer());
        Space space = spaceService.findById(dto.getIdSpace());

        validateSpaceAvailability(space, client.getIdConsumer());

        Reservation reservation = reservationMapper.toEntity(dto);
        reservation.setCreatedAt(LocalDateTime.now());
        reservation.setConsumer(client);
        reservation.setSpace(space);

        List<ServiceSelectedDTO> servicesSelectedDTO = new ArrayList<>();
        calculateAndUpdateFinalPrice(reservation, servicesSelectedDTO);

        String idEventGoogle = infoAndSyncGoogleCalendar(reservation, client, space, dto.getSaveToMyCalendar());
        reservation.setGoogleEventCode(idEventGoogle);

        Reservation saved = reservationRepository.save(reservation);
        serviceSelectedService.insertListOfServicesSelectedInAReservation(saved.getId(), servicesSelectedDTO);

        return saved;
    }

    private void checkModificationPermission(Reservation reservation) {
        boolean isAdmin = Objects.requireNonNull(SecurityContextHolder.getContext().getAuthentication())
                .getAuthorities().stream().anyMatch(a -> Objects.equals(a.getAuthority(), "ROLE_ADMIN"));

        if (!isAdmin && !reservation.getStatus().equals(ReservationStatus.TENTATIVE)) {
            throw new AccessDeniedException("No se puede modificar una reserva procesada.");
        }
    }

    @Transactional(rollbackFor = Exception.class)
    @PreAuthorize("hasRole('ADMIN') or (hasRole('CLIENT') " +
            "and @securityUtils.isReservationOwner(#dto.id, authentication.name))")
    public Reservation modifyReservation(ReservationDTO dto){
        validateReservationDates(dto.getFromDate(), dto.getUntilDate());

        Reservation aux= reservationRepository.findById(dto.getId()).orElseThrow(
                () -> new IdNotFoundException("No se encontró la reserva al querer modificarla", dto.getId()));

        // Si es un CLIENT, solo puede modificarla si está TENTATIVE
        checkModificationPermission(aux);

        aux.setConsumer(consumerService.findById(dto.getIdConsumer()));
        aux.setSpace(spaceService.findById(dto.getIdSpace()));

        //limpiar servicios seleccionados anteriores
        serviceSelectedService.deleteSelectedServiceByReserveId(dto.getId());

        //Cargo los servicios nuevos desde el catalogo
        List<ServiceSelectedDTO> servicesSelectedDTO = new ArrayList<>();
        calculateAndUpdateFinalPrice(aux, servicesSelectedDTO);

        Reservation updated = reservationRepository.save(aux);

        //Guardo la lista de servicios modificados de la reserva en la tabla de servicios
        serviceSelectedService.insertListOfServicesSelectedInAReservation(
                updated.getId(), servicesSelectedDTO);

        return updated;
    }

    @PreAuthorize("@securityUtils.isSpaceOwnerOfReservation(#id, authentication.name)")
    public Reservation confirmReservation(Integer id){
        Reservation aux= reservationRepository.findById(id).orElseThrow(
                ()->new IdNotFoundException("Reservation", id));

        aux.setStatus(ReservationStatus.CONFIRMED);
        return reservationRepository.save(aux);
    }

    @PreAuthorize("@securityUtils.isSpaceOwnerOfReservation(#id, authentication.name)")
    public Reservation completeReservation(Integer id){
        Reservation aux= reservationRepository.findById(id).orElseThrow(
                ()->new IdNotFoundException("Reservation", id));

        aux.setStatus(ReservationStatus.COMPLETED);
        //falta sacarlo de googlecalendar
        return reservationRepository.save(aux);
    }

    public Reservation softDelete(Integer id){
        Reservation aux= reservationRepository.findById(id).orElseThrow(
                ()->new IdNotFoundException("Reservation", id));

        aux.setIsActive(false);
        return reservationRepository.save(aux);

    }

    public List<Reservation> findAllByConsumerId(Integer id){
        return reservationRepository.findAllByConsumer_IdConsumer(id);
    }
}
