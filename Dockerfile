# --- ETAPA 1: Construcción (Builder) ---
FROM maven:3.9.6-eclipse-temurin-17 AS builder
WORKDIR /app

# Copiamos solo el pom.xml primero. 
# Esto es una mejor práctica para aprovechar la caché de Docker y no descargar 
# las dependencias de internet cada vez que cambias una línea de código.
COPY pom.xml .
RUN mvn dependency:go-offline

# Ahora copiamos el código fuente y compilamos el .jar omitiendo los tests
COPY src ./src
RUN mvn clean package -DskipTests

# --- ETAPA 2: Producción (Runtime) ---
# Usamos la versión "alpine" (Linux minimalista) solo con JRE, pesará ~150MB en lugar de 800MB
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app

# Copiamos el archivo .jar ya compilado desde la etapa "builder"
COPY --from=builder /app/target/*.jar app.jar

# Exponemos el puerto que usa Spring Boot
EXPOSE 8080

# Comando para iniciar la aplicación
ENTRYPOINT ["java", "-jar", "app.jar"]