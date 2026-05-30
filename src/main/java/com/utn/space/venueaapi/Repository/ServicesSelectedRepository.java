package com.utn.space.venueaapi.Repository;

import com.utn.space.venueaapi.Model.ServicesSelected;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ServicesSelectedRepository extends JpaRepository<ServicesSelected,Long> {
}
