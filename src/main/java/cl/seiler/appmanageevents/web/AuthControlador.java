package cl.seiler.appmanageevents.web;

import cl.seiler.appmanageevents.servicio.ReglaNegocioException;
import cl.seiler.appmanageevents.servicio.UsuarioServicio;
import cl.seiler.appmanageevents.web.formulario.RegistroForm;
import jakarta.validation.Valid;
import org.springframework.security.core.Authentication;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class AuthControlador {

    private final UsuarioServicio usuarios;

    public AuthControlador(UsuarioServicio usuarios) {
        this.usuarios = usuarios;
    }

    @GetMapping("/login")
    public String login(Authentication sesion) {
        return conSesion(sesion) ? "redirect:/" : "auth/login";
    }

    @GetMapping("/registro")
    public String registro(@ModelAttribute("form") RegistroForm form, Authentication sesion) {
        return conSesion(sesion) ? "redirect:/" : "auth/registro";
    }

    @PostMapping("/registro")
    public String registrar(@Valid @ModelAttribute("form") RegistroForm form, BindingResult errores,
                            Model model, RedirectAttributes flash) {
        if (errores.hasErrors()) {
            return "auth/registro";
        }
        try {
            usuarios.registrar(form);
        } catch (ReglaNegocioException e) {
            model.addAttribute("error", e.getMessage());
            return "auth/registro";
        }
        flash.addFlashAttribute("ok", "Cuenta creada. Ya puedes iniciar sesión.");
        flash.addFlashAttribute("email", form.getEmail());
        return "redirect:/login";
    }

    private static boolean conSesion(Authentication a) {
        return a != null && a.isAuthenticated() && !(a instanceof AnonymousAuthenticationToken);
    }
}
