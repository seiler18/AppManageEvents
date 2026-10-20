package cl.seiler.appmanageevents.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("app")
public record AppProperties(
        String nombreSitio,
        String urlBase,
        String zonaHoraria,
        int minutosReserva,
        Admin admin,
        Correo correo) {

    /** Cuenta administradora que se crea al arrancar si todavía no existe. */
    public record Admin(String email, String password) {
    }

    public record Correo(String remitente) {
    }

    /** URL absoluta, sin la barra final, para correos y para el retorno de Webpay. */
    public String url(String ruta) {
        String base = urlBase.endsWith("/") ? urlBase.substring(0, urlBase.length() - 1) : urlBase;
        return base + ruta;
    }
}
