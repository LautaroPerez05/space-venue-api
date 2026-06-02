package com.utn.space.venueaapi.model.records;

public record SpaceServiceDTO (
        Long id,
        String description,
        Double price,
        Long idSpace
) {}
