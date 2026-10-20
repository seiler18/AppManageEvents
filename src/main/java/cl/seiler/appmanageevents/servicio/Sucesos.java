package cl.seiler.appmanageevents.servicio;

/**
 * Hechos que disparan correos. Viajan como eventos de Spring y se atienden
 * después del commit: así nunca sale un correo por algo que terminó en rollback.
 */
public final class Sucesos {

    private Sucesos() {
    }

    public record UsuarioRegistrado(Long usuarioId) {
    }

    public record OrdenPagada(Long ordenId) {
    }

    public record OrdenReembolsada(Long ordenId) {
    }

    public record EventoPublicado(Long eventoId) {
    }

    public record EventoCancelado(Long eventoId) {
    }
}
