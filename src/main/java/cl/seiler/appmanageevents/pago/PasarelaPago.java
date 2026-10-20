package cl.seiler.appmanageevents.pago;

/**
 * Lo que la app necesita de una pasarela. Hoy la implementa Webpay Plus; las
 * pruebas la reemplazan por un doble para no salir a internet.
 */
public interface PasarelaPago {

    TransaccionIniciada crear(String ordenCompra, String idSesion, int monto, String urlRetorno);

    /** Confirma (commit) la transacción. Webpay la anula sola si no se confirma. */
    ResultadoPago confirmar(String token);

    ResultadoPago consultar(String token);

    void reembolsar(String token, int monto);

    record TransaccionIniciada(String token, String url) {
    }

    record ResultadoPago(
            String estado,
            Integer codigoRespuesta,
            Integer monto,
            String ordenCompra,
            String idSesion,
            String codigoAutorizacion,
            String tipoPago,
            Integer cuotas,
            String ultimos4) {

        /** Webpay solo cobra cuando el estado es AUTHORIZED y el código es 0. */
        public boolean aprobado() {
            return "AUTHORIZED".equals(estado) && codigoRespuesta != null && codigoRespuesta == 0;
        }
    }
}
