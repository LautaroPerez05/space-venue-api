package com.utn.space.venueaapi.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDate;

@AllArgsConstructor
@Data
@Entity
@Table(name = "Comments")
public class Comment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id_comment;

    @ManyToOne
    @JoinColumn(name = "id_consumer")
    private Consumer consumer;

    @ManyToOne
    @JoinColumn(name = "id_space")
    private Space space;

    private String description;
    private Double score;
    private LocalDate created_at;
}