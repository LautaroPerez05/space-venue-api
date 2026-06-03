package com.utn.space.venueaapi.repository;

import com.utn.space.venueaapi.model.SpaceServiceItem;
import com.utn.space.venueaapi.model.records.SpaceServiceItemDTO;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SpaceServiceItemRepository extends JpaRepository<SpaceServiceItem,Long> {
    @Query("SELECT new SpaceServiceDTO(ss.id, ss.description, ss.price, sp.id) " +
            "FROM SpaceService ss " +
            "JOIN ss.space sp " +
            "WHERE sp.id = :idSpace")
    List<SpaceServiceItemDTO> findAllSpaceServicesBySpaceId(@Param("idSpace") Long idSpace);

    @Query("SELECT CASE WHEN EXISTS (" +
                "SELECT s " +
                "FROM SpaceServiceItem s JOIN s.space sp " +
                "WHERE s.id = :idItem AND sp.id = :idSpace) " +
            "THEN true ELSE false END")
    boolean existsServiceItemInSpace(Long idItem, Long idSpace);
}
