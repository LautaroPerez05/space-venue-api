# Space & Venue 🚀

**Space & Venue** es una plataforma estilo marketplace y sistema de reservas (booking) para el alquiler temporal de espacios. El sistema está diseñado de manera flexible y sin limitaciones de categorías, permitiendo a los usuarios publicar y rentar desde oficinas vacías y salas de reuniones, hasta salones de eventos o habitaciones libres.

Este repositorio contiene la arquitectura completa del proyecto (**Backend** y **Frontend**). Esta guía está estructurada exclusivamente para desarrolladores que deseen clonar, configurar y ejecutar el entorno de desarrollo local de manera rápida y eficiente.

---

## 🛠️ Stack Tecnológico

### Backend
* **Lenguaje:** Java 17
* **Framework:** Spring Boot
* **Mapeo Objeto-Relacional (ORM):** Spring Data JPA / Hibernate
* **Gestor de Dependencias:** Maven
* **Base de Datos:** MySQL

### Frontend
* **Tecnologías:** HTML, CSS, JavaScript (Vanilla)
* **Despliegue Local:** Dockerizado (Contenedor aislado)
* **Despliegue a Producción:** Render para Backend y Frontend, Aiven para Base de Datos MySQL en la nube.

---

## Repositorio del Frontend
https://github.com/FeijooCiro4/space-venue-front

---

## Link del proyecto desplegado
https://space-venue-front.onrender.com

---


## 📋 Requisitos Previos

