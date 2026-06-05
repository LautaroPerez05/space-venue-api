package com.utn.space.venueaapi.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@AllArgsConstructor
@Data
@Table(name = "locations")
@Entity
@NoArgsConstructor
public class Location {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id_location;

    private String street_name;
    private String street_number;
    private String municipality;
    private String zip_code;
    private String city;
    private BigDecimal longitude;
    private BigDecimal latitude;
}
