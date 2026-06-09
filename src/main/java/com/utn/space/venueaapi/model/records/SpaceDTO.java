package com.utn.space.venueaapi.model.records;

import com.utn.space.venueaapi.model.flags.Create;
import com.utn.space.venueaapi.model.flags.Update;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;
import java.time.LocalDate;

public record SpaceDTO (
        @NotBlank(groups =  Update.class)
        Integer id_space,

        @NotBlank(groups = {Create.class, Update.class})
        @Positive(groups = {Create.class, Update.class})
        Integer id_consumer_owner,

        @NotBlank(groups = {Create.class, Update.class})
        @Positive(groups = {Create.class, Update.class})
        Integer id_location,

        @NotBlank(groups = {Create.class, Update.class})
        @Positive(groups = {Create.class, Update.class})
        Integer id_cancellation_policies,

        @NotBlank(groups = {Create.class, Update.class})
        String google_calendar_id,

        @NotBlank(groups = {Create.class, Update.class})
        String name_space,

        @NotBlank(groups = {Create.class, Update.class})
        String description,

        @NotBlank(groups = {Create.class, Update.class})
        @Positive(groups = {Create.class, Update.class})
        BigDecimal base_price,

        @NotBlank(groups = {Create.class, Update.class})
        LocalDate publication_date,

        @NotBlank(groups = {Create.class, Update.class})
        Integer buffer_time
) {

}
