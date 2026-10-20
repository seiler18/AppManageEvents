package cl.seiler.appmanageevents.servicio;

import cl.seiler.appmanageevents.config.AppProperties;
import cl.seiler.appmanageevents.dominio.*;
import cl.seiler.appmanageevents.pago.PagoException;
import cl.seiler.appmanageevents.pago.PasarelaPago;
import cl.seiler.appmanageevents.pago.PasarelaPago.ResultadoPago;
import cl.seiler.appmanageevents.pago.PasarelaPago.TransaccionIniciada;
import cl.seiler.appmanageevents.repositorio.EventoRepositorio;
import cl.seiler.appmanageevents.repositorio.OrdenRepositorio;
import cl.seiler.appmanageevents.repositorio.RefOrden;
import cl.seiler.appmanageevents.repositorio.UsuarioRepositorio;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Venta de entradas. Toda operación que mueve el cupo de un evento bloquea
 * primero la fila del evento y después la de la orden, siempre en ese orden.
 */
@Service
public class CompraServicio {

    private static final Logger log = LoggerFactory.getLogger(CompraServicio.class);

    private final EventoRepositorio eventos;
    private final OrdenRepositorio ordenes;
    private final UsuarioRepositorio usuarios;
    private final PasarelaPago pasarela;
    private final ApplicationEventPublisher sucesos;
    private final AppProperties app;
    private final Clock reloj;
    private final TransactionTemplate tx;

    public CompraServicio(EventoRepositorio eventos, OrdenRepositorio ordenes, UsuarioRepositorio usuarios,
                          PasarelaPago pasarela, ApplicationEventPublisher sucesos, AppProperties app,
                          Clock reloj, PlatformTransactionManager transacciones) {
        this.eventos = eventos;
        this.ordenes = ordenes;
        this.usuarios = usuarios;
        this.pasarela = pasarela;
        this.sucesos = sucesos;
        this.app = app;
        this.reloj = reloj;
        this.tx = new TransactionTemplate(transacciones);
    }

    /**
     * Retiene las entradas y deja la orden PENDIENTE. Si el evento es gratuito
     * la orden sale ya pagada, con sus entradas emitidas.
     */
    @Transactional
    public Orden reservar(Long usuarioId, Long eventoId, int cantidad) {
        Evento evento = eventos.bloquear(eventoId).orElseThrow(() -> new NoEncontradoException("El evento"));
        Instant ahora = reloj.instant();
        liberarVencidas(evento, ahora);

        if (!evento.estaPublicado()) {
            throw new ReglaNegocioException("Este evento no está a la venta.");
        }
        if (!evento.getFechaInicio().isAfter(LocalDateTime.now(reloj))) {
            throw new ReglaNegocioException("Este evento ya comenzó.");
        }
        if (cantidad < 1 || cantidad > evento.getMaxPorCompra()) {
            throw new ReglaNegocioException("Puedes comprar entre 1 y " + evento.getMaxPorCompra() + " entradas por compra.");
        }
        if (cantidad > evento.getDisponibles()) {
            throw new ReglaNegocioException(evento.getDisponibles() == 0
                    ? "Las entradas se agotaron."
                    : "Solo quedan " + evento.getDisponibles() + " entradas disponibles.");
        }

        evento.setEntradasReservadas(evento.getEntradasReservadas() + cantidad);

        Orden orden = new Orden();
        orden.setCodigo(Codigos.orden(LocalDate.now(reloj)));
        orden.setIdSesion(Codigos.secreto());
        orden.setUsuario(usuarios.getReferenceById(usuarioId));
        orden.setEvento(evento);
        orden.setCantidad(cantidad);
        orden.setPrecioUnitario(evento.getPrecio());
        orden.setMonto(evento.getPrecio() * cantidad);
        orden.setCreadaEn(ahora);
        orden.setExpiraEn(ahora.plus(Duration.ofMinutes(app.minutosReserva())));

        if (evento.isGratuito()) {
            marcarPagada(orden, ahora);
        }
        return ordenes.save(orden);
    }

    /**
     * Abre la transacción en Webpay. Va fuera de una transacción de base de
     * datos a propósito: no se retiene un bloqueo mientras se espera la red.
     */
    public TransaccionIniciada iniciarPago(String codigoOrden) {
        Orden orden = ordenes.findByCodigo(codigoOrden).orElseThrow(() -> new NoEncontradoException("La orden"));
        if (orden.getEstado() != EstadoOrden.PENDIENTE || orden.getTokenWebpay() != null) {
            throw new ReglaNegocioException("Esta compra ya no se puede pagar. Inicia una nueva.");
        }
        TransaccionIniciada transaccion;
        try {
            transaccion = pasarela.crear(orden.getCodigo(), orden.getIdSesion(), orden.getMonto(),
                    app.url("/pago/retorno"));
        } catch (PagoException e) {
            log.error("No se pudo iniciar el pago de la orden {}", codigoOrden, e);
            cerrarPendiente(codigoOrden, EstadoOrden.ANULADA);
            throw new ReglaNegocioException("No pudimos conectar con Webpay. Tus entradas se liberaron; inténtalo de nuevo en unos minutos.");
        }
        tx.executeWithoutResult(s -> ordenes.bloquear(orden.getId()).ifPresent(o -> o.setTokenWebpay(transaccion.token())));
        return transaccion;
    }

