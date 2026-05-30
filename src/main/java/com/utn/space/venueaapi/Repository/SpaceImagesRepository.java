package com.utn.space.venueaapi.Repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SpaceImagesRepository extends JpaRepository<SpaceImagesRepository,Long> {
}
