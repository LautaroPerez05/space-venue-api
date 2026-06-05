package com.utn.space.venueaapi.model.records;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

public record CommentDTO (
        Integer id_consumer,
        Integer id_space,
        String description,

        @NotNull
        @Min(value = 0, message = "La calificacion minima es 0")
        @Max(value = 5, message = "La calificacion maxima es 5")
        Byte score,
        LocalDateTime created_at){

}
