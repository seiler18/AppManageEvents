package cl.seiler.appmanageevents.servicio;

import cl.seiler.appmanageevents.config.AppProperties;
import cl.seiler.appmanageevents.dominio.EstadoOrden;
import cl.seiler.appmanageevents.dominio.Evento;
import cl.seiler.appmanageevents.dominio.Orden;
import cl.seiler.appmanageevents.dominio.Usuario;
import cl.seiler.appmanageevents.repositorio.EventoRepositorio;
import cl.seiler.appmanageevents.repositorio.OrdenRepositorio;
import cl.seiler.appmanageevents.repositorio.UsuarioRepositorio;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionalEventListener;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Correos que salen solos. Cada aviso corre en segundo plano y después del
 * commit que lo provocó: el comprador no espera al servidor de correo, y no
 * se avisa nada que luego se deshizo.
 */
@Service
public class NotificacionServicio {

    private static final Logger log = LoggerFactory.getLogger(NotificacionServicio.class);

    public enum Destinatarios { SUSCRITOS, COMPRADORES }

    private final CorreoServicio correo;
    private final UsuarioRepositorio usuarios;
    private final EventoRepositorio eventos;
    private final OrdenRepositorio ordenes;
    private final AppProperties app;
    private final Clock reloj;

    public NotificacionServicio(CorreoServicio correo, UsuarioRepositorio usuarios, EventoRepositorio eventos,
                                OrdenRepositorio ordenes, AppProperties app, Clock reloj) {
        this.correo = correo;
        this.usuarios = usuarios;
        this.eventos = eventos;
        this.ordenes = ordenes;
        this.app = app;
        this.reloj = reloj;
    }

    @Async
    @TransactionalEventListener(fallbackExecution = true)
    @Transactional(propagation = Propagation.REQUIRES_NEW, readOnly = true)
    public void alRegistrarse(Sucesos.UsuarioRegistrado s) {
        usuarios.findById(s.usuarioId()).ifPresent(u ->
                correo.enviar(u.getEmail(), "Te damos la bienvenida a " + app.nombreSitio(), "bienvenida",
                        Map.of("usuario", u, "urlBaja", urlBaja(u))));
    }

    @Async
    @TransactionalEventListener(fallbackExecution = true)
    @Transactional(propagation = Propagation.REQUIRES_NEW, readOnly = true)
    public void alPagar(Sucesos.OrdenPagada s) {
        ordenes.findById(s.ordenId()).ifPresent(o -> {
            Usuario u = o.getUsuario();
            correo.enviar(u.getEmail(), "Tus entradas para " + o.getEvento().getTitulo(), "compra",
                    Map.of("usuario", u, "orden", o, "evento", o.getEvento(), "entradas", o.getEntradas(),
                            "urlEntradas", app.url("/cuenta/entradas")));
        });
    }

    @Async
    @TransactionalEventListener(fallbackExecution = true)
    @Transactional(propagation = Propagation.REQUIRES_NEW, readOnly = true)
    public void alReembolsar(Sucesos.OrdenReembolsada s) {
        ordenes.findById(s.ordenId()).ifPresent(o ->
                correo.enviar(o.getUsuario().getEmail(), "Reembolso de tu compra " + o.getCodigo(), "reembolso",
                        Map.of("usuario", o.getUsuario(), "orden", o, "evento", o.getEvento())));
    }

    @Async
    @TransactionalEventListener(fallbackExecution = true)
    @Transactional(propagation = Propagation.REQUIRES_NEW, readOnly = true)
    public void alPublicar(Sucesos.EventoPublicado s) {
        eventos.buscarConSalon(s.eventoId()).ifPresent(e -> {
            List<Usuario> suscritos = usuarios.findByRecibeNotificacionesTrue();
            int enviados = 0;
            for (Usuario u : suscritos) {
                if (correo.enviar(u.getEmail(), "Nuevo evento: " + e.getTitulo(), "nuevo-evento",
                        Map.of("usuario", u, "evento", e, "urlEvento", urlEvento(e), "urlBaja", urlBaja(u)))) {
                    enviados++;
                }
            }
            log.info("Aviso de «{}» enviado a {} de {} suscritos", e.getTitulo(), enviados, suscritos.size());
        });
    }

    @Async
    @TransactionalEventListener(fallbackExecution = true)
    @Transactional(propagation = Propagation.REQUIRES_NEW, readOnly = true)
    public void alCancelar(Sucesos.EventoCancelado s) {
        eventos.buscarConSalon(s.eventoId()).ifPresent(e -> {
            for (Usuario u : compradores(e.getId())) {
                correo.enviar(u.getEmail(), "Evento cancelado: " + e.getTitulo(), "cancelado",
                        Map.of("usuario", u, "evento", e));
            }
        });
    }

    /** Mensaje escrito desde el panel, a los suscritos o a quienes compraron. */
    @Async
    @Transactional(readOnly = true)
    public void enviarMensaje(Long eventoId, String asunto, String texto, Destinatarios a) {
        Evento e = eventos.buscarConSalon(eventoId).orElseThrow();
        Collection<Usuario> lista = a == Destinatarios.COMPRADORES
                ? compradores(eventoId)
                : usuarios.findByRecibeNotificacionesTrue();
        for (Usuario u : lista) {
            correo.enviar(u.getEmail(), asunto, "mensaje", Map.of("usuario", u, "evento", e, "texto", texto,
                    "urlEvento", urlEvento(e), "urlBaja", urlBaja(u)));
        }
        log.info("Mensaje «{}» enviado a {} destinatarios", asunto, lista.size());
    }

    /** Cada hora, en el minuto 5: recuerda los eventos de las próximas 24 horas. */
    @Scheduled(cron = "0 5 * * * *", zone = "America/Santiago")
    @Transactional
    public void recordatorios() {
        LocalDateTime ahora = LocalDateTime.now(reloj);
        for (Evento e : eventos.pendientesDeRecordatorio(ahora, ahora.plusHours(24))) {
            for (Usuario u : compradores(e.getId())) {
                correo.enviar(u.getEmail(), "Mañana: " + e.getTitulo(), "recordatorio",
                        Map.of("usuario", u, "evento", e, "urlEntradas", app.url("/cuenta/entradas")));
            }
            e.setRecordatorioEnviado(true);
        }
    }

    private Collection<Usuario> compradores(Long eventoId) {
        Map<Long, Usuario> unicos = new LinkedHashMap<>();
        for (Orden o : ordenes.findByEventoIdAndEstado(eventoId, EstadoOrden.PAGADA)) {
            unicos.putIfAbsent(o.getUsuario().getId(), o.getUsuario());
        }
        return unicos.values();
    }

    private String urlEvento(Evento e) {
        return app.url("/eventos/" + e.getId());
    }

    private String urlBaja(Usuario u) {
        return app.url("/notificaciones/baja?t=" + u.getTokenBaja());
    }
}
