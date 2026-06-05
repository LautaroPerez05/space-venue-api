package com.utn.space.venueaapi.service;

import com.utn.space.venueaapi.exceptions.ExceptionIdNotFound;
import com.utn.space.venueaapi.exceptions.ExceptionInvalidDate;
import com.utn.space.venueaapi.model.Reservation;
import com.utn.space.venueaapi.model.SpaceServiceItem;
import com.utn.space.venueaapi.model.records.ReservationDTO;
import com.utn.space.venueaapi.repository.ConsumerRepository;
import com.utn.space.venueaapi.repository.SpaceRepository;
import com.utn.space.venueaapi.service.mappers.ReservationMapper;
import com.utn.space.venueaapi.repository.ReservationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class ReservationService {
    @Autowired
    private ReservationRepository reservationRepository;
    @Autowired
    private ReservationMapper reservationMapper;

    @Autowired
    private ConsumerRepository consumerRepository;
    @Autowired
    private SpaceRepository spaceRepository;

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
        // no cheque los id de ServiceItem
        Reservation aux= reservationMapper.toEntity(dto);
        aux.setCreatedAt(LocalDateTime.now());

        aux.setConsumer(consumerRepository.findById(dto.getId_consumer())
                .orElseThrow(()->new ExceptionIdNotFound("Consumer", dto.getId_consumer())));

        aux.setSpace(spaceRepository.findById(dto.getId_space())
                .orElseThrow(()->new ExceptionIdNotFound("Space",dto.getId_space())));

        List<SpaceServiceItem> list= new ArrayList<>();
       /*for(Long i =0; i<dto.getId_servicesSelec().size();i++){
            list.add(aux.getSpace().getItemService().stream().
                    filter(item->item.getId().equals(dto.getId_servicesSelec().get(i))).toList()
        }
        aux.getSpace().getItemService().stream().filter(item->item.getId().).toList();*/
        list= aux.getSpace().getItemService().stream()
                .filter(item->dto.getId_servicesSelec().contains(item.getId()))
                .toList();
        aux.setServices(list);
        aux.setFinalPrice(aux.getSpace().getBase_price() +
                aux.getServices().stream()
                .mapToDouble(SpaceServiceItem::getPrice)
                .sum());
        return reservationRepository.save(aux);
    }
}
