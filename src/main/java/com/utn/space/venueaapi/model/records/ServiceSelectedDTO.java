package com.utn.space.venueaapi.model.records;

public record ServiceSelectedDTO(
        Long id,
        Double priceAtReservation,
        Long idService,
        Long idReservation
) {
    public ServiceSelectedDTO(Double priceAtReservation, Long idService, Long idReservation) {
        this(0L, priceAtReservation, idService, idReservation);
    }
}
