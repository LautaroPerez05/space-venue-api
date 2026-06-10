package com.utn.space.venueaapi.service;

import com.utn.space.venueaapi.exceptions.IdNotFoundException;
import com.utn.space.venueaapi.exceptions.NameNotFoundException;
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
        return consumerRepository.findById(id).orElseThrow(()-> new IdNotFoundException("Consumer",id));
    }

    public Consumer findByCredentialsUsername(String username){
        return consumerRepository.findByUsername(username).orElseThrow(() -> new NameNotFoundException("No se ha encontrado al usuario buscado por username"));
    }
}
