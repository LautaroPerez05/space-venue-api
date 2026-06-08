package com.utn.space.venueaapi.service;

import com.utn.space.venueaapi.exceptions.ExceptionNameNotFound;
import com.utn.space.venueaapi.model.Consumer;
import com.utn.space.venueaapi.repository.ConsumerRepository;
import org.springframework.stereotype.Service;

@Service
public class ConsumerService {
    private final ConsumerRepository consumerRepository;

    public ConsumerService(ConsumerRepository consumerRepository) {
        this.consumerRepository = consumerRepository;
    }

    public Consumer findByCredentialsUsername(String username){
        return consumerRepository.findByUsername(username).orElseThrow(() -> new ExceptionNameNotFound("No se ha encontrado al usuario buscado por username"));
    }
}
