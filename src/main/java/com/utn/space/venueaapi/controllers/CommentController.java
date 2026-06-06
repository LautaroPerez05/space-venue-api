package com.utn.space.venueaapi.controllers;

import com.utn.space.venueaapi.model.Comment;
import com.utn.space.venueaapi.model.records.CommentDTO;
import com.utn.space.venueaapi.service.CommentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/s&v/comments")
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
    public void deleteCommentById(@PathVariable Integer id){
        commentService.deleteById(id);
    }

    @PostMapping()
    public void insertComment(@RequestBody CommentDTO commentDTO){
        commentService.insertComment(commentDTO);
    }

    @PutMapping("/{id}")
    public void modifyComment(@PathVariable Integer id, @RequestBody CommentDTO commentDTO){
        commentService.modifyComment(id,commentDTO);
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