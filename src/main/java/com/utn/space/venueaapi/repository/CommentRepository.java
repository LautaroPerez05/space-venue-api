package com.utn.space.venueaapi.repository;

import com.utn.space.venueaapi.model.Comment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository

public interface CommentRepository extends JpaRepository<Comment,Integer> {

    // Se utiliza la anotación Query porque en las convenciones de Spring Data JPA
    // el carácter de guion bajo (_) no se interpreta como un carácter de texto plano
    @Query("SELECT c FROM Comment c WHERE c.space.id_space = :idSpace")
    List<Comment> findAllBySpaceIdSpace(@Param("idSpace") Integer idSpace);

    @Query("SELECT c FROM Comment c WHERE c.consumer.id_consumer = :idConsumer")
    List<Comment> findAllByConsumerIdConsumer(@Param("idConsumer") Integer idConsumer);

    List<Comment> findAllByOrderByScoreAsc();
    List<Comment> findAllByOrderByScoreDesc();

}