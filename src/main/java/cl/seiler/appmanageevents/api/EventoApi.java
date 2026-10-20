package cl.seiler.appmanageevents.api;

import cl.seiler.appmanageevents.config.AppProperties;
import cl.seiler.appmanageevents.dominio.Evento;
import cl.seiler.appmanageevents.servicio.EventoServicio;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Cartelera en JSON, solo lectura: sirve para incrustar los eventos en otra
 * web. Las escrituras van por el panel, que tiene sesión y CSRF.
 */
@RestController
@RequestMapping("/api/eventos")
@Tag(name = "Eventos", description = "Cartelera pública")
public class EventoApi {

    public record EventoDto(Long id, String titulo, String descripcion, LocalDateTime fechaInicio,
                            String lugar, String direccion, int precio, int disponibles,
                            String estado, String imagenUrl, String url) {
    }

    private final EventoServicio eventos;
    private final AppProperties app;

    public EventoApi(EventoServicio eventos, AppProperties app) {
        this.eventos = eventos;
        this.app = app;
    }

    @GetMapping
    @Operation(summary = "Eventos publicados que aún no comienzan, por fecha")
    public List<EventoDto> proximos() {
        return eventos.proximos().stream().map(this::dto).toList();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Un evento publicado o cancelado")
    public EventoDto uno(@PathVariable Long id) {
        return dto(eventos.publico(id));
    }

    private EventoDto dto(Evento e) {
        return new EventoDto(e.getId(), e.getTitulo(), e.getDescripcion(), e.getFechaInicio(),
                e.getSalon() == null ? null : e.getSalon().getNombre(),
                e.getSalon() == null ? null : e.getSalon().getDireccion(),
                e.getPrecio(), e.getDisponibles(), e.getEstado().name(), e.getImagenUrl(),
                app.url("/eventos/" + e.getId()));
    }
}
