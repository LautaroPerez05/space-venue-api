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
    public ResponseEntity<Comment> findCommentById(@PathVariable Long id){
        return ResponseEntity.ok(commentService.findById(id));
    }

    @DeleteMapping("/{id}")
    public void deleteSpaceById(@PathVariable Long id){
        commentService.deleteById(id);
    }

    @PostMapping()
    public void insertSpace(@RequestBody CommentDTO commentDTO){
        commentService.insertComment(commentDTO);
    }

    @PutMapping("/{id}")
    public void modifyComment(@PathVariable Long id, @RequestBody CommentDTO commentDTO){
        commentService.modifyComment(id,commentDTO);
    }

    @GetMapping("/byspaceid/{id}")
    public ResponseEntity<List<Comment>> findAllBySpaceId(@PathVariable Long id){
        return ResponseEntity.ok(commentService.findAllBySpaceId(id));
    }

    @GetMapping("/byconsumerid/{id}")
    public ResponseEntity<List<Comment>> findAllByConsumerId(@PathVariable Long id){
        return ResponseEntity.ok(commentService.findAllByConsumerId(id));
    }
}