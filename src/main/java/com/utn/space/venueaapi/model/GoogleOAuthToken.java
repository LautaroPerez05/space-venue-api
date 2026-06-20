package com.utn.space.venueaapi.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import java.time.LocalDateTime;

@Entity
@Table(name = "google_oauth_tokens")
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class GoogleOAuthToken implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    // Relación 1:1 con Consumer
    @OneToOne
    @JoinColumn(name = "id_consumer", referencedColumnName = "idConsumer", nullable = false, unique = true)
    private Consumer consumer;

    // Token de acceso (corta duración)
    @Column(columnDefinition = "LONGTEXT", nullable = false)
    private String accessToken;

    // Token de refresco (larga duración)
    @Column(columnDefinition = "LONGTEXT")
    private String refreshToken;

    // Email del usuario de Google autenticado
    @Column(nullable = false)
    private String googleEmail;

    // Timestamp de expiración del access token
    private LocalDateTime expiresAt;

    // Timestamp de creación
    @Column(nullable = false)
    private LocalDateTime createdAt;

    // Timestamp de última actualización
    private LocalDateTime updatedAt;

    // Flag para saber si el token aún es válido
    @Column(nullable = false)
    private Boolean isActive = true;
}

