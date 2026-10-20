package cl.seiler.appmanageevents.dominio;

public enum EstadoOrden {
    /** Entradas retenidas mientras el comprador paga en Webpay. */
    PENDIENTE,
    PAGADA,
    RECHAZADA,
    /** El comprador abandonó el formulario de Webpay. */
    ANULADA,
    /** Venció la reserva sin respuesta de Webpay. */
    EXPIRADA,
    REEMBOLSADA;

    /** Estados que ya no retienen cupo. */
    public boolean liberaCupo() {
        return this != PENDIENTE && this != PAGADA;
    }
}
