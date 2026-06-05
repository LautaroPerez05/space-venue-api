package com.utn.space.venueaapi.repository;

import com.utn.space.venueaapi.model.Notification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NotificationRepository extends JpaRepository<Notification,Long> {
    @Query(value = "SELECT * FORM Notification where id_consumer=?;",nativeQuery = true)
    List<Notification>findAllByIdConsumer(Long id);
}
