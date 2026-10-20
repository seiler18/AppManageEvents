# Pendientes manuales

Lo que no se puede hacer desde el código. Borrar cada punto al terminarlo.

- [ ] **Crear el proyecto de Supabase** y copiar las tres variables de la base
      (ver `docs/DESPLIEGUE.md`, paso 1).
- [ ] **Reconectar Render.** El repo se borró y se volvió a crear el
      2026-10-20, así que el servicio `appmanageevents` de Render perdió el
      enlace. Lo más limpio: borrar el servicio viejo y crear uno nuevo con
      **New → Blueprint** (paso 2). La base Neon antigua ya no se usa.
- [ ] **Instalar JDK 25** en el PC de la casa (en la oficina quedó en
      `%USERPROFILE%\.jdks\jdk-25.0.4.1+1`). Para compilar:
      `set JAVA_HOME=...` y `mvnw.cmd verify`.
- [ ] **Probar un pago completo** en integración desde el navegador con la
      tarjeta de prueba del README (el flujo hasta Webpay ya se probó).
- [ ] **SMTP**: crear un buzón (por ejemplo en Hostinger) y cargar las
      variables `MAIL_*` en Render.
- [ ] **Afiliación a Webpay Plus** con Transbank para cobrar en producción
      (paso 4).
- [ ] **Actualizar el CV (Curriculo)**: la ficha
      `dist/projects/AppManageEvents/` y los hitos todavía describen la app
      antigua y el trabajo en equipo.
- [ ] Borrar el respaldo local del historial antiguo si ya no se necesita:
      `C:\JesusSeiler_Personal\_respaldos\AppManageEvents-historial-2026-10-20.bundle`
      (contiene los tokens antiguos del PDF; no subirlo a ningún lado).
