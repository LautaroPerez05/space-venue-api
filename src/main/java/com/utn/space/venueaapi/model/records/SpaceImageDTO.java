package com.utn.space.venueaapi.model.records;

import java.time.LocalDateTime;

public record SpaceImageDTO (
        Integer id_space,
        String file_name,
        String url_image,
        LocalDateTime date_send){

}
