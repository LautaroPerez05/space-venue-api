package com.utn.space.venueaapi.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDate;

@AllArgsConstructor
@Data
@Entity
@Table(name = "SpaceImages")
public class SpaceImage {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id_space_images;

    @ManyToOne
    @JoinColumn(name = "id_space")
    private Space space;

    private String file_name;
    private String url_image;
    private LocalDate date_sent;
}
