package com.utn.space.venueaapi.controllers;

import com.utn.space.venueaapi.security.JwtUtil;
import com.utn.space.venueaapi.service.ConsumerService;
import com.utn.space.venueaapi.service.GoogleOAuth2Service;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/google-oauth2")
@Tag(name = "Google OAuth2", description = "Endpoints para autorizar acceso a Google Calendar")
@RequiredArgsConstructor
public class GoogleOAuth2Controller {

    private final GoogleOAuth2Service googleOAuth2Service;
    private final JwtUtil jwtUtil;
    private final ConsumerService consumerService;

    /**
     * Genera la URL de autorización que el usuario debe visitar
     */
    @GetMapping("/auth-url")
    @PreAuthorize("hasAnyRole('CLIENT', 'ADMIN')")
    @Operation(
            summary = "Obtener URL de autorización de Google",
            description = "Retorna la URL a la que el usuario debe navegar para autorizar el acceso a su calendario"
    )
    public ResponseEntity<Map<String, String>> getAuthorizationUrl(HttpServletRequest request) {
        try {
            String authUrl = googleOAuth2Service.generateAuthorizationUrl();

            // Si el cliente envió su JWT en el header Authorization, lo incorporamos como 'state'
            String bearer = request.getHeader("Authorization");
            if (bearer != null && bearer.startsWith("Bearer ")) {
                String jwt = bearer.substring(7);
                String encoded = java.net.URLEncoder.encode(jwt, java.nio.charset.StandardCharsets.UTF_8);
                if (authUrl.contains("state=")) {
                    // no-op
                } else {
                    authUrl = authUrl + "&state=" + encoded;
                }
            }

            Map<String, String> response = new HashMap<>();
            response.put("authUrl", authUrl);
            return ResponseEntity.ok(response);
        } catch (GeneralSecurityException | IOException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "No se pudo generar la URL de autorización: " + e.getMessage());
            return ResponseEntity.status(500).body(error);
        }
    }

    /**
     * Callback de Google OAuth2 - el usuario es redirigido aquí después de autorizar
     */
    @GetMapping("/callback")
    @Operation(
            summary = "Callback de Google OAuth2",
            description = "Endpoint al que Google redirige después de que el usuario autoriza. " +
                    "Requiere que el usuario esté autenticado en la aplicación (JWT en Authorization header)"
    )
    public ResponseEntity<Map<String, String>> handleCallback(
            @RequestParam(value = "code") String code,
            @RequestParam(value = "state", required = false) String state,
            HttpServletRequest request) {
        try {
            // Preferimos el JWT enviado en el header, pero si Google redirigió sin headers
            // intentamos recuperar el token desde el parámetro 'state' (que incluimos al generar la URL)
            String bearerToken = request.getHeader("Authorization");
            String token = null;
            if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
                token = bearerToken.substring(7);
            } else if (state != null && !state.isBlank()) {
                // state viene URL encoded; lo decodificamos
                token = java.net.URLDecoder.decode(state, java.nio.charset.StandardCharsets.UTF_8);
            }

            if (token == null) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Usuario no autenticado - Token no encontrado ni en header ni en state");
                return ResponseEntity.status(401).body(error);
            }

            Integer consumerId = jwtUtil.extraerConsumerId(token);

            if (consumerId == null) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "No se pudo extraer el consumerId del token");
                return ResponseEntity.status(400).body(error);
            }


            googleOAuth2Service.exchangeCodeForToken(code, consumerId);
            /*
            Map<String, String> response = new HashMap<>();
            response.put("success", "true");
            response.put("message", "Calendario conectado exitosamente");
            return ResponseEntity.ok(response);
            */

            return ResponseEntity.status(HttpStatus.FOUND)
                    .location(java.net.URI.create("http://localhost:8080/space.html?id=" + consumerId))
                    .build();
        } catch (GeneralSecurityException | IOException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Error al procesar la autorización: " + e.getMessage());
            return ResponseEntity.status(500).body(error);
        }
    }

    /**
     * Verifica el estado de autorización del usuario
     */
    @GetMapping("/status")
    @PreAuthorize("hasAnyRole('CLIENT', 'ADMIN')")
    @Operation(
            summary = "Verificar estado de autorización",
            description = "Retorna true/false indicando si el usuario tiene Google Calendar conectado"
    )
    public ResponseEntity<Map<String, Object>> getAuthStatus(HttpServletRequest request) {
        Integer consumerId = extractConsumerIdFromToken(request);
        if (consumerId == null) {
            Map<String, Object> error = new HashMap<>();
            error.put("error", "Usuario no autenticado");
            return ResponseEntity.status(401).body(error);
        }

        Map<String, Object> response = new HashMap<>();
        boolean hasToken = googleOAuth2Service.hasValidToken(consumerId);
        response.put("connected", hasToken);

        if (hasToken) {
            var tokenInfo = googleOAuth2Service.getTokenInfo(consumerId);
            if (tokenInfo.isPresent()) {
                response.put("googleEmail", tokenInfo.get().getGoogleEmail());
                response.put("connectedAt", tokenInfo.get().getCreatedAt());
            }
        }

        return ResponseEntity.ok(response);
    }

    /**
     * Desconecta el calendario de Google del usuario
     */
    @PostMapping("/disconnect")
    @PreAuthorize("hasAnyRole('CLIENT', 'ADMIN')")
    @Operation(
            summary = "Desconectar Google Calendar",
            description = "Desactiva el token de Google Calendar para este usuario"
    )
    public ResponseEntity<Map<String, String>> disconnect(HttpServletRequest request) {
        Integer consumerId = extractConsumerIdFromToken(request);
        if (consumerId == null) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Usuario no autenticado");
            return ResponseEntity.status(401).body(error);
        }

        googleOAuth2Service.revokeToken(consumerId);

        Map<String, String> response = new HashMap<>();
        response.put("success", "true");
        response.put("message", "Google Calendar desconectado exitosamente");
        return ResponseEntity.ok(response);
    }

    /**
     * Helper para extraer el consumerId del JWT del header Authorization
     */
    private Integer extractConsumerIdFromToken(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (bearerToken == null || !bearerToken.startsWith("Bearer ")) {
            return null;
        }

        String token = bearerToken.substring(7);
        return jwtUtil.extraerConsumerId(token);
    }
}


