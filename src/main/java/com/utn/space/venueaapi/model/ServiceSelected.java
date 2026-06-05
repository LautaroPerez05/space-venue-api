package com.utn.space.venueaapi.model;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "servicesselected")
@Getter @Setter
@AllArgsConstructor @NoArgsConstructor
@EqualsAndHashCode @ToString
public class ServiceSelected {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_service_selected")
    private Integer id;

    @Column(name = "price_at_reservation")
    BigDecimal priceAtReservation;

    @ManyToOne
    @JoinColumn(name = "id_service")
    private SpaceServiceItem service;

    @ManyToOne
    @JoinColumn(name = "id_reservation")
    @ToString.Exclude
    private Reservation reservation;

    public ServiceSelected(BigDecimal priceAtReservation, SpaceServiceItem service, Reservation reservation) {
        this.id = 0;
        this.priceAtReservation = priceAtReservation;
        this.service = service;
        this.reservation = reservation;
    }
}
