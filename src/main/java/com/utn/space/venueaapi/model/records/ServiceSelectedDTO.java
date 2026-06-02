package com.utn.space.venueaapi.model.records;

public record ServiceSelectedDTO(
        Long id,
        Double priceAtReservation,
        Long idService,
        Long idReservation
) {}
