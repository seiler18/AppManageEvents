package cl.seiler.appmanageevents.web;

import cl.seiler.appmanageevents.dominio.Evento;
import cl.seiler.appmanageevents.servicio.EventoServicio;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.time.Clock;
import java.time.LocalDateTime;

@Controller
public class InicioControlador {

    private final EventoServicio eventos;
    private final Clock reloj;

    public InicioControlador(EventoServicio eventos, Clock reloj) {
        this.eventos = eventos;
        this.reloj = reloj;
    }

    @GetMapping({"/", "/eventos"})
    public String inicio(Model model) {
        model.addAttribute("eventos", eventos.proximos());
        return "publico/inicio";
    }

    @GetMapping("/eventos/{id}")
    public String evento(@PathVariable Long id, Model model) {
        Evento e = eventos.publico(id);
        boolean futuro = e.getFechaInicio().isAfter(LocalDateTime.now(reloj));
        model.addAttribute("evento", e);
        model.addAttribute("aLaVenta", e.estaPublicado() && futuro && e.getDisponibles() > 0);
        model.addAttribute("agotado", e.estaPublicado() && futuro && e.getDisponibles() == 0);
        model.addAttribute("terminado", !futuro);
        model.addAttribute("maxCompra", Math.min(e.getMaxPorCompra(), e.getDisponibles()));
        return "publico/evento";
    }

    /** Quien intenta comprar sin sesión vuelve aquí tras el login: el POST original no se repite. */
    @GetMapping("/eventos/{id}/comprar")
    public String volverAlEvento(@PathVariable Long id) {
        return "redirect:/eventos/" + id;
    }
}
