# ==========================================
# Paso 1: Compilación (Build Stage)
# ==========================================
FROM maven:3.8.8-eclipse-temurin-17 AS build
WORKDIR /app

# Buena práctica: Copiar primero el pom.xml para descargar las dependencias.
# Esto aprovecha la caché de Docker y acelera las compilaciones futuras.
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Copiar el código fuente del proyecto
COPY src ./src

# Compilar el proyecto omitiendo los tests para acelerar el despliegue
RUN mvn clean package -DskipTests

# ==========================================
# Paso 2: Imagen de Ejecución (Runtime Stage)
# ==========================================
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app

# Copiar el archivo .jar compilado en la etapa anterior de forma explícita
COPY --from=build /app/target/*.jar app.jar

# Exponer el puerto estándar en el que corre tu aplicación de Spring Boot
EXPOSE 8080

# Configurar variables de entorno optimizadas para contenedores
ENV JAVA_OPTS="-XX:+UseG1GC -XX:+ExitOnOutOfMemoryError"

# Comando de ejecución seguro que incluye las opciones de la JVM
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]