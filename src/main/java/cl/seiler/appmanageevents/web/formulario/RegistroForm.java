package cl.seiler.appmanageevents.web.formulario;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RegistroForm {

    @NotBlank(message = "Escribe tu nombre")
    @Size(max = 120)
    private String nombre;

    @NotBlank(message = "Escribe tu correo")
    @Email(message = "Ese correo no es válido")
    @Size(max = 160)
    private String email;

    // 72 es el tope de BCrypt: lo que pase de ahí se ignoraría en silencio.
    @NotBlank(message = "Elige una contraseña")
    @Size(min = 8, max = 72, message = "Entre 8 y 72 caracteres")
    private String password;

    private String confirmacion;

    private boolean recibeNotificaciones = true;

    @AssertTrue(message = "Las contraseñas no coinciden")
    public boolean isConfirmacionValida() {
        return password != null && password.equals(confirmacion);
    }
}
