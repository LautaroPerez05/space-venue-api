package com.utn.space.venueaapi.model.records;

import java.time.LocalDate;

public record SpaceImageDTO (
        Long id_space,
        String file_name,
        String url_image,
        LocalDate date_sent){

}
