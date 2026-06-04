package com.utn.space.venueaapi.service;

import com.utn.space.venueaapi.exceptions.InvalidDataException;
import com.utn.space.venueaapi.exceptions.NotFoundException;
import com.utn.space.venueaapi.model.Comment;
import com.utn.space.venueaapi.model.records.CommentDTO;
import com.utn.space.venueaapi.repository.CommentRepository;
import com.utn.space.venueaapi.repository.ConsumerRepository;
import com.utn.space.venueaapi.repository.SpaceRepository;
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
    ConsumerRepository consumerRepository;
    @Autowired
    SpaceRepository spaceRepository;

    public List<Comment> findAll(){
        return commentRepository.findAll();
    }

    public Comment findById(Long id){
        return commentRepository.findById(id).orElseThrow(()-> new NotFoundException("No se encontro el comentario buscado"));
    }

    public void deleteById(Long id){
        if(!commentRepository.existsById(id)){
            throw new NotFoundException("No se encontro el espacio a eliminar");
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
                consumerRepository.findById(commentDTO.id_consumer()).orElseThrow(()-> new NotFoundException("No se encontro el consumidor asocidado al comentario")),
                spaceRepository.findById(commentDTO.id_space()).orElseThrow(()-> new NotFoundException("No se encontro el espacio asociado al coemntario")),
                commentDTO.description(),
                commentDTO.score(),
                commentDTO.created_at());

        commentRepository.save(commentToInsert);
    }


    public void modifyComment(Long id, CommentDTO commentDTO){
        if(!commentRepository.existsById(id)){
            throw new NotFoundException("No se encontro el comentario a eliminar");
        }

        if(commentDTO.description().isBlank()){
            throw new InvalidDataException("Por favor ingrese una descripcion para su comentario");
        }

        if(commentDTO.score() < 0){ //Aca podriamos validar el tope de calificaciones cuando lo decidamos
            throw new InvalidDataException("Un comentario no puede tener una calificacion negativa");
        }

        Comment commentToInsert = new Comment(
                id,
                consumerRepository.findById(commentDTO.id_consumer()).orElseThrow(()-> new NotFoundException("No se encontro el consumidor asocidado al comentario")),
                spaceRepository.findById(commentDTO.id_space()).orElseThrow(()-> new NotFoundException("No se encontro el espacio asociado al coemntario")),
                commentDTO.description(),
                commentDTO.score(),
                commentDTO.created_at());

        commentRepository.save(commentToInsert);
    }

    public List<Comment> findAllBySpaceId(Long spaceId){
        if(!spaceRepository.existsById(spaceId)){
            throw new NotFoundException("No se encontro el espacio del cual se quieren buscar comentarios");
        }
        return commentRepository.findAllBySpaceIdSpace(spaceId);
    }

    public List<Comment> findAllByConsumerId(Long consumerId){
        if(!consumerRepository.existsById(consumerId)){
            throw new NotFoundException("No se encontro el consumidor del cual se quieren buscar comentarios");
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