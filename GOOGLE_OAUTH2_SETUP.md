# Google Calendar OAuth2 - Guía de Configuración

## Resumen

Se implementó **OAuth2** para Google Calendar, permitiendo que los usuarios autoricen la aplicación para:
- Escribir eventos en su calendario personal
- Recibir invitaciones de eventos
- Ver eventos sincronizados automáticamente

## 🔧 Configuración Necesaria

### 1. Crear Proyecto en Google Cloud Console

1. Ir a: https://console.cloud.google.com/
2. Crear un nuevo proyecto (nombre: "Space Venue")
3. Ir a "APIs & Services" → "Enabled APIs & Services"
4. Buscar y habilitar "Google Calendar API"

### 2. Crear Credenciales OAuth2

1. Ir a "APIs & Services" → "Credentials"
2. Click en "Create Credentials" → "OAuth 2.0 Client IDs"
3. Si pide elegir tipo de aplicación, seleccionar "Web application"
4. Configurar:
   - **Name:** Space Venue
   - **Authorized JavaScript origins:**
     - http://localhost
     - http://localhost:8080
     - (En producción: tu dominio)
   - **Authorized redirect URIs:**
     - http://localhost:8080/api/google-oauth2/callback
     - (En producción: https://tudominio.com/api/google-oauth2/callback)

5. Copiar el **Client ID** y **Client Secret**

### 3. Configurar Variables de Entorno

Agregar al archivo `.env` (o en las variables del contenedor):

```ini
GOOGLE_OAUTH2_CLIENT_ID=YOUR_CLIENT_ID_HERE
GOOGLE_OAUTH2_CLIENT_SECRET=YOUR_CLIENT_SECRET_HERE
GOOGLE_OAUTH2_REDIRECT_URI=http://localhost:8080/api/google-oauth2/callback
```

En **producción**, cambiar el redirect URI a tu dominio:
```ini
GOOGLE_OAUTH2_REDIRECT_URI=https://tudominio.com/api/google-oauth2/callback
```

### 4. Reiniciar la Aplicación

```bash
docker compose down
docker compose up --build
```

## 📋 Flujo de Autorización

### Para Usuarios (Clientes)

1. **Navegar a perfil o durante reserva:**
   - Ver botón "📅 Conectar Google Calendar"
   
2. **Hacer click en conectar:**
   - Se abre nueva pestaña con Google
   - Se solicita autorizar acceso a calendario
   
3. **Autorizar:**
   - El usuario acepta los permisos
   - Google redirige de vuelta a la app
   - Token se guarda en la base de datos
   
4. **Verificar conexión:**
   - Botón cambia a "🔌 Desconectar Google Calendar"
   - Muestra email conectado

### Sincronización de Reservas

Cuando se crea una reserva:
- Evento se crea en calendario del espacio (Service Account)
- Si usuario autorizó OAuth2, evento se crea en su calendario personal
- Usuario recibe invitación automática en su email

## 📚 Endpoints API

### Obtener URL de autorización
```http
GET /api/google-oauth2/auth-url
Authorization: Bearer <JWT_TOKEN>
```

Respuesta:
```json
{
  "authUrl": "https://accounts.google.com/o/oauth2/v2/auth?..."
}
```

### Callback (Manejo automático)
```http
GET /api/google-oauth2/callback?code=AUTH_CODE&state=STATE
```

### Verificar estado de conexión
```http
GET /api/google-oauth2/status
Authorization: Bearer <JWT_TOKEN>
```

Respuesta (conectado):
```json
{
  "connected": true,
  "googleEmail": "usuario@gmail.com",
  "connectedAt": "2026-06-20T10:30:00"
}
```

Respuesta (desconectado):
```json
{
  "connected": false
}
```

### Desconectar
```http
POST /api/google-oauth2/disconnect
Authorization: Bearer <JWT_TOKEN>
```

## 🎨 Cómo Integrar en el Frontend

### 1. Incluir el archivo JavaScript

En el HTML que quieras mostrar el botón:

```html
<script src="js/google-oauth2.js"></script>
```

### 2. Crear un contenedor para la UI

```html
<div id="google-calendar-container"></div>
```

### 3. Inicializar la UI (en JavaScript)

```javascript
// Al cargar la página
document.addEventListener('DOMContentLoaded', () => {
    GoogleOAuth2.updateUI('google-calendar-container');
});
```

### Ejemplo: Agregar en perfil de usuario

```html
<div class="card">
    <h2>Calendario</h2>
    <div id="google-calendar-container"></div>
</div>

<script src="js/google-oauth2.js"></script>
<script>
    GoogleOAuth2.updateUI('google-calendar-container');
</script>
```

## 🗂️ Cambios Realizados

### Backend

| Archivo | Cambio |
|---------|--------|
| `model/GoogleOAuthToken.java` | Nueva entidad para guardar tokens OAuth2 |
| `repository/GoogleOAuthTokenRepository.java` | Repositorio para acceder a tokens |
| `service/GoogleOAuth2Service.java` | Servicio para manejar flow OAuth2 |
| `controllers/GoogleOAuth2Controller.java` | Endpoints para autorización |
| `service/GoogleCalendarService.java` | Actualizado para usar OAuth2 + Service Account |
| `security/JwtUtil.java` | Agregar consumerId al JWT |
| `controllers/AuthController.java` | Incluir consumerId al generar token |
| `service/ReservationService.java` | Pasar consumerId al crear eventos |
| `application.properties` | Agregar propiedades de Google OAuth2 |

### Frontend

| Archivo | Cambio |
|---------|--------|
| `js/api.js` | Agregar endpoints de OAuth2 |
| `js/google-oauth2.js` | Nuevo módulo para UI y flujo OAuth2 |

### Base de Datos

Nueva tabla creada automáticamente:
```sql
CREATE TABLE google_oauth_tokens (
    id INT AUTO_INCREMENT PRIMARY KEY,
    id_consumer INT NOT NULL,
    access_token LONGTEXT NOT NULL,
    refresh_token LONGTEXT,
    google_email VARCHAR(255) NOT NULL,
    expires_at TIMESTAMP,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP,
    is_active BOOLEAN NOT NULL DEFAULT true,
    UNIQUE KEY uk_consumer (id_consumer),
    FOREIGN KEY (id_consumer) REFERENCES consumer(idConsumer)
);
```

## 🧪 Pruebas

### Test 1: Autorizar Google Calendar

1. Ir a index.html (o página con el elemento)
2. Click en "Conectar Google Calendar"
3. Permitir acceso en pantalla de Google
4. Verificar que la UI cambie a "Desconectado"

### Test 2: Crear reserva con sincronización

1. Usuario autorizado en Google Calendar
2. Ir a página de reserva
3. Marcar "Guardar en mi calendario"
4. Crear reserva
5. Verificar que el evento aparece en Google Calendar personal

### Test 3: Desconectar

1. Click en "Desconectar Google Calendar"
2. Confirmar
3. Verificar que la UI vuelve a mostrar botón de conectar

## ⚠️ Notas Importantes

- **Refresh Token:** Se obtiene solo la PRIMERA VEZ que el usuario autoriza. Si requiere permisos nuevamente, debes forzar `approvalPrompt=force`
- **Token Expiration:** Los access tokens expiran cada hora. Se refresca automáticamente cuando están a punto de expirar
- **Errores Comunes:**
  - "Invalid redirect URI": Verifica que coincida exactamente en Google Cloud
  - "Invalid client ID": Verifica que copiaste correctamente el Client ID
  - Error 403 en invitaciones: Revisa que la Service Account tenga permisos

## 📞 Soporte

Si hay problemas:
1. Verifica los logs del backend: `docker logs spacevenue-api`
2. Verifica la consola del navegador (F12)
3. Revisa que las variables de entorno estén configuradas correctamente
4. Verifica que el token de Google no esté revocado en https://myaccount.google.com/permissions

