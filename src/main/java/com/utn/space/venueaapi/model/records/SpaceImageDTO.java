package com.utn.space.venueaapi.model.records;

import com.utn.space.venueaapi.model.flags.Create;
import com.utn.space.venueaapi.model.flags.Update;
import jakarta.validation.constraints.NotBlank;

import java.time.LocalDateTime;

public record SpaceImageDTO (
        @NotBlank(groups = Update.class)
        Integer id_image,

        @NotBlank(groups = {Create.class, Update.class})
        Integer id_space,

        @NotBlank(groups = {Create.class, Update.class})
        String file_name,

        @NotBlank(groups = {Create.class, Update.class})
        String url_image,

        @NotBlank(groups = Update.class)
        LocalDateTime date_send){
}
