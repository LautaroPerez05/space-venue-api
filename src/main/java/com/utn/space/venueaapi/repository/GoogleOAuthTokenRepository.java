package com.utn.space.venueaapi.repository;

import com.utn.space.venueaapi.model.GoogleOAuthToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface GoogleOAuthTokenRepository extends JpaRepository<GoogleOAuthToken, Integer> {
    Optional<GoogleOAuthToken> findByConsumer_IdConsumer(Integer consumerId);
    Optional<GoogleOAuthToken> findByGoogleEmail(String googleEmail);
}

