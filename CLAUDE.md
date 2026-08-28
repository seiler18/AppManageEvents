# AppManageEvents

App satélite del portafolio. **No se gobierna desde aquí**: los procedimientos
viven en `Curriculo/.claude/skills/` (`revivir-app-java`, `desplegar-app-java`).
Este archivo existe para que una sesión abierta en esta carpeta sepa qué es
esto, qué parte está viva y qué parte no.

## Qué es

Gestión de eventos y salones (crear, modificar, ver, borrar), con cuentas y
sesión. Viene de un trabajo de equipo de siete personas —Cristopher Vergara,
Erasto Borthomierth, Gabriel Muñoz, Gustavo Andrade, Jesús Seiler, Daniela
Gallardo, Sixto Felipe— así que hay decisiones que **no son mías** y conviene
no reescribirlas sin motivo.

| Pieza | Qué se usa |
|---|---|
| Backend | Spring Boot 3.3.2, Java 17 |
| Vistas | Thymeleaf + `thymeleaf-extras-springsecurity6` |
| Seguridad | Spring Security 6, OAuth2 client, Firebase Admin 8 |
| Persistencia | Spring Data JPA · H2 (local) · PostgreSQL (prod) |
| API docs | springdoc-openapi 2.3 |
| Pruebas | JUnit 5, Mockito 5.7, Selenium 4.10 |
| Deploy | Render (blueprint `render.yaml`) + Postgres en Neon |

## Levantarlo

```bash
mvn clean package
mvn spring-boot:run        # http://localhost:8080
```

La raíz redirige a `/eventos`, que exige sesión. Por eso el `healthCheckPath`
de Render es `/login` y no `/`: un 302 puede contar como servicio caído.

## Qué está vivo y qué es arqueología

Lo que **importa hoy** es `render.yaml` + `Dockerfile`: es lo que sostiene
<https://appmanageevents.onrender.com>.

Lo demás es infraestructura del trabajo original que **ya no se usa** y se
conserva como evidencia del proyecto, no como algo que funcione:

| Archivo | Estado |
|---|---|
| `Jenkinsfile` | histórico — no hay Jenkins corriendo |
| `main.tf`, `terraform.tfstate*`, `.terraform/` | histórico — estado de un provider Docker local |
| `deployment.yaml`, `service.yaml` | histórico — no hay clúster |
| `docker-compose.yml`, `fluentd/` | stack local de SonarQube · Graylog · Grafana |

No lo trates como CI/CD activo ni intentes "arreglarlo". Si algún día se
retira, se anota como hito **de Curriculo**.

## Reglas

1. **`mvn clean package` verde antes de cualquier push.**
2. **Ninguna credencial nueva en el repo.** Es público. Lo sensible va con
   `sync: false` en `render.yaml` y se introduce en el panel de Render.
   → Deuda conocida: `docker-compose.yml` trae contraseñas escritas
   (`GRAYLOG_PASSWORD_SECRET`, `POSTGRES_PASSWORD=sonar`,
   `GF_SECURITY_ADMIN_PASSWORD=admin`). Son de un stack local de desarrollo,
   pero están en un repo público: si ese compose se revive, se rotan primero.
   → Deuda conocida: `.terraform/` versiona 18 MB de binario del provider y
   `terraform.tfstate` está versionado. Revisado: no contiene secretos.
3. **Firebase y OAuth2 vienen del trabajo en equipo.** Si algo de eso falla,
   se desactiva la función antes que inventar una clave.
4. **El enlace vivo se publica desde el CV**, no desde aquí. Si cambia la URL,
   se actualiza en `Curriculo` y se anota en un hito **de Curriculo**.

## Qué NO va aquí

No lleva `.claude/hitos/` ni `.claude/skills/` propios: es nivel B del
estándar de `Desarrollo/CLAUDE.md`. Su memoria y sus procedimientos son los de
`Curriculo`, que es el proyecto que la publica.

## Convenciones

Español en comentarios, commits y documentación. Commits de una línea con el
qué concreto. Los comentarios explican *por qué*, no *qué*.
