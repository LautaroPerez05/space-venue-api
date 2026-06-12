package com.utn.space.venueaapi.model.records;

import com.utn.space.venueaapi.model.flags.Create;
import com.utn.space.venueaapi.model.flags.Update;
import jakarta.validation.constraints.*;

import java.time.LocalDateTime;

public record CommentDTO (
        @NotEmpty(groups = {Create.class, Update.class})
        Integer idComment,

        @NotEmpty(groups = {Create.class, Update.class})
        Integer idConsumer,

        @NotEmpty(groups = {Create.class, Update.class})
        Integer idSpace,

        @NotBlank(groups = {Create.class, Update.class})
        String description,

        @NotNull(groups = {Create.class, Update.class})
        @Min(value = 0, message = "La calificacion minima es 0")
        @Max(value = 5, message = "La calificacion maxima es 5")
        Byte score,

        LocalDateTime created_at){

}
