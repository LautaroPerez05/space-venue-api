package com.utn.space.venueaapi.model.records;

import com.utn.space.venueaapi.model.ReservationStatus;
import com.utn.space.venueaapi.model.flags.Create;
import com.utn.space.venueaapi.model.flags.Update;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.stereotype.Component;
import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Component
public class ReservationDTO {

    @NotBlank(groups = Update.class)
    private Long id;

    @NotBlank(groups = {Create.class, Update.class})
    private String title;

    @NotBlank(groups = {Create.class, Update.class})
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

    private List<Long> id_servicesSelec;
}
