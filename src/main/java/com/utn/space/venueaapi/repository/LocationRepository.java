package com.utn.space.venueaapi.repository;

import com.utn.space.venueaapi.model.Location;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LocationRepository extends JpaRepository<Location,Long> {

}
