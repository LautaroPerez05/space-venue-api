package com.utn.space.venueaapi.service;

import com.utn.space.venueaapi.exceptions.ExceptionIdNotFound;
import com.utn.space.venueaapi.exceptions.ExceptionNameNotFound;
import com.utn.space.venueaapi.model.Consumer;
import com.utn.space.venueaapi.repository.ConsumerRepository;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@AllArgsConstructor
@Service
public class ConsumerService {
    @Autowired
    private final ConsumerRepository consumerRepository;

    public Boolean existsById(Integer id){
        return consumerRepository.existsById(id);
    }

    public Consumer findById(Integer id){
        return consumerRepository.findById(id).orElseThrow(()-> new ExceptionIdNotFound("Consumer",id));
    }

    public Consumer findByCredentialsUsername(String username){
        return consumerRepository.findByUsername(username).orElseThrow(() -> new ExceptionNameNotFound("No se ha encontrado al usuario buscado por username"));
    }
}
