package com.utn.space.venueaapi.service;

import com.utn.space.venueaapi.exceptions.IdNotFoundException;
import com.utn.space.venueaapi.model.CancellationPolicy;
import com.utn.space.venueaapi.repository.CancellationPolicyRepository;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
@AllArgsConstructor
@Service
public class CancellationPolicyService {
    @Autowired
    CancellationPolicyRepository cancellationPolicyRepository;

    public CancellationPolicy findById(Integer id){
        return cancellationPolicyRepository.findById(id).orElseThrow(()->new IdNotFoundException("No se encontraron las politicas de cancelacion: ", id));
    }
}
