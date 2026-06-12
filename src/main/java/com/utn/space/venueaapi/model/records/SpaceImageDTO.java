package com.utn.space.venueaapi.model.records;

import com.utn.space.venueaapi.model.flags.Create;
import com.utn.space.venueaapi.model.flags.Update;
import jakarta.validation.constraints.NotBlank;

import java.time.LocalDateTime;

public record SpaceImageDTO (
        @NotBlank(groups = Update.class)
        Integer idImage,

        @NotBlank(groups = {Create.class, Update.class})
        Integer idSpace,

        @NotBlank(groups = {Create.class, Update.class})
        String fileName,

        @NotBlank(groups = {Create.class, Update.class})
        String urlImage,

        @NotBlank(groups = Update.class)
        LocalDateTime dateSend){
}
