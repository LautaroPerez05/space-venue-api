package com.utn.space.venueaapi.repository;

import com.utn.space.venueaapi.model.ServiceSelected;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ServiceSelectedRepository extends JpaRepository<ServiceSelected,Long> {
}
