package cl.seiler.appmanageevents.pago;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Credenciales de Webpay Plus. Por defecto, las del comercio de integración
 * que Transbank publica en su documentación; en producción llegan por
 * variables de entorno con el código y la llave que entrega Transbank al
 * afiliarse.
 */
@ConfigurationProperties("transbank")
public record TransbankProperties(String ambiente, String codigoComercio, String apiKey) {

    public boolean esProduccion() {
        return "produccion".equalsIgnoreCase(ambiente);
    }

    public String urlApi() {
        return esProduccion() ? "https://webpay3g.transbank.cl" : "https://webpay3gint.transbank.cl";
    }
}
