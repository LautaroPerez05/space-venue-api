package com.utn.space.venueaapi.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@AllArgsConstructor
@Data
@Entity
@Table(name = "spaceimages")
@NoArgsConstructor
public class SpaceImage {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id_space_images;

    @ManyToOne
    @JoinColumn(name = "idSpace")
    private Space space;

    private String file_name;
    private String url_image;
    private LocalDateTime date_send;
}
