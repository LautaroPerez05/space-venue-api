package com.utn.space.venueaapi.model.records;

import com.utn.space.venueaapi.model.flags.Create;
import com.utn.space.venueaapi.model.flags.Update;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;
import java.time.LocalDate;

public record SpaceDTO (
        @NotNull(groups = Update.class)
        Integer idSpace,

        @NotNull(groups = {Create.class, Update.class})
        @Positive(groups = {Create.class, Update.class})
        Integer idConsumerOwner,

        @NotNull(groups = {Create.class, Update.class})
        @Valid
        LocationDTO location,

        @NotNull(groups = {Create.class, Update.class})
        @Valid
        String cancellationPolicies,

        // 🟢 QUITAMOS Create.class: Solo se valida al actualizar, en la creación puede ser null
        @NotBlank(groups = Update.class)
        String googleCalendarId,

        @NotBlank(groups = {Create.class, Update.class})
        String nameSpace,

        @NotBlank(groups = {Create.class, Update.class})
        String description,

        @NotNull(groups = {Create.class, Update.class})
        @Positive(groups = {Create.class, Update.class})
        BigDecimal basePrice,

        // 🟢 QUITAMOS VALIDACIÓN DE CREACIÓN: El backend se encargará de setearlo
        LocalDate publicationDate,

        @NotNull(groups = {Create.class, Update.class})
        Integer bufferTime,

        Boolean active
) {}
