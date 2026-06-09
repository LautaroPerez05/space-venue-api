package com.utn.space.venueaapi.model.records;

import java.math.BigDecimal;

public record ServiceSelectedDTO(
        Integer idServiceSelected,
        BigDecimal priceAtReservation,
        Integer idReservation,
        String descriptionFrozen
) {
    public ServiceSelectedDTO(Integer idServiceSelected, BigDecimal priceAtReservation, Integer idReservation, String descriptionFrozen) {
        this.idServiceSelected = idServiceSelected;
        this.priceAtReservation = priceAtReservation;
        this.idReservation = idReservation;
        this.descriptionFrozen = descriptionFrozen;
    }
}
