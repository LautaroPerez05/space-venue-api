package com.utn.space.venueaapi.service;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.auth.oauth2.TokenResponse;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.calendar.CalendarScopes;
import com.utn.space.venueaapi.exceptions.IdNotFoundException;
import com.utn.space.venueaapi.model.Consumer;
import com.utn.space.venueaapi.model.GoogleOAuthToken;
import com.utn.space.venueaapi.repository.GoogleOAuthTokenRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.google.api.client.http.GenericUrl;
import com.google.api.client.http.HttpRequest;
import com.google.api.client.json.JsonFactory;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class GoogleOAuth2Service {

    private final GoogleOAuthTokenRepository googleOAuthTokenRepository;
    private final ConsumerService consumerService;

    @Value("${google.oauth2.client-id}")
    private String clientId;

    @Value("${google.oauth2.client-secret}")
    private String clientSecret;

    @Value("${google.oauth2.redirect-uri}")
    private String redirectUri;

    private static final List<String> SCOPES = Arrays.asList(
            CalendarScopes.CALENDAR,
            "https://www.googleapis.com/auth/userinfo.email",
            "https://www.googleapis.com/auth/userinfo.profile"
    );


    /**
     * Genera la URL de autorización que el usuario debe visitar
     */
    public String generateAuthorizationUrl() throws GeneralSecurityException, IOException {
        // Validación: si no están configuradas las credenciales, informar claramente
        if (clientId == null || clientId.isBlank() || clientSecret == null || clientSecret.isBlank()) {
            throw new IllegalStateException("Google OAuth2 no está configurado. Configure GOOGLE_OAUTH2_CLIENT_ID y GOOGLE_OAUTH2_CLIENT_SECRET.");
        }

        HttpTransport httpTransport = GoogleNetHttpTransport.newTrustedTransport();
        GsonFactory jsonFactory = GsonFactory.getDefaultInstance();

        GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.Builder(
                httpTransport,
                jsonFactory,
                clientId,
                clientSecret,
                SCOPES)
                .setAccessType("offline")
                .setApprovalPrompt("force")
                .build();

        return flow.newAuthorizationUrl()
                .setRedirectUri(redirectUri)
                .build();
    }

    /**
     * Intercambia el código de autorización por un token de acceso
     */
    @Transactional
    public void exchangeCodeForToken(String authCode, Integer consumerId) throws GeneralSecurityException, IOException {
        HttpTransport httpTransport = GoogleNetHttpTransport.newTrustedTransport();
        GsonFactory jsonFactory = GsonFactory.getDefaultInstance();

        TokenResponse tokenResponse = new com.google.api.client.auth.oauth2.AuthorizationCodeTokenRequest(
                httpTransport,
                jsonFactory,
                new com.google.api.client.http.GenericUrl("https://oauth2.googleapis.com/token"),
                authCode)
                .setRedirectUri(redirectUri)
                .setClientAuthentication(new com.google.api.client.http.BasicAuthentication(clientId, clientSecret))
                .execute();

        String accessToken = tokenResponse.getAccessToken();
        String refreshToken = tokenResponse.getRefreshToken();
        String googleEmail = obtenerGoogleEmailReal(accessToken, httpTransport, jsonFactory);

        if (googleEmail == null || googleEmail.isBlank()) {
            throw new IllegalStateException("No se pudo recuperar el email real de la cuenta de Google. Verifica los permisos.");
        }

        GoogleOAuthToken oauthToken = googleOAuthTokenRepository
                .findByConsumer_IdConsumer(consumerId)
                .orElse(new GoogleOAuthToken());

        oauthToken.setConsumer(consumerService.findById(consumerId));
        oauthToken.setAccessToken(accessToken);
        if (refreshToken != null) {
            oauthToken.setRefreshToken(refreshToken);
        }
        oauthToken.setGoogleEmail(googleEmail);
        oauthToken.setIsActive(true);

        googleOAuthTokenRepository.save(oauthToken);
        log.info("✅ Token OAuth2 guardado exitosamente para el consumidor ID: {}", consumerId);
    }

    /**
     * Obtiene un Credential válido para hacer llamadas a la API de Calendar
     */
    public Credential getCredential(Integer consumerId) throws GeneralSecurityException, IOException {
        GoogleOAuthToken oauthToken = googleOAuthTokenRepository
                .findByConsumer_IdConsumer(consumerId)
                .orElseThrow(() -> new IdNotFoundException("GoogleOAuthToken para consumidor", consumerId));

        if (!oauthToken.getIsActive()) {
            throw new IllegalStateException("El token de Google para el usuario no está activo");
        }

        // Si el token va a expirar pronto, lo refrescamos internamente primero
        if (oauthToken.getExpiresAt() != null && LocalDateTime.now().isAfter(oauthToken.getExpiresAt().minusMinutes(5))) {
            refreshAccessTokenInternal(consumerId);
            oauthToken = googleOAuthTokenRepository
                    .findByConsumer_IdConsumer(consumerId)
                    .orElseThrow(() -> new IdNotFoundException("GoogleOAuthToken para consumidor", consumerId));
        }

        HttpTransport httpTransport = GoogleNetHttpTransport.newTrustedTransport();
        GsonFactory jsonFactory = GsonFactory.getDefaultInstance();

        // CORRECCIÓN AQUÍ: Usar el Builder explícito con todos los parámetros obligatorios que pide el error
        com.google.api.client.auth.oauth2.Credential credential = new com.google.api.client.auth.oauth2.Credential.Builder(
                com.google.api.client.auth.oauth2.BearerToken.authorizationHeaderAccessMethod())
                .setTransport(httpTransport)
                .setJsonFactory(jsonFactory)
                .setTokenServerUrl(new com.google.api.client.http.GenericUrl("https://oauth2.googleapis.com/token"))
                .setClientAuthentication(new com.google.api.client.http.BasicAuthentication(clientId, clientSecret))
                .build();

        // Seteamos los tokens correspondientes
        credential.setAccessToken(oauthToken.getAccessToken());
        if (oauthToken.getRefreshToken() != null && !oauthToken.getRefreshToken().isBlank()) {
            credential.setRefreshToken(oauthToken.getRefreshToken());
        }

        if (oauthToken.getExpiresAt() != null) {
            long expiresMillis = oauthToken.getExpiresAt().atZone(java.time.ZoneId.systemDefault()).toInstant().toEpochMilli();
            // Le restamos el tiempo actual para calcular el tiempo restante en segundos (exigido por Google de forma interna)
            long remainingSeconds = (expiresMillis - System.currentTimeMillis()) / 1000;
            credential.setExpiresInSeconds(remainingSeconds > 0 ? remainingSeconds : 3600L);
        }

        return credential;
    }

    /**
     * Metodo interno para refrescar el token (sin @Transactional para evitar warning)
     */
    private void refreshAccessTokenInternal(Integer consumerId) throws GeneralSecurityException, IOException {
        GoogleOAuthToken oauthToken = googleOAuthTokenRepository
                .findByConsumer_IdConsumer(consumerId)
                .orElseThrow(() -> new IdNotFoundException("GoogleOAuthToken para consumidor", consumerId));

        if (oauthToken.getRefreshToken() == null) {
            throw new IllegalStateException("No hay refresh token disponible en la base de datos.");
        }

        HttpTransport httpTransport = GoogleNetHttpTransport.newTrustedTransport();
        GsonFactory jsonFactory = GsonFactory.getDefaultInstance();

        // 2. CORRECCIÓN CLAVE: Usar la clase adecuada (RefreshTokenRequest) con su Builder explícito
        TokenResponse tokenResponse = new com.google.api.client.auth.oauth2.RefreshTokenRequest(
                httpTransport,
                jsonFactory,
                new com.google.api.client.http.GenericUrl("https://oauth2.googleapis.com/token"),
                oauthToken.getRefreshToken())
                .setClientAuthentication(new com.google.api.client.http.BasicAuthentication(clientId, clientSecret))
                .execute();

        oauthToken.setAccessToken(tokenResponse.getAccessToken());
        oauthToken.setExpiresAt(LocalDateTime.now().plusSeconds(tokenResponse.getExpiresInSeconds() != null ? tokenResponse.getExpiresInSeconds() : 3600));
        oauthToken.setUpdatedAt(LocalDateTime.now());

        googleOAuthTokenRepository.save(oauthToken);
        log.info("🔄 Token de acceso refrescado correctamente para el consumidor ID: {}", consumerId);
    }

    /**
     * Extra (Opcional pero muy recomendado): Recupera el email real de la cuenta de Google vinculada
     */
    private String obtenerGoogleEmailReal(String accessToken, HttpTransport transport, JsonFactory jsonFactory) {
        try {
            // Endpoint oficial de Google para obtener información del usuario conectado
            GenericUrl url = new GenericUrl("https://www.googleapis.com/oauth2/v2/userinfo");

            HttpRequest request = transport.createRequestFactory().buildGetRequest(url);
            request.getHeaders().setAuthorization("Bearer " + accessToken);

            String responseBody = request.execute().parseAsString();

            // Parseamos el JSON de respuesta para extraer el campo "email"
            JsonObject jsonObject = JsonParser.parseString(responseBody).getAsJsonObject();
            if (jsonObject.has("email")) {
                return jsonObject.get("email").getAsString();
            }
        } catch (IOException e) {
            System.err.println("Error al obtener el email real de Google: " + e.getMessage());
        }
        return null;
    }

    /**
     * Desactiva el token OAuth2
     */
    @Transactional
    public void revokeToken(Integer consumerId) {
        Optional<GoogleOAuthToken> oauthToken = googleOAuthTokenRepository.findByConsumer_IdConsumer(consumerId);
        if (oauthToken.isPresent()) {
            GoogleOAuthToken token = oauthToken.get();
            token.setIsActive(false);
            token.setUpdatedAt(LocalDateTime.now());
            googleOAuthTokenRepository.save(token);
            log.info("🔐 Token OAuth2 revocado para el consumidor ID: {}", consumerId);
        }
    }

    /**
     * Verifica si el usuario tiene un token OAuth2 válido
     */
    public boolean hasValidToken(Integer consumerId) {
        Optional<GoogleOAuthToken> oauthToken = googleOAuthTokenRepository.findByConsumer_IdConsumer(consumerId);
        return oauthToken.isPresent() && oauthToken.get().getIsActive();
    }

    /**
     * Obtiene la información del token OAuth2 del usuario
     */
    public Optional<GoogleOAuthToken> getTokenInfo(Integer consumerId) {
        return googleOAuthTokenRepository.findByConsumer_IdConsumer(consumerId);
    }
}

