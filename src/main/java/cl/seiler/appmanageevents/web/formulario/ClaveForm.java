package cl.seiler.appmanageevents.web.formulario;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ClaveForm {

    @NotBlank(message = "Escribe tu contraseña actual")
    private String actual;

    @NotBlank(message = "Elige la nueva contraseña")
    @Size(min = 8, max = 72, message = "Entre 8 y 72 caracteres")
    private String nueva;

    private String confirmacion;

    @AssertTrue(message = "Las contraseñas no coinciden")
    public boolean isConfirmacionValida() {
        return nueva != null && nueva.equals(confirmacion);
    }
}
