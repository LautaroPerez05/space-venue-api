package com.utn.space.venueaapi.repository;

import com.utn.space.venueaapi.model.SpaceService;
import com.utn.space.venueaapi.model.records.SpaceServiceDTO;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SpaceServiceRepository extends JpaRepository<SpaceService,Long> {
    @Query("SELECT new SpaceServiceDTO(ss.id, ss.description, ss.price, sp.id) " +
            "FROM SpaceService ss " +
            "JOIN ss.space sp " +
            "WHERE sp.id = :idSpace")
    List<SpaceServiceDTO> findAllSpaceServicesBySpaceId(@Param("idSpace") Long idSpace);
}
