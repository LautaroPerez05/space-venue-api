package com.utn.space.venueaapi.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

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
    private Integer id_space;

    @ManyToOne
    @JoinColumn(name = "id_consumer_owner")
    private Consumer consumer_owner;

    @ManyToOne
    @JoinColumn(name = "id_location")
    private Location location;

    @OneToOne
    @JoinColumn(name = "id_cancellation_policies")
    private CancellationPolicy cancellation_Policy;

    @Column(name = "google_calendar_id")
    private String googleCalendarId;

    private String name_space;
    private String description;
    private BigDecimal base_price;
    private LocalDate publication_date;
    private Integer buffer_time;
    private Boolean isActive;

    @OneToMany(mappedBy = "space")
    @JsonIgnore
    private List<SpaceServiceItem> services;

    public Space(Integer id, Consumer consumer, Location location, CancellationPolicy cancellationPolicies, String s, String s1, String description, BigDecimal bigDecimal, LocalDate localDate, Integer integer) {
    }
}
