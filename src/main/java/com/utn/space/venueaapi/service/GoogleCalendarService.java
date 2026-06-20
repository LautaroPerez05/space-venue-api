package com.utn.space.venueaapi.service;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.client.util.DateTime;
import com.google.api.services.calendar.Calendar;
import com.google.api.services.calendar.model.Event;
import com.google.api.services.calendar.model.EventDateTime;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;

@Slf4j
@Service
public class GoogleCalendarService {

    private final Calendar googleCalendarServiceAccount;

    @Autowired(required = false)
    private GoogleOAuth2Service googleOAuth2Service;

    // Inyección del cliente de Google Calendar previamente configurado como Bean
    public GoogleCalendarService(Calendar googleCalendar) {
        this.googleCalendarServiceAccount = googleCalendar;
    }

    /**
     * Crea un evento en el calendario del espacio e invita automáticamente al cliente y al oferente.
     * Intenta usar el calendario OAuth2 del usuario. Si no existe, usa la Service Account como fallback.
     */
    public String sincronizarReservaMultiplesCalendarios(
            String calendarIdSpace,
            String title,
            String description,
            LocalDateTime introduction,
            LocalDateTime endLocal,
            String emailClient,
            String emailOwner,
            boolean saveInClientCalendar,
            Integer consumerIdClient) throws IOException {

        // Instancia el modelo del evento base de Google
        Event evento = new Event()
                .setSummary(title)
                .setDescription(description + "\n\nCliente: " + emailClient + "\nDueño: " + emailOwner);

        // Formatea y setea la fecha/hora de inicio
        Date dateFrom = Date.from(introduction.atZone(ZoneId.systemDefault()).toInstant());
        EventDateTime start = new EventDateTime().setDateTime(new DateTime(dateFrom));
        evento.setStart(start);

        // Formatea y setea la fecha/hora de finalización
        Date dateUntil = Date.from(endLocal.atZone(ZoneId.systemDefault()).toInstant());
        EventDateTime end = new EventDateTime().setDateTime(new DateTime(dateUntil));
        evento.setEnd(end);

        // Intenta usar el calendario personal del cliente si tiene OAuth2 autorizado
        if (saveInClientCalendar && consumerIdClient != null && googleOAuth2Service != null) {
            sincronizarEnCalendarioPersonal(consumerIdClient, evento);
        }

        // Siempre sincronizar en el calendario del espacio usando Service Account
        Event eventoEjecutado = googleCalendarServiceAccount.events().insert(calendarIdSpace, evento)
                .setSendUpdates("none")
                .execute();

        log.info("✅ Evento sincronizado en calendario del espacio. ID: {}", eventoEjecutado.getId());

        return eventoEjecutado.getId();
    }

    /**
     * Sobrecarga para mantener compatibilidad con código anterior que no pasa consumerIdClient
     */
    public String sincronizarReservaMultiplesCalendarios(
            String calendarIdSpace,
            String title,
            String description,
            LocalDateTime introduction,
            LocalDateTime endLocal,
            String emailClient,
            String emailOwner,
            boolean saveInClientCalendar) throws IOException {
        return sincronizarReservaMultiplesCalendarios(
                calendarIdSpace, title, description, introduction, endLocal,
                emailClient, emailOwner, saveInClientCalendar, null
        );
    }

    /**
     * Intenta sincronizar el evento en el calendario personal del cliente usando OAuth2
     */
    private void sincronizarEnCalendarioPersonal(Integer consumerId, Event evento) {
        try {
            if (googleOAuth2Service == null) {
                log.debug("GoogleOAuth2Service no disponible, saltando sincronización personal");
                return;
            }

            // Verificar si el usuario tiene OAuth2 autorizado
            if (!googleOAuth2Service.hasValidToken(consumerId)) {
                log.info("ℹ️ El usuario ID {} no tiene Google Calendar conectado. Saltando sincronización personal.", consumerId);
                return;
            }

            // Obtener el credential del usuario
            Credential userCredential = googleOAuth2Service.getCredential(consumerId);

            // Crear cliente de Calendar con el credential del usuario
            HttpTransport httpTransport = GoogleNetHttpTransport.newTrustedTransport();
            GsonFactory jsonFactory = GsonFactory.getDefaultInstance();

            Calendar userCalendar = new Calendar.Builder(httpTransport, jsonFactory, userCredential)
                    .setApplicationName("VenueAPI")
                    .build();

            // Insertar evento en el calendario personal del usuario
            Event eventoPersonal = userCalendar.events().insert("primary", evento)
                    .setSendUpdates("all")  // Enviar invitación al usuario
                    .execute();

            log.info("✅ Evento sincronizado en calendario personal del usuario ID {}. ID del evento: {}",
                    consumerId, eventoPersonal.getId());

        } catch (GeneralSecurityException | IOException e) {
            log.warn("⚠️ No se pudo sincronizar evento en calendario personal del usuario ID {}: {}",
                    consumerId, e.getMessage());
        }
    }
}
