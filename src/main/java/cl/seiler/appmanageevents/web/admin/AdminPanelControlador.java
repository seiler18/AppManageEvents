package cl.seiler.appmanageevents.web.admin;

import cl.seiler.appmanageevents.dominio.EstadoOrden;
import cl.seiler.appmanageevents.dominio.Rol;
import cl.seiler.appmanageevents.servicio.*;
import cl.seiler.appmanageevents.servicio.EntradaServicio.Veredicto;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/** Portada del panel, órdenes, validación en la puerta y usuarios. */
@Controller
@RequestMapping("/admin")
public class AdminPanelControlador {

    private final PanelServicio panel;
    private final CompraServicio compras;
    private final EntradaServicio entradas;
    private final UsuarioServicio usuarios;

    public AdminPanelControlador(PanelServicio panel, CompraServicio compras, EntradaServicio entradas,
                                 UsuarioServicio usuarios) {
        this.panel = panel;
        this.compras = compras;
        this.entradas = entradas;
        this.usuarios = usuarios;
    }

    @GetMapping
    public String inicio(Model model) {
        model.addAttribute("resumen", panel.resumen());
        return "admin/panel";
    }

    @GetMapping("/ordenes")
    public String ordenes(@RequestParam(required = false) EstadoOrden estado, Model model) {
        model.addAttribute("ordenes", panel.ordenes(estado));
        model.addAttribute("estados", EstadoOrden.values());
        model.addAttribute("estado", estado);
        return "admin/ordenes";
    }

    @PostMapping("/ordenes/{codigo}/reembolsar")
    public String reembolsar(@PathVariable String codigo, @RequestHeader(value = "Referer", required = false) String origen,
                             RedirectAttributes flash) {
        try {
            compras.reembolsar(codigo);
            flash.addFlashAttribute("ok", "Orden " + codigo + " reembolsada.");
        } catch (ReglaNegocioException e) {
            flash.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:" + volverA(origen, "/admin/ordenes");
    }

    @GetMapping("/validar")
    public String validar(@RequestParam(required = false) String codigo, Model model) {
        if (StringUtils.hasText(codigo)) {
            model.addAttribute("validacion", entradas.revisar(codigo));
            model.addAttribute("codigo", codigo.trim());
            model.addAttribute("recienMarcada", false);
        }
        return "admin/validar";
    }

    @PostMapping("/validar")
    public String marcar(@RequestParam String codigo, Model model) {
        EntradaServicio.Validacion v = entradas.marcarUsada(codigo);
        model.addAttribute("validacion", v);
        model.addAttribute("codigo", codigo.trim());
        model.addAttribute("recienMarcada", v.veredicto() == Veredicto.VALIDA);
        return "admin/validar";
    }

    @GetMapping("/usuarios")
    public String usuarios(Model model) {
        model.addAttribute("usuarios", usuarios.todos());
        return "admin/usuarios";
    }

    @PostMapping("/usuarios/{id}/rol")
    public String cambiarRol(@PathVariable Long id, @RequestParam Rol rol, Authentication sesion,
                             RedirectAttributes flash) {
        try {
            usuarios.cambiarRol(id, rol, sesion.getName());
            flash.addFlashAttribute("ok", "Rol actualizado.");
        } catch (ReglaNegocioException e) {
            flash.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/admin/usuarios";
    }

    /** Solo se vuelve a rutas propias del panel: un Referer ajeno no debe convertirse en redirección abierta. */
    static String volverA(String referer, String porDefecto) {
        if (referer == null) {
            return porDefecto;
        }
        int i = referer.indexOf("/admin/");
        return i >= 0 ? referer.substring(i) : porDefecto;
    }
}
