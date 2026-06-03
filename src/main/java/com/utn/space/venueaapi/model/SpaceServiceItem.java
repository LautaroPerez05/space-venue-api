package com.utn.space.venueaapi.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "services")
@Getter @Setter
@AllArgsConstructor @NoArgsConstructor
@EqualsAndHashCode @ToString
public class SpaceServiceItem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String description;
    private Double price;
    private Boolean isActive;

    @ManyToOne
    @JoinColumn(name = "id_space")
    private Space space;
}
