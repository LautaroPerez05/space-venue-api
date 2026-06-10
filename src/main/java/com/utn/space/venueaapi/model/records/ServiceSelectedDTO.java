package com.utn.space.venueaapi.model.records;

import java.math.BigDecimal;

public record ServiceSelectedDTO(
        Integer id,
        BigDecimal priceAtReservation,
        Integer idReservation,
        String descriptionFrozen
) {
    public ServiceSelectedDTO(Integer id, BigDecimal priceAtReservation, Integer idReservation, String descriptionFrozen) {
        this.id = id;
        this.priceAtReservation = priceAtReservation;
        this.idReservation = idReservation;
        this.descriptionFrozen = descriptionFrozen;
    }
}
