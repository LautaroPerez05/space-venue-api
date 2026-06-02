package com.utn.space.venueaapi.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;

@AllArgsConstructor
@Data
@Table(name = "Locations")
@Entity
public class Location {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id_location;

    private String street_name;
    private Long street_number;
    private String municipality;
    private Long zip_code;
    private String city;
    private Double longitude;
    private Double latitude;
}
