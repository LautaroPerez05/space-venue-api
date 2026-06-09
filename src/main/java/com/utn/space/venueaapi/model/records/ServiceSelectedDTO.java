package com.utn.space.venueaapi.model.records;

import com.utn.space.venueaapi.model.flags.Create;
import com.utn.space.venueaapi.model.flags.Update;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Positive;
import lombok.Data;

import java.math.BigDecimal;


public record ServiceSelectedDTO(
        @NotEmpty
        Integer id,

        @NotBlank(groups = {Create.class, Update.class})
        @Positive(groups = {Create.class, Update.class})
        BigDecimal priceAtReservation,

        @NotBlank(groups = {Create.class, Update.class})
        Integer idService,

        @NotBlank(groups = {Create.class, Update.class})
        Integer idReservation
) {
    public ServiceSelectedDTO(BigDecimal priceAtReservation, Integer idService, Integer idReservation) {
        this(0, priceAtReservation, idService, idReservation);
    }
}
//fede no se si se necesita