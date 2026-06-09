package com.utn.space.venueaapi.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import java.util.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "credentials")
@Entity
public class Credential implements UserDetails {
    @Id
    private String username;


    @Column(name = "isActive", nullable = false)
    private Boolean isActive = true;

    @Column(name = "passwordHash")
    private String passwordHash;

    @Enumerated(EnumType.STRING)
    private ERoles rol = ERoles.ROLE_CLIENT;

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority(rol.name()));
    }

    // 3. Devuelve tu campo de contraseña
    @Override
    public String getPassword() {
        return this.passwordHash;
    }

    // 4. Devuelve tu campo de usuario
    @Override
    public String getUsername() {
        return this.username;
    }

    // 5. Métodos de estado de cuenta (puedes usar tu bandera isActive)
    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return this.isActive;
    }

}