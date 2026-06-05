package com.utn.space.venueaapi.model.records;

import java.math.BigDecimal;

public record SpaceServiceItemDTO(
        Integer id,
        String description,
        BigDecimal price,
        Boolean isActive,
        Integer idSpace
) {}