    /**
     * Webpay volvió con token_ws: el comprador terminó el formulario. Se
     * confirma la transacción y, si fue aprobada, se emiten las entradas.
     * El bloqueo de la orden hace que un doble envío del navegador espere y
     * encuentre la orden ya resuelta, sin confirmar dos veces.
     */
    @Transactional(noRollbackFor = PagoException.class)
    public Orden confirmarPago(String token) {
        RefOrden ref = ordenes.referenciaPorToken(token).orElseThrow(() -> new NoEncontradoException("La transacción"));
        Evento evento = eventos.bloquear(ref.eventoId()).orElseThrow();
        Orden orden = ordenes.bloquear(ref.ordenId()).orElseThrow();
        if (orden.getEstado() != EstadoOrden.PENDIENTE && orden.getEstado() != EstadoOrden.EXPIRADA) {
            return orden;
        }

        ResultadoPago resultado;
        try {
            resultado = pasarela.confirmar(token);
        } catch (PagoException e) {
            // Puede que ya estuviera confirmada (reintento) o que Webpay
            // fallara; el estado real lo dice la consulta.
            log.warn("Falló la confirmación de {}; se consulta el estado", orden.getCodigo(), e);
            resultado = pasarela.consultar(token);
        }

        boolean cuadra = resultado.aprobado()
                && orden.getCodigo().equals(resultado.ordenCompra())
                && resultado.monto() != null && resultado.monto() == orden.getMonto();
        if (!cuadra) {
            if (resultado.aprobado()) {
                log.error("Pago aprobado que no cuadra con la orden {}: {}", orden.getCodigo(), resultado);
                reembolsarSilencioso(token, resultado.monto());
            }
            cerrar(evento, orden, EstadoOrden.RECHAZADA);
            return orden;
        }

        copiarDatosPago(orden, resultado);
        if (orden.getEstado() == EstadoOrden.EXPIRADA) {
            // Pagó después de que venciera la reserva: se vuelve a tomar el
            // cupo si queda; si no, se devuelve el dinero.
            if (evento.getEstado() == EstadoEvento.CANCELADO || evento.getDisponibles() < orden.getCantidad()) {
                log.warn("Orden {} pagada tras vencer y sin cupo: se reembolsa", orden.getCodigo());
                reembolsarSilencioso(token, orden.getMonto());
                orden.setEstado(EstadoOrden.REEMBOLSADA);
                sucesos.publishEvent(new Sucesos.OrdenReembolsada(orden.getId()));
                return orden;
            }
            evento.setEntradasReservadas(evento.getEntradasReservadas() + orden.getCantidad());
        }
        marcarPagada(orden, reloj.instant());
        return orden;
    }

    /**
     * Webpay volvió sin token_ws: el comprador anuló o se le acabó el tiempo.
     * Si viene el TBK_TOKEN debe ser el de la orden; si no viene, se exige el
     * session_id secreto. Así nadie anula una compra ajena con solo su código.
     */
    @Transactional
    public Orden anularPorAbandono(String codigoOrden, String idSesion, String tokenTbk) {
        RefOrden ref = ordenes.referenciaPorCodigo(codigoOrden).orElseThrow(() -> new NoEncontradoException("La orden"));
        Evento evento = eventos.bloquear(ref.eventoId()).orElseThrow();
        Orden orden = ordenes.bloquear(ref.ordenId()).orElseThrow();
        boolean legitimo = tokenTbk != null
                ? tokenTbk.equals(orden.getTokenWebpay())
                : orden.getIdSesion().equals(idSesion);
        if (!legitimo) {
            throw new ReglaNegocioException("La respuesta de Webpay no corresponde a esta compra.");
        }
        if (orden.getEstado() == EstadoOrden.PENDIENTE) {
            cerrar(evento, orden, EstadoOrden.ANULADA);
        }
        return orden;
    }

    /** Devuelve el dinero en Webpay y libera el cupo. Lo usa el panel. */
    @Transactional
    public Orden reembolsar(String codigoOrden) {
        RefOrden ref = ordenes.referenciaPorCodigo(codigoOrden).orElseThrow(() -> new NoEncontradoException("La orden"));
        Evento evento = eventos.bloquear(ref.eventoId()).orElseThrow();
        Orden orden = ordenes.bloquear(ref.ordenId()).orElseThrow();
        if (!orden.estaPagada()) {
            throw new ReglaNegocioException("Solo se reembolsan órdenes pagadas.");
        }
        if (orden.getMonto() > 0) {
            try {
                pasarela.reembolsar(orden.getTokenWebpay(), orden.getMonto());
            } catch (PagoException e) {
                log.error("Webpay rechazó el reembolso de {}", codigoOrden, e);
                throw new ReglaNegocioException("Webpay no aceptó el reembolso: " + e.getMessage());
            }
        }
        cerrar(evento, orden, EstadoOrden.REEMBOLSADA);
        sucesos.publishEvent(new Sucesos.OrdenReembolsada(orden.getId()));
        return orden;
    }

