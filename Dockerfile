# Imagen única para Render hoy y para un VPS (Hostinger u otro) mañana.
# Build multietapa: se compila dentro del contenedor, sin Maven ni JDK en la
# máquina de quien despliega.
FROM maven:3.9-eclipse-temurin-25 AS build
WORKDIR /app

COPY pom.xml .
RUN mvn -B -q dependency:go-offline

COPY src ./src
# Las pruebas corren en CI (GitHub Actions) antes de llegar aquí.
RUN mvn -B -q clean package -DskipTests

FROM eclipse-temurin:25-jre
WORKDIR /app
COPY --from=build /app/target/appmanageevents.jar app.jar

# Usuario sin privilegios: un fallo explotable en la app no da root en el contenedor.
RUN groupadd --system app && useradd --system --gid app --no-create-home app
USER app

# Techo de memoria relativo al contenedor: en un plan de 512 MB, sin esto la
# JVM se pasa y el contenedor muere por OOM.
ENV JAVA_TOOL_OPTIONS="-XX:MaxRAMPercentage=70 -XX:+UseSerialGC -Xss512k -XX:TieredStopAtLevel=1"
ENV SPRING_PROFILES_ACTIVE=prod

EXPOSE 8080
HEALTHCHECK --interval=30s --timeout=5s --start-period=90s --retries=3 \
  CMD ["sh", "-c", "exec 3<>/dev/tcp/127.0.0.1/${PORT:-8080} && printf 'GET /actuator/health HTTP/1.0\\r\\n\\r\\n' >&3 && grep -q '\"UP\"' <&3"]
ENTRYPOINT ["java", "-jar", "app.jar"]
