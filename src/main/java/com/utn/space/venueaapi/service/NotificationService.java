package com.utn.space.venueaapi.service;

import com.utn.space.venueaapi.exceptions.ExceptionIdNotFound;
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

    public Notification findById(Long id){
        return notificationRepository.findById(id).orElseThrow(()->new ExceptionIdNotFound("Notificacion",id));
    }

    public Notification markAsSeen (Long id){
        Notification aux= notificationRepository.findById(id).orElseThrow(()->new ExceptionIdNotFound("Notificacion",id));
        aux.set

    }
}
