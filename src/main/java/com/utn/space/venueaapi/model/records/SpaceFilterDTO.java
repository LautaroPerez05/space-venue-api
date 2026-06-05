package com.utn.space.venueaapi.model.records;

public record SpaceFilterDTO(
        Integer id_consumer_owner,
        Integer id_location,
        Long name_space,
        Double minPrice,
        Double maxPrice)
{}