    /** Reembolsa una por una las órdenes pagadas de un evento cancelado. Devuelve cuántas fallaron. */
    public int reembolsarEvento(Long eventoId) {
        Evento evento = eventos.findById(eventoId).orElseThrow(() -> new NoEncontradoException("El evento"));
        if (evento.getEstado() != EstadoEvento.CANCELADO) {
            throw new ReglaNegocioException("Primero cancela el evento.");
        }
        int fallidas = 0;
        for (Orden o : ordenes.findByEventoIdAndEstado(eventoId, EstadoOrden.PAGADA)) {
            try {
                reembolsarEnTransaccion(o.getCodigo());
            } catch (RuntimeException e) {
                fallidas++;
            }
        }
        return fallidas;
    }

    /** Cada minuto devuelve al cupo las reservas que vencieron sin pago. */
    @Scheduled(fixedDelayString = "PT1M", initialDelayString = "PT1M")
    public void expirarReservas() {
        for (RefOrden ref : ordenes.vencidas(EstadoOrden.PENDIENTE, reloj.instant())) {
            tx.executeWithoutResult(s -> {
                Evento evento = eventos.bloquear(ref.eventoId()).orElseThrow();
                Orden orden = ordenes.bloquear(ref.ordenId()).orElseThrow();
                if (orden.getEstado() == EstadoOrden.PENDIENTE) {
                    cerrar(evento, orden, EstadoOrden.EXPIRADA);
                }
            });
        }
    }

    @Transactional(readOnly = true)
    public List<Orden> comprasDe(Long usuarioId) {
        return ordenes.findByUsuarioIdAndEstadoOrderByCreadaEnDesc(usuarioId, EstadoOrden.PAGADA);
    }

    private void reembolsarEnTransaccion(String codigo) {
        tx.executeWithoutResult(s -> reembolsar(codigo));
    }

    private void cerrarPendiente(String codigoOrden, EstadoOrden estado) {
        tx.executeWithoutResult(s -> {
            RefOrden ref = ordenes.referenciaPorCodigo(codigoOrden).orElseThrow();
            Evento evento = eventos.bloquear(ref.eventoId()).orElseThrow();
            Orden orden = ordenes.bloquear(ref.ordenId()).orElseThrow();
            if (orden.getEstado() == EstadoOrden.PENDIENTE) {
                cerrar(evento, orden, estado);
            }
        });
    }

    /** Requiere el evento bloqueado. Las vencidas de este evento devuelven su cupo antes de vender. */
    private void liberarVencidas(Evento evento, Instant ahora) {
        for (Long id : ordenes.vencidasDeEvento(evento.getId(), EstadoOrden.PENDIENTE, ahora)) {
            ordenes.bloquear(id).ifPresent(o -> cerrar(evento, o, EstadoOrden.EXPIRADA));
        }
    }

    /** Requiere evento y orden bloqueados. */
    private void cerrar(Evento evento, Orden orden, EstadoOrden estado) {
        if (!orden.getEstado().liberaCupo() && estado.liberaCupo()) {
            evento.setEntradasReservadas(evento.getEntradasReservadas() - orden.getCantidad());
        }
        orden.setEstado(estado);
    }

    private void marcarPagada(Orden orden, Instant ahora) {
        orden.setEstado(EstadoOrden.PAGADA);
        orden.setPagadaEn(ahora);
        for (int i = 0; i < orden.getCantidad(); i++) {
            Entrada entrada = new Entrada();
            entrada.setCodigo(Codigos.entrada());
            entrada.setOrden(orden);
            orden.getEntradas().add(entrada);
        }
        ordenes.save(orden);
        sucesos.publishEvent(new Sucesos.OrdenPagada(orden.getId()));
    }

    private void copiarDatosPago(Orden orden, ResultadoPago r) {
        orden.setCodigoAutorizacion(r.codigoAutorizacion());
        orden.setTipoPago(r.tipoPago());
        orden.setCuotas(r.cuotas());
        orden.setTarjetaUltimos4(r.ultimos4());
    }

    private void reembolsarSilencioso(String token, Integer monto) {
        if (monto == null || monto <= 0) {
            return;
        }
        try {
            pasarela.reembolsar(token, monto);
        } catch (PagoException e) {
            log.error("REEMBOLSO MANUAL PENDIENTE: token {} por ${}", token, monto, e);
        }
    }
}
