package cl.seiler.appmanageevents.servicio;

import cl.seiler.appmanageevents.dominio.EstadoEvento;
import cl.seiler.appmanageevents.dominio.Evento;
import cl.seiler.appmanageevents.dominio.Salon;
import cl.seiler.appmanageevents.repositorio.EventoRepositorio;
import cl.seiler.appmanageevents.repositorio.OrdenRepositorio;
import cl.seiler.appmanageevents.repositorio.SalonRepositorio;
import cl.seiler.appmanageevents.web.formulario.EventoForm;
import cl.seiler.appmanageevents.web.formulario.SalonForm;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;

@Service
@Transactional(readOnly = true)
public class EventoServicio {

    private final EventoRepositorio eventos;
    private final SalonRepositorio salones;
    private final OrdenRepositorio ordenes;
    private final ApplicationEventPublisher sucesos;
    private final Clock reloj;

    public EventoServicio(EventoRepositorio eventos, SalonRepositorio salones, OrdenRepositorio ordenes,
                          ApplicationEventPublisher sucesos, Clock reloj) {
        this.eventos = eventos;
        this.salones = salones;
        this.ordenes = ordenes;
        this.sucesos = sucesos;
        this.reloj = reloj;
    }

    public List<Evento> proximos() {
        return eventos.findByEstadoAndFechaInicioAfterOrderByFechaInicioAsc(EstadoEvento.PUBLICADO, LocalDateTime.now(reloj));
    }

    /** Lo que ve el público: un borrador no existe para él. */
    public Evento publico(Long id) {
        Evento e = obtener(id);
        if (e.getEstado() == EstadoEvento.BORRADOR) {
            throw new NoEncontradoException("El evento");
        }
        return e;
    }

    public Evento obtener(Long id) {
        return eventos.buscarConSalon(id).orElseThrow(() -> new NoEncontradoException("El evento"));
    }

    public List<Evento> todos() {
        return eventos.findAllByOrderByFechaInicioDesc();
    }

    public List<Salon> salones() {
        return salones.findAllByOrderByNombreAsc();
    }

    public Salon salon(Long id) {
        return salones.findById(id).orElseThrow(() -> new NoEncontradoException("El salón"));
    }

    @Transactional
    public Evento crear(EventoForm form) {
        if (!form.getFechaInicio().isAfter(LocalDateTime.now(reloj))) {
            throw new ReglaNegocioException("La fecha del evento debe ser futura.");
        }
        Evento e = new Evento();
        e.setCreadoEn(reloj.instant());
        aplicar(form, e);
        return eventos.save(e);
    }

    @Transactional
    public Evento actualizar(Long id, EventoForm form) {
        Evento e = eventos.bloquear(id).orElseThrow(() -> new NoEncontradoException("El evento"));
        if (form.getCupoMaximo() < e.getEntradasReservadas()) {
            throw new ReglaNegocioException("Ya hay " + e.getEntradasReservadas()
                    + " entradas vendidas o reservadas: el máximo no puede ser menor.");
        }
        if (!form.getFechaInicio().equals(e.getFechaInicio())) {
            // Con fecha nueva vuelve a corresponder el recordatorio del día anterior.
            e.setRecordatorioEnviado(false);
        }
        aplicar(form, e);
        return e;
    }

    @Transactional
    public void publicar(Long id) {
        Evento e = eventos.bloquear(id).orElseThrow(() -> new NoEncontradoException("El evento"));
        if (e.getEstado() != EstadoEvento.BORRADOR) {
            throw new ReglaNegocioException("Solo se publican eventos en borrador.");
        }
        if (!e.getFechaInicio().isAfter(LocalDateTime.now(reloj))) {
            throw new ReglaNegocioException("No se puede publicar un evento con fecha pasada.");
        }
        e.setEstado(EstadoEvento.PUBLICADO);
        sucesos.publishEvent(new Sucesos.EventoPublicado(e.getId()));
    }

    /** Detiene la venta y avisa a quienes compraron. Los reembolsos se hacen aparte, desde el panel. */
    @Transactional
    public void cancelar(Long id) {
        Evento e = eventos.bloquear(id).orElseThrow(() -> new NoEncontradoException("El evento"));
        if (e.getEstado() == EstadoEvento.CANCELADO) {
            return;
        }
        e.setEstado(EstadoEvento.CANCELADO);
        sucesos.publishEvent(new Sucesos.EventoCancelado(e.getId()));
    }

    @Transactional
    public void eliminar(Long id) {
        if (ordenes.existsByEventoId(id)) {
            throw new ReglaNegocioException("El evento tiene compras registradas: cancélalo en vez de eliminarlo.");
        }
        eventos.deleteById(id);
    }

    @Transactional
    public Salon guardarSalon(Long id, SalonForm form) {
        Salon s = id == null ? new Salon() : salon(id);
        form.aplicarA(s);
        return salones.save(s);
    }

    @Transactional
    public void eliminarSalon(Long id) {
        if (eventos.existsBySalonId(id)) {
            throw new ReglaNegocioException("Hay eventos en este salón: cámbialos de salón antes de eliminarlo.");
        }
        salones.deleteById(id);
    }

    private void aplicar(EventoForm form, Evento e) {
        Salon salon = form.getSalonId() == null ? null : salon(form.getSalonId());
        if (salon != null && form.getCupoMaximo() > salon.getCapacidad()) {
            throw new ReglaNegocioException("El salón " + salon.getNombre() + " admite hasta "
                    + salon.getCapacidad() + " personas.");
        }
        if (form.getMaxPorCompra() > form.getCupoMaximo()) {
            form.setMaxPorCompra(form.getCupoMaximo());
        }
        e.setTitulo(form.getTitulo().trim());
        e.setDescripcion(form.getDescripcion());
        e.setFechaInicio(form.getFechaInicio());
        e.setSalon(salon);
        e.setPrecio(form.getPrecio());
        e.setCupoMaximo(form.getCupoMaximo());
        e.setMaxPorCompra(form.getMaxPorCompra());
        e.setImagenUrl(StringUtils.hasText(form.getImagenUrl()) ? form.getImagenUrl().trim() : null);
    }
}
