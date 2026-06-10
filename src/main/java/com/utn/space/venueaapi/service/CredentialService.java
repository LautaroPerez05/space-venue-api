package com.utn.space.venueaapi.service;

import com.utn.space.venueaapi.model.Credential;
import com.utn.space.venueaapi.repository.CredentialRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class CredentialService {

    @Autowired
    private final CredentialRepository credentialRepository;


    public CredentialService(CredentialRepository credentialRepository) {
        this.credentialRepository = credentialRepository;
    }

    public boolean existsByUsername(String username) {
        return credentialRepository.existsByUsername(username);
    }

    public void saveCredential(Credential credential) {
        credentialRepository.save(credential);
    }
}
