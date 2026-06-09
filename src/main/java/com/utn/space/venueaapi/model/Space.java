package com.utn.space.venueaapi.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Entity
@Table(name = "spaces")
public class Space {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_space")
    private Integer idSpace;

    @ManyToOne
    @JoinColumn(name = "idConsumerOwner")
    private Consumer consumerOwner;

    @ManyToOne
    @JoinColumn(name = "idLocation")
    private Location location;

    @OneToOne
    @JoinColumn(name = "idCancellationPolicies")
    private CancellationPolicy cancellationPolicy;

    @Column(name = "googleCalendarId")
    private String googleCalendarId;

    @Column(name = "name_space")
    private String nameSpace;
    private String description;
    @Column(name = "base_price")
    private BigDecimal basePrice;
    @Column(name = "publication_date")
    private LocalDate publicationDate;
    @Column(name = "buffer_time")
    private Integer bufferTime;
    private Boolean isActive;

    /*
    @OneToMany(mappedBy = "space")
    @JsonIgnore
    private List<SpaceServiceItem> services;

    public Space(Integer id, Consumer consumer, Location location, CancellationPolicy cancellationPolicies, String s, String s1, String description, BigDecimal bigDecimal, LocalDate localDate, Integer integer) {
    }
    */
}
