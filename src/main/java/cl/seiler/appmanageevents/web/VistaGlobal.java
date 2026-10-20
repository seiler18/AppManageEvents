package cl.seiler.appmanageevents.web;

import cl.seiler.appmanageevents.config.AppProperties;
import cl.seiler.appmanageevents.pago.TransbankProperties;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

@ControllerAdvice(basePackages = "cl.seiler.appmanageevents.web")
public class VistaGlobal {

    private final AppProperties app;
    private final TransbankProperties tbk;

    public VistaGlobal(AppProperties app, TransbankProperties tbk) {
        this.app = app;
        this.tbk = tbk;
    }

    @ModelAttribute("sitio")
    public String sitio() {
        return app.nombreSitio();
    }

    /** Muestra un aviso de «modo prueba» mientras Webpay no esté en producción. */
    @ModelAttribute("pagosDePrueba")
    public boolean pagosDePrueba() {
        return !tbk.esProduccion();
    }
}
