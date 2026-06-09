package com.utn.space.venueaapi.model.records;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.math.BigDecimal;


public record ServiceSelectedDTO(
        @NotEmpty
        Integer id,
        @NotEmpty
        BigDecimal priceAtReservation,
        @NotEmpty
        Integer idService,
        @NotEmpty
        Integer idReservation
) {
    public ServiceSelectedDTO(BigDecimal priceAtReservation, Integer idService, Integer idReservation) {
        this(0, priceAtReservation, idService, idReservation);
    }
}
//fede no se si se necesita