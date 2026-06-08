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
    private Integer id_service_selected;

    @Column(name = "price_at_reservation")
    private BigDecimal price_at_reservation;

    private String description;

    @ManyToOne
    @JoinColumn(name = "id_reservation")
    @ToString.Exclude
    private Reservation reservation;

    public ServiceSelected(SpaceServiceItem item, Reservation res) {
        this.price_at_reservation = item.getPrice();
        this.description=item.getDescription();
        this.reservation=res;
    }
}
