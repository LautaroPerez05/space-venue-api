package com.utn.space.venueaapi.Model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "servicesselected")
@Getter @Setter
@AllArgsConstructor @NoArgsConstructor
@EqualsAndHashCode @ToString
public class ServicesSelected {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "id_service")
    private Services service;

    @ManyToOne
    @JoinColumn(name = "id_reservation")
    private Reservations reservation;
}
