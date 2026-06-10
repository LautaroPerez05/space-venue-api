package com.utn.space.venueaapi.config;

import com.utn.space.venueaapi.model.Credential;
import com.utn.space.venueaapi.repository.CredentialRepository;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
//@Data <----- se elimina por conflicto entre lombok y Spring Security
public class CustomUserDetailsService implements UserDetailsService {
    private final CredentialRepository credentialsRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        // 1. Buscamos NUESTRO usuario en la base de datos usando el repositorio
        Credential currentUser = credentialsRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado: " + username));

        // 2. Traducimos NUESTRO usuario al "User" que entiende Spring Security
        return User.withUsername(currentUser.getUsername())
                // La contraseña ya debe estar encriptada en la base de datos con BCrypt
                .password(currentUser.getPasswordHash())
                // Le pasamos el rol que trajimos de la BD
                .authorities(currentUser.getAuthorities())
                .build();
    }

}
