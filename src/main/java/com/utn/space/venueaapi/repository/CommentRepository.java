package com.utn.space.venueaapi.repository;

import com.utn.space.venueaapi.model.Comment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository

public interface CommentRepository extends JpaRepository<Comment,Long> {
    List<Comment> findAllBySpaceIdSpace(Long id);
    List<Comment> findAllByConsumerIdConsumer(Long id);
}