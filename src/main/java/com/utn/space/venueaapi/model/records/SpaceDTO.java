package com.utn.space.venueaapi.model.records;

import java.time.LocalDate;

public record SpaceDTO (
        Long id_space,
        Long id_consumer_owner,
        Long id_location,
        Long id_cancellation_policies,
        String name_space,
        String description,
        Double base_price,
        LocalDate publication_date,
        Long buffer_time) {

}
