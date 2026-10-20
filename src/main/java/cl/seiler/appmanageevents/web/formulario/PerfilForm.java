package cl.seiler.appmanageevents.web.formulario;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PerfilForm {

    @NotBlank(message = "Escribe tu nombre")
    @Size(max = 120)
    private String nombre;

    private boolean recibeNotificaciones;
}
