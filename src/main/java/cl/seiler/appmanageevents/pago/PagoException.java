package cl.seiler.appmanageevents.pago;

public class PagoException extends RuntimeException {

    public PagoException(String mensaje, Throwable causa) {
        super(mensaje, causa);
    }
}
