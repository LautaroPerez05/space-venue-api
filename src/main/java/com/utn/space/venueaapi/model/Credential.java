package com.utn.space.venueaapi.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "credentials")
@Entity
public class Credential {
    @Id
    private String username;

    @Column(name = "isActive", nullable = false)
    private Boolean isActive = true;

    @Column(name = "passwordHash")
    private String passwordHash;

    @Enumerated(EnumType.STRING)
    private ERoles rol = ERoles.ROLE_CLIENT;

}