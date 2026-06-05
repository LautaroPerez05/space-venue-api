package com.utn.space.venueaapi.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@AllArgsConstructor
@Data
@Entity
@Table(name = "comments")
@NoArgsConstructor
public class Comment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id_comment;

    @ManyToOne
    @JoinColumn(name = "id_consumer")
    private Consumer consumer;

    @ManyToOne
    @JoinColumn(name = "id_space")
    private Space space;

    private String description;

    @Column(name = "score", nullable = false)
    private Byte score;

    private LocalDateTime created_at;
}