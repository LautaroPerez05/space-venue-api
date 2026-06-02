package com.utn.space.venueaapi.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Data
@AllArgsConstructor @NoArgsConstructor
public class Reservation {
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

}
