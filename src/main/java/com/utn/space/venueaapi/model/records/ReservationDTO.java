package com.utn.space.venueaapi.model.records;

import com.utn.space.venueaapi.model.Consumer;
import com.utn.space.venueaapi.model.ReservationStatus;
import com.utn.space.venueaapi.model.Space;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.stereotype.Component;
import java.time.LocalDateTime;
@Data
@NoArgsConstructor
@AllArgsConstructor

@Component
public class ReservationDTO {

    @NotBlank(groups = Update.class)
    private Long id;

    @NotBlank(groups = {Create.class, Update.class})
    private String title;
    private String description;

    private String googleEventCode;

    @NotBlank(groups = {Create.class, Update.class})
    private LocalDateTime fromDate;

    @NotBlank(groups = {Create.class, Update.class})
    private LocalDateTime untilDate;

    private Double finalPrice;

    @NotBlank(groups = {Create.class, Update.class})
    private ReservationStatus status;

    @NotBlank(groups = {Create.class, Update.class})
    private LocalDateTime createdAt;
    private Boolean isActive;

    @NotBlank(groups = {Create.class, Update.class})
    private Long id_consumer;

    @NotBlank(groups = {Create.class, Update.class})
    private Long id_space;
}
