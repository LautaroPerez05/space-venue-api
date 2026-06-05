package com.utn.space.venueaapi.model.records;

import java.math.BigDecimal;

public record ServiceSelectedWithoutReservationDTO(
        BigDecimal priceAtReservation,
        Integer idService
) {
}
