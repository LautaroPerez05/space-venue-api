package com.utn.space.venueaapi.service;

import com.utn.space.venueaapi.model.Credential;
import com.utn.space.venueaapi.repository.CredentialRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CredentialService {

    private final CredentialRepository credentialRepository;
    private final PasswordEncoder passwordEncoder;


    @Autowired
    public CredentialService(CredentialRepository credentialRepository, PasswordEncoder passwordEncoder) {
        this.credentialRepository = credentialRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public boolean existsByUsername(String username) {
        return credentialRepository.existsByUsername(username);
    }

    /**
     * Registra o actualiza una credencial aplicando algoritmos de hashing delegados
     */
    public void saveCredential(Credential credential) {
        if (credential.getPasswordHash() == null || credential.getPasswordHash().isEmpty()) {
            throw new IllegalArgumentException("La contraseña de la credencial no puede ser nula o vacía.");
        }

        // 1. Interceptamos la clave en texto plano y la encriptamos de forma segura
        String securedPassword = passwordEncoder.encode(credential.getPasswordHash());
        credential.setPasswordHash(securedPassword);

        // 2. Aseguramos que la credencial nazca activa para que no falle el login
        credential.setIsActive(true);

        // 3. Guardamos en la base de datos
        credentialRepository.save(credential);
    }

    // Este metodo debería modificarse para que devuelva un DTO (con datos de credenciales, sin contraseñas, y consumers)
    public List<Credential> findAll() {
        return credentialRepository.findAll();
    }
}
