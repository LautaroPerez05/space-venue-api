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

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.time.LocalDateTime;
import java.util.Collections;
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
                Collections.singletonList(CalendarScopes.CALENDAR))
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

        // 1. Usamos AuthorizationCodeTokenRequest con el Builder explícito exigido por Google
        TokenResponse tokenResponse = new com.google.api.client.auth.oauth2.AuthorizationCodeTokenRequest(
                httpTransport,
                jsonFactory,
                new com.google.api.client.http.GenericUrl("https://oauth2.googleapis.com/token"),
                authCode)
                .setRedirectUri(redirectUri)
                .setClientAuthentication(new com.google.api.client.http.BasicAuthentication(clientId, clientSecret))
                .execute();

        String accessToken = tokenResponse.getAccessToken();
        // Si el usuario ya dio permisos antes, Google podría no enviar el Refresh Token de nuevo a menos que fuerces la aprobación.
        String refreshToken = tokenResponse.getRefreshToken();
        Long expiresInSeconds = tokenResponse.getExpiresInSeconds();

        // Obtener el email real del usuario autenticado en lugar de hardcodearlo
        String googleEmail = obtenerGoogleEmailReal(accessToken, httpTransport, jsonFactory);
        if (googleEmail == null) {
            googleEmail = "google-user-" + consumerId;
        }

        Consumer consumer = consumerService.findById(consumerId);
        GoogleOAuthToken oauthToken = googleOAuthTokenRepository
                .findByConsumer_IdConsumer(consumerId)
                .orElse(new GoogleOAuthToken());

        oauthToken.setConsumer(consumer);
        oauthToken.setAccessToken(accessToken);

        // Conservar el refresh token antiguo si la respuesta nueva viene vacía (comportamiento por defecto de Google)
        if (refreshToken != null && !refreshToken.isBlank()) {
            oauthToken.setRefreshToken(refreshToken);
        }

        oauthToken.setGoogleEmail(googleEmail);
        oauthToken.setExpiresAt(LocalDateTime.now().plusSeconds(expiresInSeconds != null ? expiresInSeconds : 3600));
        oauthToken.setCreatedAt(oauthToken.getCreatedAt() != null ? oauthToken.getCreatedAt() : LocalDateTime.now());
        oauthToken.setUpdatedAt(LocalDateTime.now());
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

        if (oauthToken.getExpiresAt() != null && LocalDateTime.now().isAfter(oauthToken.getExpiresAt().minusMinutes(5))) {
            refreshAccessTokenInternal(consumerId);
            oauthToken = googleOAuthTokenRepository
                    .findByConsumer_IdConsumer(consumerId)
                    .orElseThrow(() -> new IdNotFoundException("GoogleOAuthToken para consumidor", consumerId));
        }

        // Construir correctamente el Credential usando BearerToken como AccessMethod
        com.google.api.client.auth.oauth2.Credential credential = new com.google.api.client.auth.oauth2.Credential
                .Builder(com.google.api.client.auth.oauth2.BearerToken.authorizationHeaderAccessMethod())
                .setTransport(GoogleNetHttpTransport.newTrustedTransport())
                .setJsonFactory(GsonFactory.getDefaultInstance())
                .build();

        credential.setAccessToken(oauthToken.getAccessToken());
        if (oauthToken.getRefreshToken() != null) credential.setRefreshToken(oauthToken.getRefreshToken());
        if (oauthToken.getExpiresAt() != null) {
            credential.setExpirationTimeMilliseconds(
                    oauthToken.getExpiresAt().atZone(java.time.ZoneId.systemDefault()).toInstant().toEpochMilli());
        }

        return credential;
    }

    /**
     * Método interno para refrescar el token (sin @Transactional para evitar warning)
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
    private String obtenerGoogleEmailReal(String accessToken, HttpTransport transport, GsonFactory factory) {
        try {
            com.google.api.client.http.HttpRequestFactory requestFactory = transport.createRequestFactory(
                    request -> request.getHeaders().setAuthorization("Bearer " + accessToken)
            );
            com.google.api.client.http.GenericUrl url = new com.google.api.client.http.GenericUrl("https://www.googleapis.com/oauth2/v2/userinfo");
            com.google.api.client.http.HttpRequest request = requestFactory.buildGetRequest(url);
            String responseBody = request.execute().parseAsString();

            // Un parseo rápido usando una expresión regular para no añadir librerías adicionales
            java.util.regex.Matcher matcher = java.util.regex.Pattern.compile("\"email\"\\s*:\\s*\"([^\"]+)\"").matcher(responseBody);
            if (matcher.find()) {
                return matcher.group(1);
            }
        } catch (Exception e) {
            log.warn("No se pudo obtener el email real de Google: {}", e.getMessage());
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

