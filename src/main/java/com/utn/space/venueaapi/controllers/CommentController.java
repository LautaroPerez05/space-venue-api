package com.utn.space.venueaapi.controllers;

import com.utn.space.venueaapi.model.Comment;
import com.utn.space.venueaapi.model.records.CommentDTO;
import com.utn.space.venueaapi.service.CommentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/comments")
public class CommentController {
    @Autowired
    CommentService commentService;

    @GetMapping
    public ResponseEntity<List<Comment>> listAllComments(){
        return ResponseEntity.ok(commentService.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Comment> findCommentById(@PathVariable Integer id){
        return ResponseEntity.ok(commentService.findById(id));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('CLIENT', 'ADMIN')")
    public void deleteCommentById(@PathVariable Integer id, Authentication authentication){
        //Authorities se deberia poder mandar desde el front con el JWT
        if (authentication.getAuthorities().stream().anyMatch(r -> r.getAuthority().equals("ROLE_ADMIN"))){
            //Logica si es un Admin
            commentService.deleteById(id);
        }else {
            //Logica si es un client
            commentService.deleteByIdCustomer(id);
        }
    }

    @PostMapping()
    @PreAuthorize("hasAnyRole('CLIENT', 'ADMIN')")
    public void insertComment(@RequestBody CommentDTO commentDTO, Authentication authentication){
        if (authentication.getAuthorities().stream().anyMatch(r -> r.getAuthority().equals("ROLE_ADMIN"))){
            //Logica si es un Admin
            commentService.insertComment(commentDTO);
        }else {
            //Logica si es un client
            commentService.consumerInsertCommentOnSpace(commentDTO); //Uso el nuevo metodo que verifica que se pueda hacer el comentario
        }
    }

    @PutMapping("/{id}")
    public void modifyComment(@PathVariable Integer id, @RequestBody CommentDTO commentDTO,Authentication authentication){
        if (authentication.getAuthorities().stream().anyMatch(r -> r.getAuthority().equals("ROLE_ADMIN"))){
            //Logica si es un Admin
            commentService.modifyComment(id,commentDTO);
        }else {
            //Logica si es un client
             commentService.consumerModifyCommentOnSpace(id,commentDTO);
        }
    }

    @GetMapping("/byspaceid/{id}")
    public ResponseEntity<List<Comment>> findAllBySpaceId(@PathVariable Integer id){
        return ResponseEntity.ok(commentService.findAllBySpaceId(id));
    }

    @GetMapping("/byconsumerid/{id}")
    public ResponseEntity<List<Comment>> findAllByConsumerId(@PathVariable Integer id){
        return ResponseEntity.ok(commentService.findAllByConsumerId(id));
    }
    @GetMapping("/byscore/asc")
    public ResponseEntity<List<Comment>> findAllByScoreASC(){
        return ResponseEntity.ok(commentService.filterByScoreASC());
    }

    @GetMapping("/byscore/desc")
    public ResponseEntity<List<Comment>> findAllByScoreDesc(){
        return ResponseEntity.ok(commentService.filterByScoreDESC());
    }
}