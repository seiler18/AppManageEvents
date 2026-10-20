package cl.seiler.appmanageevents.web;

import cl.seiler.appmanageevents.servicio.UsuarioServicio;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class NotificacionControlador {

    private final UsuarioServicio usuarios;

    public NotificacionControlador(UsuarioServicio usuarios) {
        this.usuarios = usuarios;
    }

    /**
     * El enlace del correo solo muestra un botón: algunos clientes de correo
     * abren los enlaces por su cuenta para revisarlos, y un GET que diera de
     * baja desuscribiría a gente que nunca hizo clic.
     */
    @GetMapping("/notificaciones/baja")
    public String confirmar(@RequestParam("t") String token, Model model) {
        model.addAttribute("token", token);
        return "publico/baja";
    }

    @PostMapping("/notificaciones/baja")
    public String darDeBaja(@RequestParam("t") String token, Model model) {
        model.addAttribute("hecho", usuarios.darDeBaja(token));
        model.addAttribute("token", token);
        return "publico/baja";
    }
}
