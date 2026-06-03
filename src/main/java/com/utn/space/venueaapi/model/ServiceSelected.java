package com.utn.space.venueaapi.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "servicesselected")
@Getter @Setter
@AllArgsConstructor @NoArgsConstructor
@EqualsAndHashCode @ToString
public class ServiceSelected {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "price_at_reservation")
    Double priceAtReservation;

    @ManyToOne
    @JoinColumn(name = "id_service")
    private SpaceServiceItem service;

    @ManyToOne
    @JoinColumn(name = "id_reservation")
    private Reservation reservation;

    public ServiceSelected(Double priceAtReservation, SpaceServiceItem service, Reservation reservation) {
        this.id = 0L;
        this.priceAtReservation = priceAtReservation;
        this.service = service;
        this.reservation = reservation;
    }
}
