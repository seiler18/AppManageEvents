package cl.seiler.appmanageevents.web.formulario;

import cl.seiler.appmanageevents.dominio.Salon;
import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SalonForm {

    @NotBlank(message = "Ponle un nombre")
    @Size(max = 120)
    private String nombre;

    @Size(max = 200)
    private String direccion;

    @NotNull(message = "Indica la capacidad")
    @Min(value = 1, message = "La capacidad debe ser mayor que cero")
    private Integer capacidad;

    @Size(max = 1000)
    private String descripcion;

    public static SalonForm desde(Salon s) {
        SalonForm f = new SalonForm();
        f.nombre = s.getNombre();
        f.direccion = s.getDireccion();
        f.capacidad = s.getCapacidad();
        f.descripcion = s.getDescripcion();
        return f;
    }

    public void aplicarA(Salon s) {
        s.setNombre(nombre.trim());
        s.setDireccion(direccion);
        s.setCapacidad(capacidad);
        s.setDescripcion(descripcion);
    }
}
