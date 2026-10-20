# AppManageEvents

Gestor de eventos hecho con Java 17 y Spring Boot 3: eventos, salones,
cuentas de usuario y panel de administración.

## Stack

| Pieza | Qué se usa |
|---|---|
| Backend | Spring Boot 3, Java 17 |
| Vistas | Thymeleaf + Spring Security 6 |
| Persistencia | Spring Data JPA · H2 (local) · PostgreSQL (producción) |
| API docs | springdoc-openapi |
| Contenedor | Dockerfile multietapa, usuario sin privilegios |

## Levantarlo en local

```bash
./mvnw clean package
./mvnw spring-boot:run        # http://localhost:8083
```

En Windows, `mvnw.cmd` en lugar de `./mvnw`.

## Licencia

© 2026 Jesús Seiler. Todos los derechos reservados.
