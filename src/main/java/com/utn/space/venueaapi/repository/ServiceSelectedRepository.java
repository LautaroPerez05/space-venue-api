package com.utn.space.venueaapi.repository;

import com.utn.space.venueaapi.model.ServiceSelected;
import com.utn.space.venueaapi.model.records.ServiceSelectedDTO;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ServiceSelectedRepository extends JpaRepository<ServiceSelected,Integer> {
    @Query("SELECT new com.utn.space.venueaapi.model.records.ServiceSelectedDTO(ss.id, ss.priceAtReservation, s.id, r.id)" +
            "FROM ServiceSelected ss " +
            "JOIN ss.service s " +
            "JOIN ss.reservation r " +
            "WHERE r.id = :idReservation")
    List<ServiceSelectedDTO> findServiceSelectedByIdReservation(@Param("idReservation") Integer idReservation);

    @Modifying
    @Transactional
    @Query("DELETE FROM ServiceSelected s WHERE s.reservation.id = :idReservation")
    void deleteSelectedServiceByReserveId (@Param("idReservation")Integer id);
}
