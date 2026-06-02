package com.utn.space.venueaapi.model.records;

import java.time.LocalDate;

public record CommentDTO (
        Long id_consumer,
        Long id_space,
        String description,
        Double score,
        LocalDate created_at){

}
