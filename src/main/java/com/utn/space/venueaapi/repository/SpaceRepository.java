package com.utn.space.venueaapi.repository;

import com.utn.space.venueaapi.model.Space;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SpaceRepository extends JpaRepository<Space,Long> {
}
