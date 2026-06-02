package com.utn.space.venueaapi.repository;

import com.utn.space.venueaapi.model.SpaceImage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository

public interface SpaceImageRepository extends JpaRepository<SpaceImage,Long> {
    List<SpaceImage> findAllBySpaceIdSpace(Long id);
}
