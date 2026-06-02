package com.utn.space.venueaapi.repository;

import com.utn.space.venueaapi.model.CancellationPolicy;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CancellationPolicyRepository extends JpaRepository<CancellationPolicy,Long> {

}
