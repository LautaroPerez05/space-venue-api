package com.utn.space.venueaapi.model.records;

import java.math.BigDecimal;

public record ServiceSelectedDTO(
        Integer id_service_selected,
        BigDecimal priceAtReservation,
        Integer idReservation,
        String descriptionFrozen
) {
    public ServiceSelectedDTO(Integer id_service_selected, BigDecimal priceAtReservation, Integer idReservation, String descriptionFrozen) {
        this.id_service_selected = id_service_selected;
        this.priceAtReservation = priceAtReservation;
        this.idReservation = idReservation;
        this.descriptionFrozen = descriptionFrozen;
    }
}
