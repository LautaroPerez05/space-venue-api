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
@Table(name = "consumers")
@Entity
public class Consumer {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id_consumer;

    private String firstname;
    private String lastname;

    @Email
    private String email;

    private String phone;

    @OneToOne
    @JoinColumn(name = "username")
    private Credential credentials;

    @OneToMany(mappedBy = "consumer")
    @JsonIgnore
    private List<Notification> notificationsList;

    @OneToMany(mappedBy = "consumer_owner")
    @JsonIgnore
    private List<Space> spacesList;

    @OneToMany(mappedBy = "consumer")
    @JsonIgnore
    private List<Comment> commentsList;

    @OneToMany(mappedBy = "consumer")
    @JsonIgnore
    private List<Reservation> reservationsList;
}