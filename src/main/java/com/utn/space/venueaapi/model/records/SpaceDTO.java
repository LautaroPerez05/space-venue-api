package com.utn.space.venueaapi.model.records;

import java.math.BigDecimal;
import java.time.LocalDate;

public record SpaceDTO (
        Integer idSpace,
        Integer idConsumerOwner,
        Integer idLocation,
        Integer idCancellationPolicies,
        String googleCalendarId,
        String nameSpace,
        String description,
        BigDecimal basePrice,
        LocalDate publicationDate,
        Integer bufferTime,
        Boolean isActive) {

}
