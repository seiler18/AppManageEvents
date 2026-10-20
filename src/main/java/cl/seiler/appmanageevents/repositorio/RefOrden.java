package cl.seiler.appmanageevents.repositorio;

/**
 * Identifica una orden y su evento sin cargar la entidad: así el bloqueo de la
 * orden que viene después lee su estado fresco y no el de una copia en caché.
 */
public record RefOrden(Long ordenId, Long eventoId) {
}
