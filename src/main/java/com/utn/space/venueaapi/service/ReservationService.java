package com.utn.space.venueaapi.service;

import com.utn.space.venueaapi.exceptions.ExceptionIdNotFound;
import com.utn.space.venueaapi.exceptions.ExceptionInvalidDate;
import com.utn.space.venueaapi.model.Reservation;
import com.utn.space.venueaapi.model.records.ReservationDTO;
import com.utn.space.venueaapi.model.records.ReservationMapper;
import com.utn.space.venueaapi.repository.ReservationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class ReservationService {
    @Autowired
    private ReservationRepository reservationRepository;
    private ReservationMapper reservationMapper;

    /// -------------------Metodos-------------------------------------------

    public List<Reservation> findAll (){
        return reservationRepository.findAll();
    }

    public Reservation findById (Long id){
        return reservationRepository.findById(id).orElseThrow(()-> new ExceptionIdNotFound("Reservacion",id));
    }

    public Reservation create (ReservationDTO dto) {
        if (dto.getUntilDate().isBefore(dto.getFromDate())) {
            throw new ExceptionInvalidDate("La Fecha Final no puede ser antes que la Fecha de Inicio");
        }
        if (dto.getFromDate().isBefore(LocalDateTime.now())) {
            throw new ExceptionInvalidDate("La Fecha Final no puede ser antes que la Fecha de Inicio");
        }

        ///validar las id?
///desta

        Reservation aux= reservationMapper.toEntity(dto);

//agregar los id

        return reservationRepository.save(aux);
    }
}
