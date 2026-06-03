package com.utn.space.venueaapi.model.records;

public record SpaceServiceItemDTO(
        Long id,
        String description,
        Double price,
        Boolean isActive,
        Long idSpace
) {}
