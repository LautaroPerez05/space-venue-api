package com.utn.space.venueaapi.service;

import com.utn.space.venueaapi.exceptions.IdNotFoundException;
import com.utn.space.venueaapi.exceptions.InvalidDataException;
import com.utn.space.venueaapi.model.Comment;
import com.utn.space.venueaapi.model.Reservation;
import com.utn.space.venueaapi.model.ReservationStatus;
import com.utn.space.venueaapi.model.records.CommentDTO;
import com.utn.space.venueaapi.repository.CommentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CommentService {
    @Autowired
    CommentRepository commentRepository;
    @Autowired
    ConsumerService consumerService;
    @Autowired
    SpaceService spaceService;
    @Autowired
    ReservationService reservationService;

    public List<Comment> findAll(){
        return commentRepository.findAll();
    }

    public Comment findById(Integer id){
        return commentRepository.findById(id).orElseThrow(()-> new IdNotFoundException("No se encontro el comentario buscado: ", id));
    }

    public void deleteById(Integer id){
        if(!commentRepository.existsById(id)){
            throw new IdNotFoundException("No se encontro el comentario a eliminar: ", id);
        }
        commentRepository.deleteById(id);
    }

    public void deleteByIdCustomer(Integer id){
        Comment commentToDelete = commentRepository.findById(id).orElseThrow(()-> new IdNotFoundException("No se encontro el comentario a eliminar: ", id));
        //Logica para que un usuario solo elimine comentarios propios
        if(!commentToDelete.getConsumer().getIdConsumer().equals(consumerService.getLoggedConsumerId())){
            throw new InvalidDataException("No se puede elimnar un comentario que no es propio");
        }
        commentRepository.deleteById(id);
    }

    public void insertComment(CommentDTO commentDTO){
        if(commentDTO.description().isBlank()){
            throw new InvalidDataException("Por favor ingrese una descripcion para su comentario");
        }

        if((commentDTO.score()*2) % 1 != 0){
            throw new InvalidDataException("El score ingresado es invalido");
        }

        Integer currentUserId = consumerService.getLoggedConsumerId(); //Comentamos siempre con el id de quien esta loggeado

        Comment commentToInsert = new Comment(
                null,
                consumerService.findById(currentUserId),
                spaceService.findById(commentDTO.idSpace()),
                commentDTO.description(),
                commentDTO.score(),
                commentDTO.created_at());

        commentRepository.save(commentToInsert);
    }


    public void modifyComment(Integer id, CommentDTO commentDTO){
        if(!commentRepository.existsById(id)){
            throw new IdNotFoundException("No se encontro el comentario a eliminar: ", id);
        }

        if(commentDTO.description().isBlank()){
            throw new InvalidDataException("Por favor ingrese una descripcion para su comentario");
        }

        if((commentDTO.score()*2) % 1 != 0){
            throw new InvalidDataException("El score ingresado es invalido");
        }

        Integer currentUserId = consumerService.getLoggedConsumerId(); //Comentamos siempre con el id de quien esta loggeado

        Comment commentToInsert = new Comment(
                id,
                consumerService.findById(currentUserId),
                spaceService.findById(commentDTO.idSpace()),
                commentDTO.description(),
                commentDTO.score(),
                commentDTO.created_at());

        commentRepository.save(commentToInsert);
    }

    public List<Comment> findAllBySpaceId(Integer spaceId){
        if(!spaceService.existsById(spaceId)){
            throw new IdNotFoundException("No se encontro el espacio del cual se quieren buscar comentarios: ", spaceId);
        }
        return commentRepository.findAllBySpaceIdSpace(spaceId);
    }

    public List<Comment> findAllByConsumerId(Integer consumerId){
        if(!consumerService.existsById(consumerId)){
            throw new IdNotFoundException("No se encontro el consumidor del cual se quieren buscar comentarios: ", consumerId);
        }
        return commentRepository.findAllByConsumerIdConsumer(consumerId);
    }
    public List<Comment> filterByScoreASC(){
        return commentRepository.findAllByOrderByScoreAsc();
    }

    public List<Comment> filterByScoreDESC(){
        return commentRepository.findAllByOrderByScoreDesc();
    }

    public void consumerInsertCommentOnSpace(CommentDTO commentDTO){
        //Averiguamos las reservas del consumer loggeado
        List<Reservation> reservationsForConsumer = reservationService.findByIdConsumer(consumerService.getLoggedConsumerId());
        //Filtramos para que queden solo reservas activas y completas
        reservationsForConsumer = reservationsForConsumer.stream()
                .filter(reservation -> reservation.getIsActive() && reservation.getStatus().equals(ReservationStatus.COMPLETED)).toList();

        //Verificamos que alguna de las reservas fue sobre el espacio que queremos comentar
        if(!reservationsForConsumer.stream().anyMatch(reservation -> reservation.getSpace().getIdSpace().equals(commentDTO.idSpace()))){
            throw new InvalidDataException("No puede comentar sobre un espacio que nunca reservo o cuya reserva no completo");
        }

        //Finalmente insertamos el comentario
        insertComment(commentDTO);
    }

    public void consumerModifyCommentOnSpace(Integer id,CommentDTO commentDTO){
        if(!commentDTO.idConsumer().equals(consumerService.getLoggedConsumerId())){
            throw new InvalidDataException("No se puede modificar un comentario que no es propio");
        }

        modifyComment(id,commentDTO);
    }
}