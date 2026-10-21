# AppManageEvents (Java / Spring Boot)

> **Versión archivada.** La app sigue en [Renginys](https://github.com/seiler18/Renginys),
> rehecha en Next.js para correr en el hosting Node.js de Hostinger. Este
> repositorio queda como referencia de la versión en Java 25 y Spring Boot 4.1.

Gestor de eventos con venta de entradas: cartelera pública, registro con
avisos por correo, pago con **Webpay Plus** (Transbank) y panel de
administración para crear eventos con un máximo de entradas.

![Java 25](https://img.shields.io/badge/Java-25_LTS-ED8B00?logo=openjdk&logoColor=white)
![Spring Boot 4.1](https://img.shields.io/badge/Spring_Boot-4.1-6DB33F?logo=springboot&logoColor=white)
![PostgreSQL](https://img.shields.io/badge/Supabase-Postgres_17-3ECF8E?logo=supabase&logoColor=white)
![Webpay Plus](https://img.shields.io/badge/Pagos-Webpay_Plus-D5004A)

## Qué hace

**Público**
- Cartelera de próximos eventos y ficha de cada evento con cupos disponibles.
- Registro e inicio de sesión. Al registrarse se elige si recibir avisos de
  nuevos eventos; cada correo trae un enlace de baja.
- Compra de entradas con Webpay Plus (débito, crédito, prepago). Los eventos
  gratuitos emiten la entrada sin pasar por el pago.
- «Mis entradas»: cada entrada con su **código QR** de un solo uso.

**Correos automáticos**: bienvenida, confirmación de compra con los códigos,
aviso de evento nuevo a los suscritos, recordatorio 24 h antes, aviso de
cancelación y de reembolso. El administrador puede escribir un mensaje a
quienes compraron o a todos los suscritos.

**Panel de administración** (`/admin`)
- Métricas: eventos a la venta, entradas vendidas, recaudación, suscritos.
- Eventos: crear, editar, publicar (dispara el aviso), cancelar, eliminar.
  Cada evento tiene **máximo de entradas**, máximo por compra, precio en CLP,
  salón e imagen.
- Salones con su capacidad: el máximo de un evento no puede superarla.
- Órdenes con filtro por estado y **reembolso en Webpay** (una o todas las de
  un evento cancelado).
- **Validación en la puerta**: se escanea el QR con el teléfono y se registra
  el ingreso; una entrada no pasa dos veces.
- Usuarios y roles. Exportación CSV de asistentes.

**API pública** de solo lectura en `/api/eventos`, documentada en `/api/docs`.

## Cómo protege el cupo

Cada compra bloquea la fila del evento (`SELECT … FOR UPDATE`) antes de
mirar cuántas entradas quedan, así que dos compras simultáneas del último
cupo no pueden venderlo dos veces. Además, la base tiene una restricción
`entradas_reservadas <= cupo_maximo` como última defensa. Una compra
pendiente retiene sus entradas 20 minutos; si no se paga, vuelven a la venta
solas. Si alguien paga después de que su reserva venció y ya no hay cupo, la
app le devuelve el dinero automáticamente.

## Stack

| Pieza | Versión |
|---|---|
| Java | 25 LTS |
| Spring Boot | 4.1 (Spring 7, Security 7, Hibernate 7, Tomcat 11) |
| Vistas | Thymeleaf + Bootstrap 5.3, tema claro/oscuro |
| Base de datos | PostgreSQL 17 en Supabase · H2 en local |
| Migraciones | Flyway |
| Pagos | Webpay Plus, API REST v1.2 |
| Pruebas | JUnit 6, Mockito, MockMvc, Postgres 17 embebido |

## Levantarlo en local

Requiere JDK 25. No hace falta nada más: usa H2 en memoria, carga datos de
demo y Webpay queda en el ambiente de integración de Transbank.

```bash
./mvnw spring-boot:run          # Windows: mvnw.cmd spring-boot:run
```

Abre <http://localhost:8080>:

| Cuenta | Correo | Contraseña |
|---|---|---|
| Administración | `admin@demo.cl` | `admin12345` |
| Usuario | `usuario@demo.cl` | `usuario12345` |

Para pagar en integración usa las tarjetas de prueba de Transbank, por
ejemplo VISA `4051 8856 0044 6623`, CVV `123`, cualquier fecha futura; en la
autenticación, RUT `11.111.111-1` y clave `123`.

```bash
./mvnw verify                   # 34 pruebas, incluida una contra Postgres 17 real
```

## Configuración

Todo se configura con variables de entorno; en el repo no hay credenciales.

| Variable | Para qué |
|---|---|
| `SPRING_PROFILES_ACTIVE=prod` | Activa Postgres y cookies seguras |
| `SPRING_DATASOURCE_URL` / `_USERNAME` / `_PASSWORD` | Conexión a Supabase |
| `APP_URL_BASE` | URL pública (Webpay vuelve a ella, los correos la enlazan) |
| `ADMIN_EMAIL` / `ADMIN_PASSWORD` | Crea la primera cuenta admin al arrancar |
| `TRANSBANK_AMBIENTE` | `integracion` (por defecto) o `produccion` |
| `TRANSBANK_CODIGO_COMERCIO` / `TRANSBANK_API_KEY` | Los entrega Transbank al afiliarte |
| `MAIL_HOST` / `MAIL_PORT` / `MAIL_USERNAME` / `MAIL_PASSWORD` / `MAIL_FROM` | SMTP; sin `MAIL_HOST` los correos solo van al log |

## Despliegue

- **Hoy:** Render (`render.yaml`) + Postgres de Supabase. Ver
  [docs/DESPLIEGUE.md](docs/DESPLIEGUE.md).
- **Más adelante:** el mismo `Dockerfile` corre en un VPS de Hostinger; los
  pasos están en la misma guía. El hosting web compartido de Hostinger no
  ejecuta Java, por eso la ruta es VPS.

## Licencia

© 2026 Jesús Seiler. Todos los derechos reservados.
