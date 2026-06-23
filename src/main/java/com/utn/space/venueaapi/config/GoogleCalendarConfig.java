package com.utn.space.venueaapi.config;

import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.HttpRequestInitializer;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.calendar.Calendar;
import com.google.api.services.calendar.CalendarScopes;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;

import java.io.ByteArrayInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.security.GeneralSecurityException;
import java.util.Base64;
import java.util.Collections;

@Configuration
public class GoogleCalendarConfig {

    private static final String APPLICATION_NAME = "VenueAPI";
    private static final String CREDENTIALS_FILE_PATH = "google-credentials.json";

    @Bean
    public Calendar googleCalendarClient() {
        try {
            final HttpTransport httpTransport = GoogleNetHttpTransport.newTrustedTransport();
            final GsonFactory jsonFactory = GsonFactory.getDefaultInstance();

            InputStream in;
            String base64Credentials = System.getenv("GOOGLE_CREDENTIALS_BASE64");

            if (base64Credentials != null && !base64Credentials.trim().isEmpty()) {
                byte[] decoded = Base64.getDecoder().decode(base64Credentials.trim());
                in = new ByteArrayInputStream(decoded);
            } else {
                ClassPathResource resource = new ClassPathResource(CREDENTIALS_FILE_PATH);
                if (!resource.exists()) {
                    System.err.println("[GoogleCalendar] ⚠️ Credenciales no encontradas. " +
                            "Google Calendar deshabilitado. La app continuará sin esta función.");
                    return null;
                }
                in = resource.getInputStream();
            }

            GoogleCredential credential = GoogleCredential.fromStream(in)
                    .createScoped(Collections.singleton(CalendarScopes.CALENDAR));

            HttpRequestInitializer requestWithTimeout = request -> {
                credential.initialize(request);
                request.setConnectTimeout(10_000); // 10 segundos
                request.setReadTimeout(30_000);    // 30 segundos
            };

            System.out.println("[GoogleCalendar] ✅ Cliente inicializado correctamente.");
            return new Calendar.Builder(httpTransport, jsonFactory, requestWithTimeout)
                    .setApplicationName(APPLICATION_NAME)
                    .build();

        } catch (Exception e) {
            System.err.println("[GoogleCalendar] ⚠️ Error al inicializar: " + e.getMessage() +
                    ". Google Calendar deshabilitado.");
            return null;
        }
    }
}