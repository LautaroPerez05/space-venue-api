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


@Schema( description = "DTO de Reservas")
public record ReservationDTO (
    @NotEmpty(groups = Update.class)
    @Schema(description = "Identificador único", example = "1")
    Integer id,

    @NotBlank(groups = {Create.class, Update.class})
    @Schema(description = "Nombre de la reserva", example = "Cumple de Sofia")
    String title,

    @NotBlank(groups = {Create.class, Update.class})
    @Schema(description = "Descripción del Evento", example = "El cumpleaños de 15 de Sofia")
    String description,

    @NotEmpty(groups = {Create.class, Update.class})
    String googleEventCode,

    @NotEmpty(groups = {Create.class, Update.class})
    @Schema(description = "Fecha de Inicio del Evento", example = "2026-09-15T21:00:00")
    LocalDateTime fromDate,

    @NotEmpty(groups = {Create.class, Update.class})
    @Schema(description = "Fecha de Fin del Evento", example = "2026-09-16T05:00:00")
    LocalDateTime untilDate,

    @Schema(description = "Precio final de la reserva")
    Double finalPrice,

    @NotEmpty(groups = {Create.class, Update.class})
    @Schema(description = "Estado de la Reserva", example = "CONFIRMED")
    ReservationStatus status,

    @NotEmpty(groups = {Create.class, Update.class})
    @Schema(description = "Fecha en la que se hizo la reserva", example = "2026-05-20T011:25:31")
    LocalDateTime createdAt,

    @Schema(description = "Flag para saber si se hizo un softdelete en la reserva", example = "2026-05-20T011:25:31")
    Boolean isActive,
    Boolean saveToMyCalendar,

    @NotEmpty(groups = {Create.class, Update.class})
    @Schema(description = "ID del usuario que creo la reserva")
    Integer idConsumer,

    @NotEmpty(groups = {Create.class, Update.class})
    @Schema(description = "ID del espacio a reservar", example = "2026-05-20T011:25:31")
    Integer idSpace,


    @Schema(description = "Lista de las IDs de los servicios agregados")
    List<Integer> idServicesSelec
){
    public Boolean getSaveToMyCalendar() {
        // Evita NullPointerException asignando falso por defecto si no es provisto en la petición JSON
        return saveToMyCalendar != null ? saveToMyCalendar : false;
    }
}
