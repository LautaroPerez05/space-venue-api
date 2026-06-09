package com.utn.space.venueaapi.service;

import com.utn.space.venueaapi.exceptions.ExceptionIdNotFound;
import com.utn.space.venueaapi.exceptions.InvalidDataException;
import com.utn.space.venueaapi.model.Comment;
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
                consumerService.findById(commentDTO.idConsumer()),
                spaceService.findById(commentDTO.idSpace()),
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
                consumerService.findById(commentDTO.idConsumer()),
                spaceService.findById(commentDTO.idSpace()),
                commentDTO.description(),
                commentDTO.score(),
                commentDTO.created_at());

        commentRepository.save(commentToInsert);
    }

    public List<Comment> findAllBySpaceId(Integer spaceId){
        if(!spaceService.existsById(spaceId)){
            throw new ExceptionIdNotFound("Space",spaceId);
        }
        return commentRepository.findAllBySpaceIdSpace(spaceId);
    }

    public List<Comment> findAllByConsumerId(Integer consumerId){
        if(!consumerService.existsById(consumerId)){
            throw new ExceptionIdNotFound("Consumer",consumerId);
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