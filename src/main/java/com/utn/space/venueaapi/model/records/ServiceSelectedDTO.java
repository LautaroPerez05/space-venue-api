package com.utn.space.venueaapi.model.records;

import com.utn.space.venueaapi.model.flags.Create;
import com.utn.space.venueaapi.model.flags.Update;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Positive;
import lombok.Data;

import java.math.BigDecimal;


public record ServiceSelectedDTO(
        @NotEmpty(groups = Update.class)
        Integer id,

        @NotBlank(groups = {Create.class, Update.class})
        @Positive(groups = {Create.class, Update.class})
        BigDecimal priceAtReservation,

        @NotBlank(groups = {Create.class, Update.class})
        String descriptionFrozen,

        @NotBlank(groups = {Create.class, Update.class})
        Integer idReservation
){}
//fede no se si se necesita