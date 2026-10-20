package cl.seiler.appmanageevents.web.admin;

import cl.seiler.appmanageevents.dominio.Entrada;
import cl.seiler.appmanageevents.dominio.Evento;
import cl.seiler.appmanageevents.dominio.Orden;
import cl.seiler.appmanageevents.servicio.*;
import cl.seiler.appmanageevents.servicio.NotificacionServicio.Destinatarios;
import cl.seiler.appmanageevents.web.Formato;
import cl.seiler.appmanageevents.web.formulario.EventoForm;
import cl.seiler.appmanageevents.web.formulario.SalonForm;
import jakarta.validation.Valid;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.nio.charset.StandardCharsets;
import java.util.List;

@Controller
@RequestMapping("/admin")
public class AdminEventoControlador {

    private final EventoServicio eventos;
    private final CompraServicio compras;
    private final PanelServicio panel;
    private final NotificacionServicio notificaciones;
    private final Formato fmt;

    public AdminEventoControlador(EventoServicio eventos, CompraServicio compras, PanelServicio panel,
                                  NotificacionServicio notificaciones, Formato fmt) {
        this.eventos = eventos;
        this.compras = compras;
        this.panel = panel;
        this.notificaciones = notificaciones;
        this.fmt = fmt;
    }

    // ---------- Eventos ----------

    @GetMapping("/eventos")
    public String lista(Model model) {
        model.addAttribute("eventos", eventos.todos());
        return "admin/eventos";
    }

    @GetMapping("/eventos/nuevo")
    public String nuevo(Model model) {
        return formulario(model, new EventoForm(), null);
    }

    @PostMapping("/eventos")
    public String crear(@Valid @ModelAttribute("form") EventoForm form, BindingResult errores,
                        Model model, RedirectAttributes flash) {
        if (errores.hasErrors()) {
            return formulario(model, form, null);
        }
        try {
            Evento e = eventos.crear(form);
            flash.addFlashAttribute("ok", "Evento creado como borrador. Revísalo y publícalo cuando esté listo.");
            return "redirect:/admin/eventos/" + e.getId();
        } catch (ReglaNegocioException ex) {
            model.addAttribute("error", ex.getMessage());
            return formulario(model, form, null);
        }
    }

    @GetMapping("/eventos/{id}")
    public String detalle(@PathVariable Long id, Model model) {
        model.addAttribute("evento", eventos.obtener(id));
        model.addAttribute("ordenes", panel.ordenesDeEvento(id));
        model.addAttribute("recaudacion", panel.recaudacionDeEvento(id));
        return "admin/evento";
    }

    @GetMapping("/eventos/{id}/editar")
    public String editar(@PathVariable Long id, Model model) {
        return formulario(model, EventoForm.desde(eventos.obtener(id)), id);
    }

    @PostMapping("/eventos/{id}")
    public String actualizar(@PathVariable Long id, @Valid @ModelAttribute("form") EventoForm form,
                             BindingResult errores, Model model, RedirectAttributes flash) {
        if (errores.hasErrors()) {
            return formulario(model, form, id);
        }
        try {
            eventos.actualizar(id, form);
            flash.addFlashAttribute("ok", "Cambios guardados.");
            return "redirect:/admin/eventos/" + id;
        } catch (ReglaNegocioException ex) {
            model.addAttribute("error", ex.getMessage());
            return formulario(model, form, id);
        }
    }

    @PostMapping("/eventos/{id}/publicar")
    public String publicar(@PathVariable Long id, RedirectAttributes flash) {
        return accion(id, flash, () -> eventos.publicar(id),
                "Evento publicado. Se está avisando por correo a los suscritos.");
    }

    @PostMapping("/eventos/{id}/cancelar")
    public String cancelar(@PathVariable Long id, RedirectAttributes flash) {
        return accion(id, flash, () -> eventos.cancelar(id),
                "Evento cancelado y venta detenida. Se avisó a quienes compraron; los reembolsos se hacen aparte.");
    }

