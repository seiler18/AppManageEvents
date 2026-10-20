package cl.seiler.appmanageevents.web;

import cl.seiler.appmanageevents.dominio.Orden;
import cl.seiler.appmanageevents.servicio.NoEncontradoException;
import org.springframework.security.core.Authentication;

final class Acceso {

    private Acceso() {
    }

    /**
     * La orden ajena se responde como inexistente, no como prohibida: así el
     * código de orden no sirve para saber si existe una compra de otro.
     */
    static void exigirDuenoOAdmin(Orden orden, Authentication sesion) {
        boolean admin = sesion.getAuthorities().stream().anyMatch(a -> "ROLE_ADMIN".equals(a.getAuthority()));
        if (!admin && !orden.getUsuario().getEmail().equalsIgnoreCase(sesion.getName())) {
            throw new NoEncontradoException("La orden");
        }
    }
}
