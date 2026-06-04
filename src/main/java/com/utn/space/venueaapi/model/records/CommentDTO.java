package com.utn.space.venueaapi.model.records;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public record CommentDTO (
        Long id_consumer,
        Long id_space,
        String description,

        @NotNull
        @Min(value = 0, message = "La calificacion minima es 0")
        @Max(value = 5, message = "La calificacion maxima es 5")
        Double score,
        LocalDate created_at){

}
