package com.utn.space.venueaapi.config;

import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.calendar.Calendar;
import com.google.api.services.calendar.CalendarScopes;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.security.GeneralSecurityException;
import java.util.Collections;

@Configuration
public class GoogleCalendarConfig {

    // Nombre identificativo de tu aplicación para las auditorías en la consola de Google
    private static final String APPLICATION_NAME = "VenueAPI";
    // Ruta del archivo de claves privadas de la Cuenta de Servicio dentro de src/main/resources
    private static final String CREDENTIALS_FILE_PATH = "google-credentials.json";

    @Bean // Expone la instancia para que pueda ser inyectada con @Autowired o mediante constructor
    public Calendar googleCalendarClient() throws GeneralSecurityException, IOException {
        try {
            // Inicializa un canal de transporte HTTP robusto y cifrado compatible con Google
            final HttpTransport httpTransport = GoogleNetHttpTransport.newTrustedTransport();
            // Inicializa la factoría JSON nativa de Google para transformar objetos Java a texto JSON
            final GsonFactory jsonFactory = GsonFactory.getDefaultInstance();

            // Abre el archivo físico JSON de credenciales desde el Classpath de la aplicación
            ClassPathResource resource = new ClassPathResource(CREDENTIALS_FILE_PATH);
            if (!resource.exists()) {
                throw new FileNotFoundException("CRÍTICO: El archivo '" + CREDENTIALS_FILE_PATH +
                        "' no se encuentra en la ruta src/main/resources/. Asegúrate de haberlo descargado de Google Cloud Console.");
            }
            InputStream in = resource.getInputStream();

            // Parsea el archivo y genera un objeto credential mapeando el Scope necesario para gestionar calendarios
            GoogleCredential credential = GoogleCredential.fromStream(in)
                    .createScoped(Collections.singleton(CalendarScopes.CALENDAR));

            if (credential.getServiceAccountId() == null) {
                throw new IllegalArgumentException("CRÍTICO: El archivo JSON provisto no corresponde a una Cuenta de Servicio (Service Account). " +
                        "Asegúrate de generar una clave tipo JSON desde el apartado IAM de Google Cloud.");
            }

            // Construye y retorna la instancia del cliente oficial de Google Calendar v3 configurado
            return new Calendar.Builder(httpTransport, jsonFactory, credential)
                    .setApplicationName(APPLICATION_NAME)
                    .build();
        } catch (FileNotFoundException e) {
            System.err.println(e.getMessage());
            throw e;
        } catch (Exception e) {
            System.err.println("CRÍTICO: Error interno al inicializar Google Calendar Client. Verifique que el JSON no esté vacío ni corrupto. Detalle: " + e.getMessage());
            throw e;
        }
    }
}
