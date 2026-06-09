package com.utn.space.venueaapi.model;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
// Este objeto guarda los servicios asociados a una cuenta en el momento de su reserva con el precio que tenian
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

    @ManyToOne
    @JoinColumn(name = "id_reservation")
    @ToString.Exclude
    private Reservation reservation;

    @Column(name = "price_at_reservation")
    BigDecimal priceAtReservation;

    @Column(name = "description_frozen")
    String descriptionFrozen;

}
