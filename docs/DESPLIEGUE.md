# Despliegue

## 1. Base de datos en Supabase

1. Crear un proyecto nuevo en <https://supabase.com/dashboard> (región São
   Paulo, `sa-east-1`, la más cercana a Chile). Guardar la contraseña de la
   base en el gestor de contraseñas.
2. En **Connect → Session pooler** copiar host, puerto y usuario. Se usa el
   *session pooler* porque la conexión directa de Supabase es solo IPv6 y
   Render no la alcanza. No usar el *transaction pooler* (puerto 6543): no
   admite sentencias preparadas.
3. Con eso quedan las tres variables:

   ```
   SPRING_DATASOURCE_URL=jdbc:postgresql://aws-0-sa-east-1.pooler.supabase.com:5432/postgres?sslmode=require
   SPRING_DATASOURCE_USERNAME=postgres.<ref-del-proyecto>
   SPRING_DATASOURCE_PASSWORD=<contraseña de la base>
   ```

Las tablas las crea Flyway en el primer arranque. La migración
`V1_1__cerrar_api_supabase.sql` activa RLS en todas las tablas, así que la
API REST automática de Supabase no expone nada aunque la clave `anon` sea
pública.

## 2. App en Render

1. En Render: **New → Blueprint** → elegir el repo `seiler18/AppManageEvents`.
   Render lee `render.yaml` y pide las variables marcadas `sync: false`.
2. Mínimo para arrancar: las tres de la base, `APP_URL_BASE`
   (`https://<servicio>.onrender.com`), `ADMIN_EMAIL` y `ADMIN_PASSWORD`.
3. Al terminar el primer despliegue, entrar con la cuenta admin. Después se
   puede borrar `ADMIN_PASSWORD`: la cuenta ya existe y la variable no
   vuelve a usarse.

El plan gratuito de Render duerme el servicio tras 15 minutos sin visitas.
Mientras duerme no corren las tareas programadas (vencimiento de reservas,
recordatorios); al despertar se ponen al día. Las reservas vencidas de un
evento también se liberan en la siguiente compra de ese evento.

## 3. Correo (SMTP)

Cualquier SMTP sirve. Con un correo de Hostinger:

```
MAIL_HOST=smtp.hostinger.com
MAIL_PORT=587
MAIL_USERNAME=eventos@tudominio.cl
MAIL_PASSWORD=<contraseña del buzón>
MAIL_FROM=eventos@tudominio.cl
```

Sin `MAIL_HOST` la app funciona igual y deja cada correo en el log.

## 4. Webpay Plus en producción

Mientras `TRANSBANK_AMBIENTE` no sea `produccion`, los pagos van al ambiente
de integración: nadie paga de verdad y el pie de página lo avisa.

Para cobrar de verdad:

1. Afiliarse a Webpay Plus en <https://www.transbank.cl> (o con Transbank
   Developers). Transbank entrega el **código de comercio** y, tras el
   proceso de validación, la **API Key secreta**.
2. Transbank pide una transacción de prueba en producción. Se hace con una
   compra real de bajo monto y luego se reembolsa desde el panel.
3. Variables:

   ```
   TRANSBANK_AMBIENTE=produccion
   TRANSBANK_CODIGO_COMERCIO=5970XXXXXXXX
   TRANSBANK_API_KEY=<llave secreta>
   ```

## 5. Más adelante: VPS de Hostinger

El hosting web compartido de Hostinger (donde están los sitios Node.js y
WordPress) no ejecuta Java. Un **VPS de Hostinger** sí, con el mismo
`Dockerfile`:

```bash
# En el VPS (Ubuntu con Docker)
git clone https://github.com/seiler18/AppManageEvents.git && cd AppManageEvents
docker build -t appmanageevents .
docker run -d --name eventos --restart unless-stopped -p 127.0.0.1:8080:8080 \
  --env-file /etc/appmanageevents.env appmanageevents
```

`/etc/appmanageevents.env` lleva las mismas variables de arriba (permisos
`600`, fuera del repo). Delante va Nginx o Caddy con HTTPS apuntando a
`127.0.0.1:8080`; la app ya entiende las cabeceras `X-Forwarded-*`.

La base puede seguir en Supabase o pasar a un Postgres en el mismo VPS: basta
cambiar `SPRING_DATASOURCE_URL`.
