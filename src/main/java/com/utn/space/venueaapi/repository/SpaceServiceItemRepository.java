package com.utn.space.venueaapi.repository;

import com.utn.space.venueaapi.model.SpaceServiceItem;
import com.utn.space.venueaapi.model.records.SpaceServiceItemDTO;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SpaceServiceItemRepository extends JpaRepository<SpaceServiceItem,Integer> {
    @Query("SELECT new com.utn.space.venueaapi.model.records.SpaceServiceItemDTO(ss.id, ss.description, ss.price, ss.isActive, sp.id_space) " +
            "FROM SpaceServiceItem ss " +
            "JOIN ss.space sp " +
            "WHERE sp.id_space = :idSpace")
    List<SpaceServiceItemDTO> findAllSpaceServicesBySpaceId(@Param("idSpace") Integer idSpace);

    @Query("SELECT CASE WHEN EXISTS (" +
                "SELECT s " +
                "FROM SpaceServiceItem s JOIN s.space sp " +
                "WHERE s.id = :idItem AND sp.id_space = :idSpace) " +
            "THEN true ELSE false END")
    boolean existsServiceItemInSpace(Integer idItem, Integer idSpace);
}
