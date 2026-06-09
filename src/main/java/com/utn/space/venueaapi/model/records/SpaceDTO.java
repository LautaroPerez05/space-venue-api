package com.utn.space.venueaapi.model.records;

import java.math.BigDecimal;
import java.time.LocalDate;

public record SpaceDTO (
        Integer id_space,
        Integer id_consumer_owner,
        Integer id_location,
        Integer id_cancellation_policies,
        String google_calendar_id,
        String name_space,
        String description,
        BigDecimal base_price,
        LocalDate publication_date,
        Integer buffer_time,
        Boolean isActive) {

}
