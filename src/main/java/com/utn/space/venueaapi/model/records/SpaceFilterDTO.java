package com.utn.space.venueaapi.model.records;

public record SpaceFilterDTO(
        Long id_consumer_owner,
        Long id_location,
        Long name_space,
        Double minPrice,
        Double maxPrice)
{}