Antes de comenzar con la instalación, asegúrate de tener instaladas las siguientes herramientas en tu máquina de desarrollo:
* [Java JDK 17](https://www.oracle.com/java/technologies/downloads/)
* [MySQL Server/Workbench](https://dev.mysql.com/downloads/installer/)
* [Docker Desktop](https://www.docker.com/products/docker-desktop/)
* **IDE Recomendado para el Backend:** [IntelliJ IDEA](https://www.jetbrains.com/idea/) (Necesario para una fácil inyección de variables de entorno).

---

## ⚙️ Configuración e Instalación

### 1. Clonar el Repositorio
Abre una terminal y clona el proyecto en tu máquina local:
```bash
git clone https://github.com/FeijooCiro4/space-venue-api.git
cd space-venue
```

### 2. Configurar la Base de Datos
1. Inicia sesión en tu cliente local de MySQL (Workbench, phpMyAdmin o consola).
2. Crea un esquema o base de datos vacía. **Nota importante:** No necesitas importar ningún script de tablas, ya que Hibernate se encargará de mapear y crear automáticamente la estructura y las relaciones gracias a la propiedad `ddl-auto=update`.
```sql
CREATE DATABASE space_venue_db;
```

### 3. Variables de Entorno
El archivo `application.properties` está parametrizado para no exponer credenciales. Las siguientes variables deben ser inyectadas en la configuración de ejecución de tu IDE:

| Variable                     | Descripción / Ejemplo                                                                                                                                                                                                                                          |
|:-----------------------------|:---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| `PORT`                       | Puerto de escucha del servidor backend (Por defecto: `8080`)                                                                                                                                                                                                   |
| `SPRING_DATASOURCE_URL`      | URL de conexión JDBC (Ej: `jdbc:mysql://localhost:3306/space_venue_db`)                                                                                                                                                                                        |
| `SPRING_DATASOURCE_USERNAME` | Usuario de tu instancia local de MySQL (Ej: `root`)                                                                                                                                                                                                            |
| `SPRING_DATASOURCE_PASSWORD` | Contraseña de tu instancia local de MySQL                                                                                                                                                                                                                      |
| `MP_ACCESS_TOKEN`            | Token de acceso para la API de Mercado Pago                                                                                                                                                                                                                    |
| `ID_FOR_GOOGLE_CALENDAR`     | ID del calendario corporativo donde se centralizan y guardan todas las reservas. Actualmente se utiliza un ID fijo (hardcodeado) para unificar los eventos de los clientes en una cuenta de empresa específica. (Ej:`identificador@group.calendar.google.com`) |
| `GOOGLE_CREDENTIALS_BASE64`  | Credenciales de servicio de Google para la API. Corresponde al contenido íntegro del archivo de credenciales .json, pero convertido a un único string en formato Base64 (necesario para el despliegue del proyecto). (Ej: `woglCJ0eXBlIjogInNlcnZpY...`)       |

---

## 🚀 Ejecución del Proyecto

El flujo de trabajo actual requiere mantener el Backend ejecutándose a través de tu IDE y el Frontend corriendo de forma aislada dentro de un contenedor Docker.

### Paso 1: Levantar el Backend (IntelliJ IDEA)
La forma correcta de ejecutar el servidor localmente sin problemas de inyección de propiedades es utilizando IntelliJ IDEA:

1. Abre el proyecto clonado en **IntelliJ IDEA**.
2. Ve al menú superior y selecciona **Run** -> **Edit Configurations...**.
3. Haz clic en el ícono **`+`** (Add New Configuration) y selecciona **Spring Boot** (o **Application** apuntando a tu clase principal que contiene el método `main`).
4. Busca el campo **Environment variables** y añade los pares de clave-valor mencionados en la tabla superior.
5. Aplica los cambios y haz clic en el botón ▶️ **Run** (Mayús+F10). El servidor backend quedará escuchando peticiones en el puerto configurado.

### Paso 2: Levantar el Frontend con Docker
1. Abre una ventana de la línea de comandos (`cmd` o terminal) y navega al directorio del frontend:
```bash
cd ruta/al/directorio/del/frontend
```
2. Ejecuta el comando de Docker correspondiente para levantar el contenedor:
```bash
# Opción con Dockerfile estándar:
docker build -t space-venue-front .
docker run -d -p 80:80 space-venue-front

# Opción si el entorno incluye Docker Compose:
docker-compose up -d
```
3. Una vez iniciado el contenedor, abre Google Chrome e ingresa a `http://localhost` para interactuar con la interfaz.

---

## 📅 Integración con Google Calendar API (Flujo de Sincronización)



El sistema cuenta con un módulo de sincronización automática que registra las reservas confirmadas en un calendario de Google. Dado que el backend opera de forma autónoma (sin intervención de un usuario humano que inicie sesión en su navegador), la integración se diseñó utilizando una **Cuenta de Servicio (Service Account)** de Google Cloud bajo la especificación **OAuth2**.



A continuación, se detalla el flujo de arquitectura y los pasos configurados para que las reservas impacten correctamente en el calendario:



### 1. Configuración en la Consola de Google Cloud (IAM)

* **Creación del Proyecto:** Se dio de alta un proyecto dedicado en Google Cloud Console para gestionar las credenciales de la API.

* **Habilitación de la API:** Se activó la **Google Calendar API v3** para el proyecto.

* **Service Account:** Se creó una cuenta de servicio, la cual actúa como un "usuario robótico" con una identidad propia expresada en un correo electrónico con el siguiente formato:  

  `tu-cuenta-servicio@proyecto.iam.gserviceaccount.com`

* **Descarga de Claves:** Se generó una clave privada en formato **JSON** que contiene el par de llaves criptográficas necesarias para que Spring Boot demuestre su identidad ante Google.



### 2. Estrategia Híbrida de Almacenamiento de Credenciales

Para resguardar la seguridad de las claves privadas y evitar subir el archivo JSON a repositorios públicos de GitHub, el archivo `GoogleCalendarConfig.java` implementa un flujo de lectura inteligente:



* **En Entorno de Desarrollo (Localhost):** El backend busca un archivo físico local llamado `google-credentials.json` dentro de la ruta `src/main/resources/`.

* **En Entorno de Producción (Docker / OnRender):** El contenido completo del archivo JSON se codifica en un string **Base64** y se almacena en la variable de entorno del servidor llamada `GOOGLE_CREDENTIALS_BASE64`. El backend detecta esta variable, la decodifica en memoria y levanta las credenciales de forma 100% segura sin necesidad de archivos físicos.



### 3. Autorización por "Scopes" y Autenticación de Red

Al arrancar el servidor, la clase de configuración ejecuta los siguientes pasos:

1. Inicializa un transporte HTTP seguro (`GoogleNetHttpTransport.newTrustedTransport()`) que cifra los datos de viaje.

2. Mapea un **Scope (Alcance)** restringido mediante `CalendarScopes.CALENDAR`. Esto le avisa a Google que este robot solo solicita permisos de lectura y escritura sobre el calendario, bloqueando el acceso a cualquier otra herramienta (como Gmail o Drive).

3. Construye y expone un componente `@Bean` global del cliente `Calendar` de Google listo para ser inyectado en el código.



### 4. Permisos en el Calendario de Google (Paso Crítico)

Para que el robot de la cuenta de servicio pueda escribir eventos en el calendario personal del dueño de los espacios o de la aplicación, se requiere un paso de delegación de acceso:

1. Se copia la dirección de correo electrónico de la cuenta de servicio (`...-account.com`).

2. Se accede al Google Calendar de destino desde el navegador, se ingresa a la **Configuración del Calendario** y en el apartado **"Compartir con personas específicas"**, se añade el mail del robot otorgándole el permiso de **"Hacer cambios y administrar el uso compartido"**.



### 5. Flujo de Registro de una Reserva (Tiempo de Ejecución)

Cuando un cliente solicita una reserva exitosa desde el Frontend, el backend ejecuta la siguiente secuencia en cascada:



```text

[Frontend: app.js] ──(Petición Reserva)──> [Backend: ReservationService]

                                                    │

                                         (Valida disponibilidad)

                                                    │

                                                    ▼

[Google Calendar] <──(Inyecta Evento v3)─── [GoogleCalendarService]

        │

(Devuelve ID Único)

        │

        ▼

[Base de Datos] ──(Guarda googleCalendarId en tabla 'spaces'/'reservations')
```

---

## 📄 Estructura de Configuración (`application.properties`)
A modo de referencia, esta es la estructura base del Backend:

```properties
spring.application.name=space-venueaapi
server.port=${PORT:8080}
mercadopago.access.token=${MP_ACCESS_TOKEN}
spring.datasource.url=${SPRING_DATASOURCE_URL}
spring.datasource.username=${SPRING_DATASOURCE_USERNAME}
spring.datasource.password=${SPRING_DATASOURCE_PASSWORD}
spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true
spring.jpa.database-platform=org.hibernate.dialect.MySQLDialect
spring.jpa.hibernate.naming.physical-strategy=org.hibernate.boot.model.naming.PhysicalNamingStrategyStandardImpl
spring.jpa.hibernate.naming.implicit-strategy=org.hibernate.boot.model.naming.ImplicitNamingStrategyLegacyHbmImpl
```
