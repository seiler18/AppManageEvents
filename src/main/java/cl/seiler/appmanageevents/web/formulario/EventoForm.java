package cl.seiler.appmanageevents.web.formulario;

import cl.seiler.appmanageevents.dominio.Evento;
import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.validator.constraints.URL;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

@Getter
@Setter
public class EventoForm {

    @NotBlank(message = "Ponle un título")
    @Size(max = 150)
    private String titulo;

    @Size(max = 5000)
    private String descripcion;

    @NotNull(message = "Indica fecha y hora")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private LocalDateTime fechaInicio;

    private Long salonId;

    @NotNull(message = "Indica el precio (0 si es gratuito)")
    @Min(value = 0, message = "El precio no puede ser negativo")
    @Max(value = 99_999_999, message = "Precio demasiado alto")
    private Integer precio = 0;

    @NotNull(message = "Indica el máximo de entradas")
    @Min(value = 1, message = "Debe haber al menos una entrada")
    @Max(value = 1_000_000)
    private Integer cupoMaximo;

    @NotNull
    @Min(value = 1, message = "Al menos 1 por compra")
    @Max(value = 50, message = "Máximo 50 por compra")
    private Integer maxPorCompra = 10;

    @URL(message = "Debe ser una URL (https://...)")
    @Size(max = 500)
    private String imagenUrl;

    public static EventoForm desde(Evento e) {
        EventoForm f = new EventoForm();
        f.titulo = e.getTitulo();
        f.descripcion = e.getDescripcion();
        f.fechaInicio = e.getFechaInicio();
        f.salonId = e.getSalon() == null ? null : e.getSalon().getId();
        f.precio = e.getPrecio();
        f.cupoMaximo = e.getCupoMaximo();
        f.maxPorCompra = e.getMaxPorCompra();
        f.imagenUrl = e.getImagenUrl();
        return f;
    }
}
