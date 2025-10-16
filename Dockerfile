# ---- Build stage ----
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app

# Copiamos primero el pom.xml para aprovechar la cache de dependencias
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Copiamos el código fuente
COPY src ./src

# Construimos el jar (sin tests)
RUN mvn -B clean package -DskipTests

# ---- Runtime stage ----
FROM eclipse-temurin:21-jre
WORKDIR /app

# Variables de entorno
ENV SERVER_PORT=8080
EXPOSE 8080

# Copiamos el jar desde la etapa de build
COPY --from=build /app/target/*.jar app.jar

# Lanzamos la aplicación
ENTRYPOINT ["java", "-jar", "app.jar"]
