package com.utn.space.venueaapi.model;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.*;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import java.util.*;

//@Data <---- Se elimina por bucle entre lombok y Spring Security
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "credentials")
@Entity
public class Credential implements UserDetails {
    @Id
    @NotBlank
    @Schema(description = "UserName único del Usuario", example = "Carlos25")
    private String username;

    @Column(name = "isActive", nullable = false)
    @NotBlank
    private Boolean isActive = true;

    @Column(name = "passwordHash")
    @NotBlank
    @Schema(description = "Contraceña del Usuario", example = "Carlos2000")
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