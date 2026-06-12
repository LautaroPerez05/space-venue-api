package com.utn.space.venueaapi.model.records;

import com.utn.space.venueaapi.model.flags.Create;
import com.utn.space.venueaapi.model.flags.Update;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

public record SpaceServiceItemDTO(
        @NotEmpty(groups = Update.class)
        Integer id,

        @NotBlank(groups = {Create.class, Update.class})
        String description,

        @NotEmpty(groups = {Create.class, Update.class})
        @Positive(groups = {Create.class, Update.class})
        BigDecimal price,

        @NotEmpty(groups = {Create.class, Update.class})
        Boolean isActive,

        @NotEmpty(groups = {Create.class, Update.class})
        Integer idSpace
) {}
