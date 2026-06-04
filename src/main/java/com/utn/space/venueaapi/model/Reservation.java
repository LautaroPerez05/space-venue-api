package com.utn.space.venueaapi.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Reservation {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;
    private String description;

    @Column(name = "google_event_code")
    private String googleEventCode;

    @Column(name = "from_date")
    private LocalDateTime fromDate;

    @Column(name = "until_date")
    private LocalDateTime untilDate;

    @Column(name = "final_price")
    private Double finalPrice;

    private ReservationStatus status;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    private Boolean isActive=true;


    @ManyToOne
    @JoinColumn(name = "id_consumer")
    private Consumer consumer;

    @ManyToOne
    @JoinColumn(name = "id_space")
    private Space space;

    @OneToMany(mappedBy = "reservation")
    private List<SpaceServiceItem> services;
}
