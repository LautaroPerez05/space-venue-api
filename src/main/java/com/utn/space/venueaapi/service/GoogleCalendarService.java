package com.utn.space.venueaapi.service;

import com.google.api.client.util.DateTime;
import com.google.api.services.calendar.Calendar;
import com.google.api.services.calendar.model.Event;
import com.google.api.services.calendar.model.EventDateTime;
import org.springframework.stereotype.Service;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;

@Service
public class GoogleCalendarService {

    private final Calendar googleCalendar;

    // Inyección del cliente de Google Calendar previamente configurado como Bean
    public GoogleCalendarService(Calendar googleCalendar) {
        this.googleCalendar = googleCalendar;
    }


    // Crea un evento en el calendario del espacio e invita automáticamente al cliente y al oferente.
    public String sincronizarReservaMultiplesCalendarios(
            String calendarIdSpace,
            String title,
            String description,
            LocalDateTime introduction,
            LocalDateTime endLocal,
            String emailClient,
            String emailOwner,
            boolean saveInClientCalendar) throws IOException {

        // Instancia el modelo del evento base de Google
        Event evento = new Event()
                .setSummary(title)
                .setDescription(description + "\n\nCliente: " + emailClient + "\nDueño: " + emailOwner); // 🌟 Colocamos los correos en la descripción para que no se pierdan

        // Formatea y setea la fecha/hora de inicio
        Date dateFrom = Date.from(introduction.atZone(ZoneId.systemDefault()).toInstant());
        EventDateTime start = new EventDateTime().setDateTime(new DateTime(dateFrom));
        evento.setStart(start);

        // Formatea y setea la fecha/hora de finalización
        Date dateUntil = Date.from(endLocal.atZone(ZoneId.systemDefault()).toInstant());
        EventDateTime end = new EventDateTime().setDateTime(new DateTime(dateUntil));
        evento.setEnd(end);

        /* SE ELIMINA EL BLOQUE DE ATTENDEES (GUESTS) QUE PROVOCA UN ERROR 403
        * Las Cuentas de Servicio gratuitas no pueden tener invitados externos.
        * Gestión dinámica de Invitados (Attendees) para impactar múltiples calendarios
        *
        List<EventAttendee> guests = new ArrayList<>();

        * */

        // Enviamos el evento al calendario "primary" de la Service Account
        Event eventoEjecutado = googleCalendar.events().insert(calendarIdSpace, evento)
                .setSendUpdates("none")
                .execute();

        // Devuelve el ID único del evento generado por los servidores de Google
        return eventoEjecutado.getId();
    }
}
