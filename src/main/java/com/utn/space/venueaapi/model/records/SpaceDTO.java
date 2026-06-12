package com.utn.space.venueaapi.model.records;

import com.utn.space.venueaapi.model.flags.Create;
import com.utn.space.venueaapi.model.flags.Update;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;
import java.time.LocalDate;

public record SpaceDTO (
        @NotEmpty(groups =  Update.class)
        Integer idSpace,

        @NotEmpty(groups = {Create.class, Update.class})
        @Positive(groups = {Create.class, Update.class})
        Integer idConsumerOwner,

        @NotEmpty(groups = {Create.class, Update.class})
        @Positive(groups = {Create.class, Update.class})
        Integer idLocation,

        @NotEmpty(groups = {Create.class, Update.class})
        @Positive(groups = {Create.class, Update.class})
        Integer idCancellationPolicies,

        @NotBlank(groups = {Create.class, Update.class})
        String googleCalendarId,

        @NotBlank(groups = {Create.class, Update.class})
        String nameSpace,

        @NotBlank(groups = {Create.class, Update.class})
        String description,

        @NotEmpty(groups = {Create.class, Update.class})
        @Positive(groups = {Create.class, Update.class})
        BigDecimal basePrice,

        @NotEmpty(groups = {Create.class, Update.class})
        LocalDate publicationDate,

        @NotEmpty(groups = {Create.class, Update.class})
        Integer bufferTime,

        Boolean active
) {

}
