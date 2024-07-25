# Usa una imagen base de Java 17
FROM openjdk:17-jdk-slim

# Añade un volumen apuntando a /tmp
VOLUME /tmp

# Añade el JAR de la aplicación al contenedor
ARG JAR_FILE=target/appmanageevents-0.0.1-RELEASE.jar
COPY ${JAR_FILE} appmanageevents-0.0.1-RELEASE.jar


#Este corresponde al segundo puerto que se expone 8080:*8080*
# Expone el puerto de la aplicación
EXPOSE 8080

# Ejecuta la aplicación
ENTRYPOINT ["java", "-jar", "appmanageevents-0.0.1-RELEASE.jar"]
