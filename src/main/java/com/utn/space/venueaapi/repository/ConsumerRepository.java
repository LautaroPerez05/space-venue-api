package com.utn.space.venueaapi.repository;

import com.utn.space.venueaapi.model.Consumer;
import jakarta.validation.constraints.NotBlank;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ConsumerRepository extends JpaRepository<Consumer,Integer> {
    @Query("SELECT c FROM Consumer c WHERE c.credentials.username = :username")
    Optional<Consumer> findByUsername(@Param("username") String username);
}
