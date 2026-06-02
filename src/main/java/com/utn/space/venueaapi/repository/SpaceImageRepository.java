package com.utn.space.venueaapi.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SpaceImageRepository extends JpaRepository<SpaceImageRepository,Long> {
}
