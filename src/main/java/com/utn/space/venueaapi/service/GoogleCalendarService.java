package com.utn.space.venueaapi.service;

import com.google.api.client.auth.oauth2.Credential;
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
import java.time.ZonedDateTime;

@Slf4j
@Service
public class GoogleCalendarService {

    private final Calendar googleCalendarServiceAccount;

    @Autowired(required = false)
    private GoogleOAuth2Service googleOAuth2Service;

    public GoogleCalendarService(Calendar googleCalendar) {
        this.googleCalendarServiceAccount = googleCalendar;
    }

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

        if (googleCalendarServiceAccount == null) {
            log.warn("⚠️ Google Calendar no disponible (bean nulo). Saltando sincronización.");
            return null;
        }

        System.out.println("LOG-CALENDAR: saveInClientCalendar = " + saveInClientCalendar);
        System.out.println("LOG-CALENDAR: consumerIdClient = " + consumerIdClient);

        Event evento = new Event()
                .setSummary(title)
                .setDescription(description + "\n\nCliente: " + emailClient + "\nDueño: " + emailOwner);

        ZoneId zonaLocal = ZoneId.of("America/Argentina/Buenos_Aires");

        ZonedDateTime desdeConZona = introduction.atZone(zonaLocal);
        EventDateTime start = new EventDateTime()
                .setDateTime(new DateTime(desdeConZona.toInstant().toEpochMilli()))
                .setTimeZone("America/Argentina/Buenos_Aires");
        evento.setStart(start);

        ZonedDateTime hastaConZona = endLocal.atZone(zonaLocal);
        EventDateTime end = new EventDateTime()
                .setDateTime(new DateTime(hastaConZona.toInstant().toEpochMilli()))
                .setTimeZone("America/Argentina/Buenos_Aires");
        evento.setEnd(end);

        Event eventoEjecutado = googleCalendarServiceAccount.events().insert(calendarIdSpace, evento)
                .setSendUpdates("none")
                .execute();

        log.info("✅ Evento sincronizado en calendario del espacio. ID: {}", eventoEjecutado.getId());

        if (saveInClientCalendar && consumerIdClient != null && googleOAuth2Service != null) {
            System.out.println("LOG-CALENDAR: 🟢 Entrando a la sincronización personal...");
            sincronizarEnCalendarioPersonal(consumerIdClient, evento);
        } else {
            System.out.println("LOG-CALENDAR: ❌ NO se cumplió la condición para el calendario personal.");
        }

        return eventoEjecutado.getId();
    }

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

    private void sincronizarEnCalendarioPersonal(Integer consumerId, Event evento) {
        log.info("DEBUG: Intentando entrar a sincronizar calendario personal para usuario ID: {}", consumerId);

        try {
            System.out.println("LOG-CALENDAR: Buscando token para el usuario: " + consumerId);

            if (googleOAuth2Service == null) {
                log.debug("GoogleOAuth2Service no disponible, saltando sincronización personal");
                return;
            }

            if (!googleOAuth2Service.hasValidToken(consumerId)) {
                log.info("ℹ️ El usuario ID {} no tiene Google Calendar conectado. Saltando sincronización personal.", consumerId);
                return;
            }

            Credential userCredential = googleOAuth2Service.getCredential(consumerId);

            Calendar userCalendar = new Calendar.Builder(
                    userCredential.getTransport(),
                    userCredential.getJsonFactory(),
                    userCredential)
                    .setApplicationName("VenueAPI")
                    .build();

            System.out.println("LOG-CALENDAR: Enviando evento a 'primary'...");
            Event resultado = userCalendar.events().insert("primary", evento).execute();

            System.out.println("LOG-CALENDAR: 🎉 ¡ÉXITO! Evento creado en cuenta personal. Link: " + resultado.getHtmlLink());

        } catch (GeneralSecurityException | IOException e) {
            System.out.println("LOG-CALENDAR: 🚨 ERROR CRÍTICO EN CALENDARIO PERSONAL: " + e.getMessage());
            e.printStackTrace();

            log.warn("⚠️ No se pudo sincronizar evento en calendario personal del usuario ID {}: {}",
                    consumerId, e.getMessage());
        }
    }
}