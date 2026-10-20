package cl.seiler.appmanageevents.servicio;

import cl.seiler.appmanageevents.config.AppProperties;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.thymeleaf.ITemplateEngine;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.expression.ThymeleafEvaluationContext;

import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

@Service
public class CorreoServicio {

    private static final Logger log = LoggerFactory.getLogger(CorreoServicio.class);
    private static final Locale CHILE = Locale.of("es", "CL");

    private final ObjectProvider<JavaMailSender> remitente;
    private final ITemplateEngine plantillas;
    private final AppProperties app;
    private final ApplicationContext contexto;
    private final boolean configurado;

    public CorreoServicio(ObjectProvider<JavaMailSender> remitente, ITemplateEngine plantillas, AppProperties app,
                          ApplicationContext contexto, @Value("${spring.mail.host:}") String host) {
        this.remitente = remitente;
        this.plantillas = plantillas;
        this.app = app;
        this.contexto = contexto;
        // spring.mail.host vacío igual crea el JavaMailSender; se mira el valor.
        this.configurado = StringUtils.hasText(host);
        if (!configurado) {
            log.warn("Sin MAIL_HOST: los correos no se envían, solo se registran en el log");
        }
    }

    /** Renderiza la plantilla de templates/correo/; separado del envío para poder probar cada una. */
    public String armar(String asunto, String plantilla, Map<String, Object> variables) {
        Map<String, Object> vars = new HashMap<>(variables);
        vars.put("sitio", app.nombreSitio());
        vars.put("urlBase", app.url(""));
        vars.put("asunto", asunto);
        // Sin esto, fuera de una petición web las plantillas no resuelven @fmt.
        vars.put(ThymeleafEvaluationContext.THYMELEAF_EVALUATION_CONTEXT_CONTEXT_VARIABLE_NAME,
                new ThymeleafEvaluationContext(contexto, null));
        return plantillas.process("correo/" + plantilla, new Context(CHILE, vars));
    }

    /**
     * Envía un correo HTML. Nunca lanza: un correo que no sale no debe tumbar
     * una compra ya pagada. Devuelve si salió.
     */
    public boolean enviar(String para, String asunto, String plantilla, Map<String, Object> variables) {
        String html;
        try {
            html = armar(asunto, plantilla, variables);
        } catch (RuntimeException e) {
            log.error("No se pudo armar el correo «{}» ({})", asunto, plantilla, e);
            return false;
        }
        if (!configurado) {
            log.info("Correo no enviado (sin SMTP) a {}: «{}»", para, asunto);
            log.debug(html);
            return false;
        }
        try {
            JavaMailSender mailer = remitente.getObject();
            MimeMessage mensaje = mailer.createMimeMessage();
            MimeMessageHelper ayudante = new MimeMessageHelper(mensaje, false, StandardCharsets.UTF_8.name());
            ayudante.setFrom(app.correo().remitente(), app.nombreSitio());
            ayudante.setTo(para);
            ayudante.setSubject(asunto);
            ayudante.setText(html, true);
            mailer.send(mensaje);
            return true;
        } catch (MailException | MessagingException | UnsupportedEncodingException e) {
            log.error("No se pudo enviar «{}» a {}", asunto, para, e);
            return false;
        }
    }
}
