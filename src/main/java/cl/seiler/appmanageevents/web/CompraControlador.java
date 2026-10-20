package cl.seiler.appmanageevents.web;

import cl.seiler.appmanageevents.dominio.Orden;
import cl.seiler.appmanageevents.pago.PagoException;
import cl.seiler.appmanageevents.pago.PasarelaPago.TransaccionIniciada;
import cl.seiler.appmanageevents.repositorio.OrdenRepositorio;
import cl.seiler.appmanageevents.servicio.CompraServicio;
import cl.seiler.appmanageevents.servicio.NoEncontradoException;
import cl.seiler.appmanageevents.servicio.ReglaNegocioException;
import cl.seiler.appmanageevents.servicio.UsuarioServicio;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class CompraControlador {

    private static final Logger log = LoggerFactory.getLogger(CompraControlador.class);

    private final CompraServicio compras;
    private final UsuarioServicio usuarios;
    private final OrdenRepositorio ordenes;

    public CompraControlador(CompraServicio compras, UsuarioServicio usuarios, OrdenRepositorio ordenes) {
        this.compras = compras;
        this.usuarios = usuarios;
        this.ordenes = ordenes;
    }

    @PostMapping("/eventos/{id}/comprar")
    public String comprar(@PathVariable Long id, @RequestParam int cantidad, Authentication sesion,
                          Model model, RedirectAttributes flash) {
        try {
            Long usuarioId = usuarios.porEmail(sesion.getName()).getId();
            Orden orden = compras.reservar(usuarioId, id, cantidad);
            if (orden.estaPagada()) {
                return "redirect:/compras/" + orden.getCodigo();
            }
            TransaccionIniciada t = compras.iniciarPago(orden.getCodigo());
            model.addAttribute("urlWebpay", t.url());
            model.addAttribute("token", t.token());
            return "compra/redirigir";
        } catch (ReglaNegocioException e) {
            flash.addFlashAttribute("error", e.getMessage());
            return "redirect:/eventos/" + id;
        }
    }

    /**
     * Retorno de Webpay. Llega por POST o GET según el caso:
     * <ul>
     *   <li>solo token_ws: el formulario terminó (aprobado o rechazado), se confirma;</li>
     *   <li>TBK_TOKEN + TBK_ORDEN_COMPRA: el comprador anuló;</li>
     *   <li>solo TBK_ORDEN_COMPRA + TBK_ID_SESION: se agotó el tiempo;</li>
     *   <li>token_ws + TBK_TOKEN: error en el formulario.</li>
     * </ul>
     * No exige sesión: el POST viene del dominio de Transbank y el navegador
     * no manda la cookie de sesión en un POST entre sitios.
     */
    @RequestMapping(value = "/pago/retorno", method = {RequestMethod.GET, RequestMethod.POST})
    public String retorno(@RequestParam(name = "token_ws", required = false) String tokenWs,
                          @RequestParam(name = "TBK_TOKEN", required = false) String tbkToken,
                          @RequestParam(name = "TBK_ORDEN_COMPRA", required = false) String tbkOrden,
                          @RequestParam(name = "TBK_ID_SESION", required = false) String tbkSesion,
                          Model model) {
        try {
            if (tokenWs != null && tbkToken == null) {
                return "redirect:/compras/" + compras.confirmarPago(tokenWs).getCodigo();
            }
            if (tbkOrden != null) {
                return "redirect:/compras/" + compras.anularPorAbandono(tbkOrden, tbkSesion, tbkToken).getCodigo();
            }
        } catch (PagoException e) {
            log.error("No se pudo confirmar el pago {}", tokenWs, e);
            model.addAttribute("mensaje", "No pudimos confirmar tu pago con Webpay. Si la transacción no se "
                    + "confirma, Transbank no realiza el cargo. Revisa «Mis entradas» en unos minutos.");
            return "compra/problema";
        } catch (NoEncontradoException | ReglaNegocioException e) {
            log.warn("Retorno de Webpay que no corresponde a ninguna orden: {}", e.getMessage());
        }
        model.addAttribute("mensaje", "La respuesta de Webpay no corresponde a ninguna compra.");
        return "compra/problema";
    }

    @GetMapping("/compras/{codigo}")
    public String resultado(@PathVariable String codigo, Authentication sesion, Model model) {
        Orden orden = ordenes.findByCodigo(codigo).orElseThrow(() -> new NoEncontradoException("La orden"));
        Acceso.exigirDuenoOAdmin(orden, sesion);
        model.addAttribute("orden", orden);
        return "compra/resultado";
    }
}