    @PostMapping("/eventos/{id}/reembolsar")
    public String reembolsarTodo(@PathVariable Long id, RedirectAttributes flash) {
        try {
            int fallidas = compras.reembolsarEvento(id);
            if (fallidas == 0) {
                flash.addFlashAttribute("ok", "Todas las órdenes pagadas fueron reembolsadas.");
            } else {
                flash.addFlashAttribute("error", fallidas + " reembolsos fallaron; revisa las órdenes que siguen pagadas.");
            }
        } catch (ReglaNegocioException e) {
            flash.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/admin/eventos/" + id;
    }

    @PostMapping("/eventos/{id}/eliminar")
    public String eliminar(@PathVariable Long id, RedirectAttributes flash) {
        try {
            eventos.eliminar(id);
            flash.addFlashAttribute("ok", "Evento eliminado.");
            return "redirect:/admin/eventos";
        } catch (ReglaNegocioException e) {
            flash.addFlashAttribute("error", e.getMessage());
            return "redirect:/admin/eventos/" + id;
        }
    }

    @PostMapping("/eventos/{id}/mensaje")
    public String mensaje(@PathVariable Long id, @RequestParam String asunto, @RequestParam String texto,
                          @RequestParam Destinatarios destinatarios, RedirectAttributes flash) {
        if (!StringUtils.hasText(asunto) || !StringUtils.hasText(texto)) {
            flash.addFlashAttribute("error", "Escribe asunto y mensaje.");
        } else {
            notificaciones.enviarMensaje(id, asunto.trim(), texto.trim(), destinatarios);
            flash.addFlashAttribute("ok", "Mensaje en camino.");
        }
        return "redirect:/admin/eventos/" + id;
    }

    @GetMapping("/eventos/{id}/asistentes.csv")
    public ResponseEntity<byte[]> asistentes(@PathVariable Long id) {
        Evento e = eventos.obtener(id);
        StringBuilder csv = new StringBuilder("﻿"); // BOM: Excel abre bien las tildes
        csv.append("Nombre;Correo;Orden;Entrada;Usada;Pagada el\r\n");
        for (Orden o : panel.ordenesDeEvento(id)) {
            if (!o.estaPagada()) {
                continue;
            }
            for (Entrada en : o.getEntradas()) {
                csv.append(celda(o.getUsuario().getNombre())).append(';')
                        .append(celda(o.getUsuario().getEmail())).append(';')
                        .append(o.getCodigo()).append(';')
                        .append(en.getCodigo()).append(';')
                        .append(en.isUsada() ? fmt.corta(en.getUsadaEn()) : "No").append(';')
                        .append(fmt.corta(o.getPagadaEn())).append("\r\n");
            }
        }
        return ResponseEntity.ok()
                .contentType(new MediaType("text", "csv", StandardCharsets.UTF_8))
                .header(HttpHeaders.CONTENT_DISPOSITION, ContentDisposition.attachment()
                        .filename("asistentes-evento-" + e.getId() + ".csv").build().toString())
                .body(csv.toString().getBytes(StandardCharsets.UTF_8));
    }

    // ---------- Salones ----------

    @GetMapping("/salones")
    public String salones(Model model) {
        model.addAttribute("salones", eventos.salones());
        return "admin/salones";
    }

    @GetMapping("/salones/nuevo")
    public String nuevoSalon(Model model) {
        model.addAttribute("form", new SalonForm());
        return "admin/salon-form";
    }

    @GetMapping("/salones/{id}/editar")
    public String editarSalon(@PathVariable Long id, Model model) {
        model.addAttribute("form", SalonForm.desde(eventos.salon(id)));
        model.addAttribute("salonId", id);
        return "admin/salon-form";
    }

    @PostMapping({"/salones", "/salones/{id}"})
    public String guardarSalon(@PathVariable(required = false) Long id, @Valid @ModelAttribute("form") SalonForm form,
                               BindingResult errores, Model model, RedirectAttributes flash) {
        if (errores.hasErrors()) {
            model.addAttribute("salonId", id);
            return "admin/salon-form";
        }
        eventos.guardarSalon(id, form);
        flash.addFlashAttribute("ok", "Salón guardado.");
        return "redirect:/admin/salones";
    }

    @PostMapping("/salones/{id}/eliminar")
    public String eliminarSalon(@PathVariable Long id, RedirectAttributes flash) {
        try {
            eventos.eliminarSalon(id);
            flash.addFlashAttribute("ok", "Salón eliminado.");
        } catch (ReglaNegocioException e) {
            flash.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/admin/salones";
    }

    private String formulario(Model model, EventoForm form, Long id) {
        List<?> salones = eventos.salones();
        model.addAttribute("form", form);
        model.addAttribute("salones", salones);
        model.addAttribute("eventoId", id);
        return "admin/evento-form";
    }

    private String accion(Long id, RedirectAttributes flash, Runnable accion, String exito) {
        try {
            accion.run();
            flash.addFlashAttribute("ok", exito);
        } catch (ReglaNegocioException e) {
            flash.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/admin/eventos/" + id;
    }

    /** Evita que un nombre como «=HYPERLINK(...)» se ejecute como fórmula al abrir el CSV. */
    private static String celda(String valor) {
        if (valor == null) {
            return "";
        }
        String v = valor.replace("\"", "\"\"");
        if (!v.isEmpty() && "=+-@".indexOf(v.charAt(0)) >= 0) {
            v = "'" + v;
        }
        return "\"" + v + "\"";
    }
}
