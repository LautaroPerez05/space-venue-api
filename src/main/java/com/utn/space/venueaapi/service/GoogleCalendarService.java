package com.utn.space.venueaapi.service;

import com.google.api.client.util.DateTime;
import com.google.api.services.calendar.Calendar;
import com.google.api.services.calendar.model.Event;
import com.google.api.services.calendar.model.EventAttendee;
import com.google.api.services.calendar.model.EventDateTime;
import org.springframework.stereotype.Service;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Service // Marca la clase como un componente de servicio de lógica de negocio
public class GoogleCalendarService {

    private final Calendar googleCalendar;

    // Inyección del cliente de Google Calendar previamente configurado como Bean
    public GoogleCalendarService(Calendar googleCalendar) {
        this.googleCalendar = googleCalendar;
    }


    /// Crea un evento en el calendario del espacio e invita automáticamente al cliente y al oferente.

    public String sincronizarReservaMultiplesCalendarios(
            String calendarIdSpace,
            String title,
            String description,
            LocalDateTime introduction,
            LocalDateTime endLocal,
            String emailClient,
            String emailOwner,
            boolean saveInClientCalendar) throws IOException {

        // 1. Instancia el modelo del evento base de Google
        Event evento = new Event()
                .setSummary(title)
                .setDescription(description);

        // 2. Formatea y setea la fecha/hora de inicio convirtiendo de LocalDateTime a DateTime de Google
        Date dateFrom = Date.from(introduction.atZone(ZoneId.systemDefault()).toInstant());
        EventDateTime start = new EventDateTime().setDateTime(new DateTime(dateFrom));
        evento.setStart(start);

        // 3. Formatea y setea la fecha/hora de finalización
        Date dateUntil = Date.from(endLocal.atZone(ZoneId.systemDefault()).toInstant());
        EventDateTime end = new EventDateTime().setDateTime(new DateTime(dateUntil));
        evento.setEnd(end);

        // 4. Gestión dinámica de Invitados (Attendees) para impactar múltiples calendarios
        List<EventAttendee> guests = new ArrayList<>();

        // Agrega al Oferente (Dueño del espacio) de forma obligatoria
        EventAttendee asistenteOferente = new EventAttendee().setEmail(emailOwner).setResponseStatus("accepted");
        guests.add(asistenteOferente);

        // Agrega al Cliente (Consumer) únicamente si este otorgó su consentimiento explícito
        if (saveInClientCalendar && emailClient != null) {
            EventAttendee asistenteCliente = new EventAttendee().setEmail(emailClient).setResponseStatus("tentative");
            guests.add(asistenteCliente);
        }

        // Vincula la lista de invitados construida al cuerpo del evento
        evento.setAttendees(guests);

        // 5. Envía el evento a Google indicando que se deben despachar notificaciones de invitación por correo
        Event eventoEjecutado = googleCalendar.events().insert(calendarIdSpace, evento)
                .setSendUpdates("all") // "all" fuerza el envío de correos y la inserción automática en los calendarios de los invitados
                .execute();

        // Devuelve el ID único del evento generado por los servidores de Google
        return eventoEjecutado.getId();
    }
}
