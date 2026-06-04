package com.utn.space.venueaapi.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

import java.time.LocalDate;
@AllArgsConstructor
@NoArgsConstructor
@Data
@Entity
@Table(name = "spaces")
public class Space {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id_space;

    @ManyToOne
    @JoinColumn(name = "id_consumer_owner")
    private Consumer consumer_owner;

    @ManyToOne
    @JoinColumn(name = "id_location")
    private Location location;

    @OneToOne
    @JoinColumn(name = "id_cancellation_policies")
    private CancellationPolicy cancellation_Policy;

    private String name_space;
    private String description;
    private Double base_price;
    private LocalDate publication_date;
    private Long buffer_time;
}
