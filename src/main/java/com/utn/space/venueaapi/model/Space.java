package com.utn.space.venueaapi.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

import java.time.LocalDate;

@NoArgsConstructor
@Data
@Entity
//Automatiza el soft delete en el repositorio (hace un UPDATE en vez de un DELETE)
@SQLDelete(sql = "UPDATE spaces SET active = false WHERE id = ?")
//Hace que no se levanten de la tabla elementos que no esten activos
@SQLRestriction("active = true")
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
    private Boolean active;

    //Creo el constructor sin usar lombok para poder asignarle true a active.
    public Space(Long id_space,Consumer consumer_owner, Location location, CancellationPolicy cancellation_Policy, String name_space, String description, Double base_price, LocalDate publication_date, Long buffer_time) {
        this.id_space = id_space;
        this.consumer_owner = consumer_owner;
        this.location = location;
        this.cancellation_Policy = cancellation_Policy;
        this.name_space = name_space;
        this.description = description;
        this.base_price = base_price;
        this.publication_date = publication_date;
        this.buffer_time = buffer_time;
        this.active = true;
    }
}
