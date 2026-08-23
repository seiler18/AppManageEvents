# Build multietapa: el contenedor se construye desde el codigo fuente, sin
# necesitar un mvn package previo en la maquina de quien despliega.
# El Dockerfile anterior hacia COPY del jar y fallaba si target/ estaba vacio.
FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /app

COPY pom.xml .
RUN mvn -B -q dependency:go-offline

COPY src ./src
# Los tests se saltan aqui a proposito: GoogleSearchTest necesita un Chrome real,
# que no existe en el contenedor. Se corren en local con mvn test.
RUN mvn -B -q clean package -DskipTests

FROM eclipse-temurin:17-jre
WORKDIR /app
COPY --from=build /app/target/appmanageevents-0.0.1-RELEASE.jar app.jar

# 512MB y 0.1 CPU en el plan gratuito: sin techo de heap la JVM se pasa y el
# contenedor muere por OOM.
ENV JAVA_TOOL_OPTIONS="-XX:MaxRAMPercentage=70 -XX:+UseSerialGC -Xss512k"

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
