package cl.seiler.appmanageevents.servicio;

import cl.seiler.appmanageevents.dominio.EstadoEvento;
import cl.seiler.appmanageevents.dominio.EstadoOrden;
import cl.seiler.appmanageevents.dominio.Evento;
import cl.seiler.appmanageevents.dominio.Orden;
import cl.seiler.appmanageevents.repositorio.EventoRepositorio;
import cl.seiler.appmanageevents.repositorio.OrdenRepositorio;
import cl.seiler.appmanageevents.repositorio.UsuarioRepositorio;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;

/** Lecturas del panel de administración. */
@Service
@Transactional(readOnly = true)
public class PanelServicio {

    public record Resumen(long eventosProximos, long entradasVendidas, long recaudacion,
                          long usuarios, long suscritos, List<Evento> proximos) {
    }

    private final EventoRepositorio eventos;
    private final OrdenRepositorio ordenes;
    private final UsuarioRepositorio usuarios;
    private final Clock reloj;

    public PanelServicio(EventoRepositorio eventos, OrdenRepositorio ordenes, UsuarioRepositorio usuarios, Clock reloj) {
        this.eventos = eventos;
        this.ordenes = ordenes;
        this.usuarios = usuarios;
        this.reloj = reloj;
    }

    public Resumen resumen() {
        LocalDateTime ahora = LocalDateTime.now(reloj);
        List<Evento> proximos = eventos.findByEstadoAndFechaInicioAfterOrderByFechaInicioAsc(EstadoEvento.PUBLICADO, ahora);
        return new Resumen(
                eventos.countByEstadoAndFechaInicioAfter(EstadoEvento.PUBLICADO, ahora),
                ordenes.entradasVendidas(),
                ordenes.recaudacionTotal(),
                usuarios.count(),
                usuarios.countByRecibeNotificacionesTrue(),
                proximos.stream().limit(8).toList());
    }

    public List<Orden> ordenesDeEvento(Long eventoId) {
        return ordenes.findByEventoIdOrderByCreadaEnDesc(eventoId);
    }

    public long recaudacionDeEvento(Long eventoId) {
        return ordenes.recaudacionEvento(eventoId);
    }

    public List<Orden> ordenes(EstadoOrden estado) {
        return estado == null
                ? ordenes.findTop200ByOrderByCreadaEnDesc()
                : ordenes.findTop200ByEstadoOrderByCreadaEnDesc(estado);
    }
}
