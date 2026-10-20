# AppManageEvents

Gestor de eventos con venta de entradas, avisos por correo, pago con Webpay
Plus y panel de administración. Proyecto personal de Jesús Seiler.
Java 25 · Spring Boot 4.1 · Thymeleaf · Postgres (Supabase) · Flyway.

## Reglas

0. **Ningún commit lleva `Co-Authored-By` ni «Generated with Claude Code».**
   El repo tiene un único autor: Jesús Seiler <ichbinseiler@gmail.com>.
1. **`mvnw verify` verde antes de cualquier push** (en Windows `mvnw.cmd`,
   con `JAVA_HOME` apuntando a un JDK 25). El `./mvnw` de Git Bash falla al
   mover la distribución de Maven en este PC.
2. **Ninguna credencial en el repo.** Es público. Todo entra por variables de
   entorno (ver README). Las credenciales de Webpay que aparecen en
   `application.properties` son las de integración que Transbank publica.
3. **El esquema cambia solo con migraciones Flyway nuevas** (`db/migration`,
   o `db/postgresql` si es sintaxis solo de Postgres). Nunca se edita una
   migración ya aplicada. Hibernate solo valida (`ddl-auto=validate`).
4. **Todo lo que mueve el cupo bloquea primero el evento y después la
   orden**, en ese orden. Ver `CompraServicio`.
5. **Sin JS ni `<style>` en línea**: la CSP lo bloquea. El JS vive en
   `static/js/app.js`.
6. `open-in-view` está apagado: lo que use una vista se carga en la consulta
   (`@EntityGraph`). `WebTest.elPanelCompletoRenderiza` lo vigila.

## Pendientes

Lo manual (Supabase, Render, Transbank, SMTP) está en `PENDIENTES.md`.
Al cerrar un trabajo, actualizarlo.

## Convenciones

Español en código de dominio, comentarios, commits y documentación. Commits
de una línea con el qué concreto. Los comentarios explican *por qué*.
