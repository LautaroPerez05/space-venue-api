package com.utn.space.venueaapi.service;

import com.utn.space.venueaapi.exceptions.ExceptionIdNotFound;
import com.utn.space.venueaapi.exceptions.InvalidDataException;
import com.utn.space.venueaapi.model.Comment;
import com.utn.space.venueaapi.model.records.CommentDTO;
import com.utn.space.venueaapi.repository.CommentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class CommentService {
    @Autowired
    CommentRepository commentRepository;
    @Autowired
    ConsumerService consumerService;
    @Autowired
    SpaceService spaceService;

    public List<Comment> findAll(){
        return commentRepository.findAll();
    }

    public Comment findById(Integer id){
        return commentRepository.findById(id).orElseThrow(()-> new ExceptionIdNotFound("No se encontro el comentario buscado: ", id));
    }

    public void deleteById(Integer id){
        if(!commentRepository.existsById(id)){
            throw new ExceptionIdNotFound("No se encontro el espacio a eliminar: ", id);
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

        Comment commentToInsert = new Comment(
                null,
                consumerService.findById(commentDTO.id_consumer()),
                spaceService.findById(commentDTO.id_space()),
                commentDTO.description(),
                commentDTO.score(),
                commentDTO.created_at());

        commentRepository.save(commentToInsert);
    }


    public void modifyComment(Integer id, CommentDTO commentDTO){
        if(!commentRepository.existsById(id)){
            throw new ExceptionIdNotFound("No se encontro el comentario a eliminar: ", id);
        }

        if(commentDTO.description().isBlank()){
            throw new InvalidDataException("Por favor ingrese una descripcion para su comentario");
        }

        if((commentDTO.score()*2) % 1 != 0){
            throw new InvalidDataException("El score ingresado es invalido");
        }

        Comment commentToInsert = new Comment(
                id,
                consumerService.findById(commentDTO.id_consumer()),
                spaceService.findById(commentDTO.id_space()),
                commentDTO.description(),
                commentDTO.score(),
                commentDTO.created_at());

        commentRepository.save(commentToInsert);
    }

    public List<Comment> findAllBySpaceId(Integer spaceId){
        if(!spaceService.existsById(spaceId)){
            throw new ExceptionIdNotFound("No se encontro el espacio del cual se quieren buscar comentarios: ", spaceId);
        }
        return commentRepository.findAllBySpaceIdSpace(spaceId);
    }

    public List<Comment> findAllByConsumerId(Integer consumerId){
        if(!consumerService.existsById(consumerId)){
            throw new ExceptionIdNotFound("No se encontro el consumidor del cual se quieren buscar comentarios: ", consumerId);
        }
        return commentRepository.findAllByConsumerIdConsumer(consumerId);
    }
    public List<Comment> filterByScoreASC(){
        return commentRepository.findAllByOrderByScoreAsc();
    }

    public List<Comment> filterByScoreDESC(){
        return commentRepository.findAllByOrderByScoreDesc();
    }
}