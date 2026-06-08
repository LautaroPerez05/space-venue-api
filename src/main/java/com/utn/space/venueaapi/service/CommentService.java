package com.utn.space.venueaapi.service;

import com.utn.space.venueaapi.exceptions.ExceptionIdNotFound;
import com.utn.space.venueaapi.exceptions.InvalidDataException;
import com.utn.space.venueaapi.model.Comment;
import com.utn.space.venueaapi.model.records.CommentDTO;
import com.utn.space.venueaapi.repository.CommentRepository;
import com.utn.space.venueaapi.repository.ConsumerRepository;
import com.utn.space.venueaapi.repository.SpaceRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CommentService {
    @Autowired
    CommentRepository commentRepository;
    @Autowired
    ConsumerRepository consumerRepository;
    @Autowired
    SpaceRepository spaceRepository;

    public List<Comment> findAll(){
        return commentRepository.findAll();
    }

    public Comment findById(Integer id){
        return commentRepository.findById(id).orElseThrow(()-> new ExceptionIdNotFound("Comment", id));
    }

    public void deleteById(Integer id){
        if(!commentRepository.existsById(id)){
            throw new ExceptionIdNotFound("Comment", id);
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
                consumerRepository.findById(commentDTO.id_consumer()).orElseThrow(()-> new ExceptionIdNotFound("Consummer", commentDTO.id_consumer())),
                spaceRepository.findById(commentDTO.id_space()).orElseThrow(()-> new ExceptionIdNotFound("Space", commentDTO.id_space())),
                commentDTO.description(),
                commentDTO.score(),
                commentDTO.created_at());

        commentRepository.save(commentToInsert);
    }


    public void modifyComment(Integer id, CommentDTO commentDTO){
        if(!commentRepository.existsById(id)){
            throw new ExceptionIdNotFound("Comment", id);
        }

        if(commentDTO.description().isBlank()){
            throw new InvalidDataException("Por favor ingrese una descripcion para su comentario");
        }

        if((commentDTO.score()*2) % 1 != 0){
            throw new InvalidDataException("El score ingresado es invalido");
        }

        Comment commentToInsert = new Comment(
                id,
                consumerRepository.findById(commentDTO.id_consumer()).orElseThrow(()-> new ExceptionIdNotFound("Consummer", commentDTO.id_consumer())),
                spaceRepository.findById(commentDTO.id_space()).orElseThrow(()-> new ExceptionIdNotFound("Space", commentDTO.id_space())),
                commentDTO.description(),
                commentDTO.score(),
                commentDTO.created_at());

        commentRepository.save(commentToInsert);
    }

    public List<Comment> findAllBySpaceId(Integer spaceId){
        if(!spaceRepository.existsById(spaceId)){
            throw new ExceptionIdNotFound("Space", spaceId);
        }
        return commentRepository.findAllBySpaceIdSpace(spaceId);
    }

    public List<Comment> findAllByConsumerId(Integer consumerId){
        if(!consumerRepository.existsById(consumerId)){
            throw new ExceptionIdNotFound("Consumer", consumerId);
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