package com.utn.space.venueaapi.model.records;

import java.math.BigDecimal;

public record ServiceSelectedDTO(
        Integer id,
        BigDecimal priceAtReservation,
        Integer idService,
        Integer idReservation
) {
    public ServiceSelectedDTO(BigDecimal priceAtReservation, Integer idService, Integer idReservation) {
        this(0, priceAtReservation, idService, idReservation);
    }
}
