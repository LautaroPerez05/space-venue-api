package com.utn.space.venueaapi.service;

import com.utn.space.venueaapi.exceptions.IdNotFoundException;
import com.utn.space.venueaapi.exceptions.NameNotFoundException;
import com.utn.space.venueaapi.model.Consumer;
import com.utn.space.venueaapi.model.Credential;
import com.utn.space.venueaapi.model.records.ConsumerFilterDTO;
import com.utn.space.venueaapi.repository.ConsumerRepository;
import jdk.dynalink.linker.LinkerServices;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;

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
    public Consumer findByUsername(String username){
        return consumerRepository.findByUsername(username).orElseThrow(() -> new NameNotFoundException("No hay usuarios con ese username"));
    }

    public Consumer findByCredentialsUsername(String username){
        return consumerRepository.findByUsername(username).orElseThrow(() -> new NameNotFoundException("No se ha encontrado al usuario buscado por username"));
    }

    public List<Consumer> findAllByfields(ConsumerFilterDTO consumerFilterDTO){
        return consumerRepository.findAllByFilters(
                consumerFilterDTO.firstname(),
                consumerFilterDTO.lastname(),
                consumerFilterDTO.email(),
                consumerFilterDTO.phone());
    }
    public Boolean existByEmail(String email){
        return consumerRepository.existsByEmail(email);
    }
    public Boolean existsByPhone(String phone){
        return consumerRepository.existsByPhone(phone);
    }

    public void deleteById(Integer id){
        if(!existsById(id)){
            throw new IdNotFoundException("Consumer",id);
        }
        consumerRepository.deleteById(id);
    }
    public void updateUser(Consumer consumer) throws IdNotFoundException{
        if(consumerRepository.existsById(consumer.getIdConsumer())){
            consumerRepository.save(consumer);
        }
        else {
            throw new IdNotFoundException("Consumer", consumer.getIdConsumer());
        }
    }

    //Quizas esto quieran ponerlo en security utils
    public Integer getLoggedConsumerId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        if (auth == null || !auth.isAuthenticated() || auth instanceof AnonymousAuthenticationToken) {
            throw new IllegalStateException("No hay un usuario autenticado");
        }

        String username = auth.getName(); // normalmente debería ser el username (credential.username)
        Consumer consumer = findByCredentialsUsername(username);
        return consumer.getIdConsumer();
    }
    public void deleteUserLogically(String username) {
        // 1. Buscas al consumidor por su username
        Consumer consumer = consumerRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
        // 2. Obtienes la entidad Credential asociada
        Credential credential = consumer.getCredentials();
        if (credential == null) {
            throw new RuntimeException("No se encontraron credenciales para este usuario");
        }
        // 3. Cambias el atributo de baja lógica
        credential.setIsActive(false);
        // 4. Guardas el cambio (al guardar el consumer, JPA actualiza la relación gracias al ciclo de vida)
        consumerRepository.save(consumer);
    }
}
