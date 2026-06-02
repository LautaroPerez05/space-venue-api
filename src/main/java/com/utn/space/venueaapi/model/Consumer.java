package com.utn.space.venueaapi.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.utn.space.venueaapi.model.*;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.*;
@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
public class Consumer {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id_consumer;

    private String firstname;
    private String lastname;

    @Email
    private String email;

    private String phone;
    private boolean isActive = true;

    @OneToOne
    @JoinColumn(name = "username")
    private Credential credentials;

    @OneToMany(mappedBy = "consumers")
    @JsonIgnore
    private List<Notification> notificationsList;

    @OneToMany(mappedBy = "consumerOwner")
    @JsonIgnore
    private List<Space> spacesList;

    @OneToMany(mappedBy = "consumers")
    @JsonIgnore
    private List<Comment> commentsList;

    @OneToMany(mappedBy = "consumers")
    @JsonIgnore
    private List<Reservation> reservationsList;
}