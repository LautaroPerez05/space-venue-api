package com.utn.space.venueaapi.model;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.utn.space.venueaapi.model.flags.Create;
import com.utn.space.venueaapi.model.flags.Update;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
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

    @ManyToOne(cascade = CascadeType.PERSIST)
    @JoinColumn(name = "idLocation")
    private Location location;

    @ManyToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "idCancellationPolicies")
    private CancellationPolicies cancellationPolicies;

    @Column(name = "googleCalendarId", nullable = true)//Para que Google calendar sea opcional
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


    @OneToMany(mappedBy = "space", cascade = CascadeType.ALL, fetch = FetchType.EAGER, orphanRemoval = true)
    @JsonManagedReference
    private List<SpaceServiceItem> services;

    public Space(Integer id, Consumer consumer, Location location, CancellationPolicies cancellationPolicies, String s, String s1, String description, BigDecimal bigDecimal, LocalDate localDate, Integer integer) {
    }

    public Space(Object o, Consumer byId, Location byId1, CancellationPolicies byId2, @NotBlank(groups = {Create.class, Update.class}) String s, @NotBlank(groups = {Create.class, Update.class}) String s1, @NotBlank(groups = {Create.class, Update.class}) String description, @NotBlank(groups = {Create.class, Update.class}) @Positive(groups = {Create.class, Update.class}) BigDecimal bigDecimal, @NotBlank(groups = {Create.class, Update.class}) LocalDate localDate, @NotBlank(groups = {Create.class, Update.class}) Integer integer, boolean b) {
    }
}
