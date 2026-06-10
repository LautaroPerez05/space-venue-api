package com.utn.space.venueaapi.service;

import com.utn.space.venueaapi.exceptions.IdNotFoundException;
import com.utn.space.venueaapi.model.Notification;
import com.utn.space.venueaapi.repository.NotificationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class NotificationService {
    @Autowired
    private NotificationRepository notificationRepository;

    /// ---------------------------------Metodos------------------------------------------

    public List<Notification> listAll(){
        return notificationRepository.findAll();
    }

    public Notification findById(Integer id){
        return notificationRepository.findById(id).orElseThrow(()->new IdNotFoundException("Notificacion",id));
    }

    public Notification markAsSeen (Integer id){
        Notification aux= notificationRepository.findById(id).orElseThrow(()->new IdNotFoundException("Notificacion",id));
        aux.setIsSeen(true);
        return notificationRepository.save(aux);
    }

    public List<Notification> listAllByIdConsumer (Integer id){
        return notificationRepository.findAllByIdConsumer(id);
    }
}
