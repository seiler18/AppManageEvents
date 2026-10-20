package cl.seiler.appmanageevents.servicio;

/** Error pensado para mostrarse tal cual al usuario: el mensaje ya está redactado para él. */
public class ReglaNegocioException extends RuntimeException {

    public ReglaNegocioException(String mensaje) {
        super(mensaje);
    }
}
