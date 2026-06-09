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
    @Column(name = "id_location")
    private Integer idLocation;
    @Column(name = "street_name")
    private String streetName;
    @Column(name = "street_number")
    private String streetNumber;
    private String municipality;
    @Column(name = "zip_code")
    private String zipCode;
    private String city;
    private BigDecimal longitude;
    private BigDecimal latitude;
}
