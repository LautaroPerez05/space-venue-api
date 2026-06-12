package com.utn.space.venueaapi.model.records;

import com.utn.space.venueaapi.model.flags.Create;
import com.utn.space.venueaapi.model.flags.Update;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;
import java.time.LocalDate;

public record SpaceDTO (
        @NotBlank(groups =  Update.class)
        Integer idSpace,

        @NotBlank(groups = {Create.class, Update.class})
        @Positive(groups = {Create.class, Update.class})
        Integer idConsumerOwner,

        @NotBlank(groups = {Create.class, Update.class})
        @Positive(groups = {Create.class, Update.class})
        Integer idLocation,

        @NotBlank(groups = {Create.class, Update.class})
        @Positive(groups = {Create.class, Update.class})
        Integer idCancellationPolicies,

        @NotBlank(groups = {Create.class, Update.class})
        String googleCalendarId,

        @NotBlank(groups = {Create.class, Update.class})
        String nameSpace,

        @NotBlank(groups = {Create.class, Update.class})
        String description,

        @NotBlank(groups = {Create.class, Update.class})
        @Positive(groups = {Create.class, Update.class})
        BigDecimal basePrice,

        @NotBlank(groups = {Create.class, Update.class})
        LocalDate publicationDate,

        @NotBlank(groups = {Create.class, Update.class})
        Integer bufferTime,

        Boolean active
) {

}
