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
git clone https://github.com/tu-usuario/space-venue.git
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
