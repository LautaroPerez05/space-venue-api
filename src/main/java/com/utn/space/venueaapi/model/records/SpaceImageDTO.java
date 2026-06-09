package com.utn.space.venueaapi.model.records;

import java.time.LocalDateTime;

public record SpaceImageDTO (
        Integer idSpace,
        String fileName,
        String urlImage,
        LocalDateTime dateSend){

}
