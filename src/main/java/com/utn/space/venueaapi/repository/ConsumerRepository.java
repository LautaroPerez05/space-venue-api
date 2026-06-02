package com.utn.space.venueaapi.repository;

import com.utn.space.venueaapi.model.Consumer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ConsumerRepository extends JpaRepository<Consumer,Long> {
}
