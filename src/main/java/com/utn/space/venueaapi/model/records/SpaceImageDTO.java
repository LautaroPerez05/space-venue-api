package com.utn.space.venueaapi.model.records;

import com.utn.space.venueaapi.model.flags.Create;
import com.utn.space.venueaapi.model.flags.Update;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;

import java.time.LocalDateTime;

public record SpaceImageDTO (
        @NotEmpty(groups = {Create.class, Update.class})
        Integer idImage,

        @NotEmpty(groups = {Create.class, Update.class})
        Integer idSpace,

        @NotBlank(groups = {Create.class, Update.class})
        String fileName,

        @NotBlank(groups = {Create.class, Update.class})
        String urlImage,

        @NotEmpty(groups = {Create.class, Update.class})
        LocalDateTime dateSend){
}
