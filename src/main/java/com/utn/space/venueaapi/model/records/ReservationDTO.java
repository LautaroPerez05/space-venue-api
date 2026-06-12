package com.utn.space.venueaapi.model.records;

import com.utn.space.venueaapi.model.ReservationStatus;
import com.utn.space.venueaapi.model.flags.Create;
import com.utn.space.venueaapi.model.flags.Update;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
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

@Schema( description = "DTO de Reservas")
public class ReservationDTO {
    @NotEmpty(groups = Update.class)
    @Schema(description = "Identificador único", example = "1")
    private Integer id;

    @NotBlank(groups = {Create.class, Update.class})
    @Schema(description = "Nombre de la reserva", example = "Cumple de Sofia")
    private String title;

    @NotBlank(groups = {Create.class, Update.class})
    @Schema(description = "Descripción del Evento", example = "El cumpleaños de 15 de Sofia")
    private String description;

    @NotEmpty(groups = {Create.class, Update.class})
    private String googleEventCode;

    @NotEmpty(groups = {Create.class, Update.class})
    @Schema(description = "Fecha de Inicio del Evento", example = "2026-09-15T21:00:00")
    private LocalDateTime fromDate;

    @NotEmpty(groups = {Create.class, Update.class})
    @Schema(description = "Fecha de Fin del Evento", example = "2026-09-16T05:00:00")
    private LocalDateTime untilDate;

    @Schema(description = "Precio final de la reserva")
    private Double finalPrice;

    @NotEmpty(groups = {Create.class, Update.class})
    @Schema(description = "Estado de la Reserva", example = "CONFIRMED")
    private ReservationStatus status;

    @NotEmpty(groups = {Create.class, Update.class})
    @Schema(description = "Fecha en la que se hizo la reserva", example = "2026-05-20T011:25:31")
    private LocalDateTime createdAt;

    @Schema(description = "Flag para saber si se hizo un softdelete en la reserva", example = "2026-05-20T011:25:31")
    private Boolean isActive;

    private Boolean saveToMyCalendar;

    @NotEmpty(groups = {Create.class, Update.class})
    @Schema(description = "ID del usuario que creo la reserva")
    private Integer idConsumer;

    @NotEmpty(groups = {Create.class, Update.class})
    @Schema(description = "ID del espacio a reservar", example = "2026-05-20T011:25:31")
    private Integer idSpace;


    @Schema(description = "Lista de las IDs de los servicios agregados")
    private List<Integer> idServicesSelec;


    public Boolean getSaveToMyCalendar() {
        // Evita NullPointerException asignando falso por defecto si no es provisto en la petición JSON
        return saveToMyCalendar != null ? saveToMyCalendar : false;
    }
}
