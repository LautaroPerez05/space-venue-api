package com.utn.space.venueaapi.model.records;

import com.utn.space.venueaapi.model.flags.Create;
import com.utn.space.venueaapi.model.flags.Update;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

public record SpaceServiceItemDTO(
        @NotBlank(groups = Update.class)
        Integer id,

        @NotBlank(groups = {Create.class, Update.class})
        String description,

        @NotBlank(groups = {Create.class, Update.class})
        @Positive(groups = {Create.class, Update.class})
        BigDecimal price,

        @NotBlank(groups = {Create.class, Update.class})
        Boolean isActive,

        @NotBlank(groups = {Create.class, Update.class})
        Integer idSpace
) {}
