package com.utn.space.venueaapi.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;
import java.security.Key;
import java.util.Date;

@Component
public class JwtUtil {

    // Genera automáticamente una clave criptográfica de 256 bits apta para el algoritmo HS256
    private final Key CLAVE_SECRETA = Keys.secretKeyFor(SignatureAlgorithm.HS256);

    // Define la vida útil del token en 10 horas expresadas en milisegundos
    private final long TIEMPO_EXPIRACION = 36_000_000;

    // Construye el token JWT empaquetando el nombre de usuario del cliente
    public String generarToken(String username) {
        return Jwts.builder()
                .setSubject(username) // Almacena el identificador principal del usuario (username)
                .setIssuedAt(new Date(System.currentTimeMillis())) // Registra el momento exacto de emisión
                .setExpiration(new Date(System.currentTimeMillis() + TIEMPO_EXPIRACION)) // Define el momento de caducidad
                .signWith(CLAVE_SECRETA) // Firma el contenido para evitar alteraciones maliciosas
                .compact(); // Compacta la estructura jerárquica en un String plano separado por puntos
    }

    // Abre el token y extrae su payload de datos (Claims) usando la firma secreta de control
    public Claims extraerClaims(String token) {
        return Jwts.parser()
                .setSigningKey(CLAVE_SECRETA) // Suministra la clave para comprobar que el token no se modificó en el camino
                .build()
                .parseClaimsJws(token) // Intenta parsear e inspeccionar la firma del token
                .getBody(); // Devuelve el mapa interno de datos si la firma es válida
    }

    // Recupera directamente el nombre de usuario desde el cuerpo del token
    public String extraerUsername(String token) {
        return extraerClaims(token).getSubject(); // El Subject almacena el username
    }

    // Verifica que el token pertenezca al usuario en cuestión y que la fecha actual no supere la de expiración
    public boolean validarToken(String token, String username) {
        final String tokenUsername = extraerUsername(token);
        boolean estaExpirado = extraerClaims(token).getExpiration().before(new Date());
        return (tokenUsername.equals(username) && !estaExpirado); // Retorna verdadero solo si ambas condiciones se cumplen
    }
}

