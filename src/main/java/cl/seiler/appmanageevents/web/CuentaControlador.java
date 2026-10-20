package cl.seiler.appmanageevents.web;

import cl.seiler.appmanageevents.config.AppProperties;
import cl.seiler.appmanageevents.dominio.Entrada;
import cl.seiler.appmanageevents.dominio.Usuario;
import cl.seiler.appmanageevents.servicio.*;
import cl.seiler.appmanageevents.web.formulario.ClaveForm;
import cl.seiler.appmanageevents.web.formulario.PerfilForm;
import jakarta.validation.Valid;
import org.springframework.http.CacheControl;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/cuenta")
public class CuentaControlador {

    private final CompraServicio compras;
    private final EntradaServicio entradas;
    private final UsuarioServicio usuarios;
    private final QrServicio qr;
    private final AppProperties app;

    public CuentaControlador(CompraServicio compras, EntradaServicio entradas, UsuarioServicio usuarios,
                             QrServicio qr, AppProperties app) {
        this.compras = compras;
        this.entradas = entradas;
        this.usuarios = usuarios;
        this.qr = qr;
        this.app = app;
    }

    @GetMapping("/entradas")
    public String misEntradas(Authentication sesion, Model model) {
        Usuario u = usuarios.porEmail(sesion.getName());
        model.addAttribute("ordenes", compras.comprasDe(u.getId()));
        return "cuenta/entradas";
    }

    @GetMapping("/entradas/{codigo}")
    public String entrada(@PathVariable String codigo, Authentication sesion, Model model) {
        Entrada e = entradaPropia(codigo, sesion);
        model.addAttribute("entrada", e);
        model.addAttribute("orden", e.getOrden());
        model.addAttribute("evento", e.getOrden().getEvento());
        return "cuenta/entrada";
    }

    /** El QR lleva la URL de validación del panel: el personal la abre con la cámara del teléfono. */
    @GetMapping(value = "/entradas/{codigo}/qr.png", produces = MediaType.IMAGE_PNG_VALUE)
    @ResponseBody
    public ResponseEntity<byte[]> qr(@PathVariable String codigo, Authentication sesion) {
        Entrada e = entradaPropia(codigo, sesion);
        byte[] png = qr.png(app.url("/admin/validar?codigo=" + e.getCodigo()), 320);
        return ResponseEntity.ok().cacheControl(CacheControl.noStore()).body(png);
    }

    @GetMapping("/perfil")
    public String perfil(Authentication sesion, Model model) {
        Usuario u = usuarios.porEmail(sesion.getName());
        PerfilForm form = new PerfilForm();
        form.setNombre(u.getNombre());
        form.setRecibeNotificaciones(u.isRecibeNotificaciones());
        model.addAttribute("form", form);
        model.addAttribute("clave", new ClaveForm());
        model.addAttribute("usuario", u);
        return "cuenta/perfil";
    }

    @PostMapping("/perfil")
    public String guardarPerfil(@Valid @ModelAttribute("form") PerfilForm form, BindingResult errores,
                                Authentication sesion, Model model, RedirectAttributes flash) {
        if (errores.hasErrors()) {
            model.addAttribute("clave", new ClaveForm());
            model.addAttribute("usuario", usuarios.porEmail(sesion.getName()));
            return "cuenta/perfil";
        }
        usuarios.actualizarPerfil(sesion.getName(), form);
        flash.addFlashAttribute("ok", "Datos guardados.");
        return "redirect:/cuenta/perfil";
    }

    @PostMapping("/clave")
    public String cambiarClave(@Valid @ModelAttribute("clave") ClaveForm form, BindingResult errores,
                               Authentication sesion, Model model, RedirectAttributes flash) {
        if (!errores.hasErrors()) {
            try {
                usuarios.cambiarClave(sesion.getName(), form);
                flash.addFlashAttribute("ok", "Contraseña actualizada.");
                return "redirect:/cuenta/perfil";
            } catch (ReglaNegocioException e) {
                model.addAttribute("errorClave", e.getMessage());
            }
        }
        Usuario u = usuarios.porEmail(sesion.getName());
        PerfilForm perfil = new PerfilForm();
        perfil.setNombre(u.getNombre());
        perfil.setRecibeNotificaciones(u.isRecibeNotificaciones());
        model.addAttribute("form", perfil);
        model.addAttribute("usuario", u);
        return "cuenta/perfil";
    }

    private Entrada entradaPropia(String codigo, Authentication sesion) {
        Entrada e = entradas.buscar(codigo).orElseThrow(() -> new NoEncontradoException("La entrada"));
        Acceso.exigirDuenoOAdmin(e.getOrden(), sesion);
        return e;
    }
}
