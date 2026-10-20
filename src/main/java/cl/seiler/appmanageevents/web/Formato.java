package cl.seiler.appmanageevents.web;

import cl.seiler.appmanageevents.config.AppProperties;
import org.springframework.stereotype.Component;

import java.text.NumberFormat;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

/** Formatos chilenos para las vistas y los correos: {@code ${@fmt.clp(evento.precio)}}. */
@Component("fmt")
public class Formato {

    private static final Locale CHILE = Locale.of("es", "CL");
    private static final DateTimeFormatter LARGA = DateTimeFormatter.ofPattern("EEEE d 'de' MMMM 'de' yyyy, HH:mm 'h'", CHILE);
    private static final DateTimeFormatter CORTA = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm", CHILE);
    private static final DateTimeFormatter DIA = DateTimeFormatter.ofPattern("d MMM", CHILE);
    private static final DateTimeFormatter INPUT = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm");

    private final ZoneId zona;

    public Formato(AppProperties app) {
        this.zona = ZoneId.of(app.zonaHoraria());
    }

    public String clp(Number monto) {
        if (monto == null) {
            return "";
        }
        NumberFormat nf = NumberFormat.getIntegerInstance(CHILE);
        return "$" + nf.format(monto.longValue());
    }

    public String precio(int monto) {
        return monto == 0 ? "Gratis" : clp(monto);
    }

    public String fecha(LocalDateTime f) {
        if (f == null) {
            return "";
        }
        String s = f.format(LARGA);
        return Character.toUpperCase(s.charAt(0)) + s.substring(1);
    }

    public String corta(LocalDateTime f) {
        return f == null ? "" : f.format(CORTA);
    }

    public String corta(Instant i) {
        return i == null ? "" : LocalDateTime.ofInstant(i, zona).format(CORTA);
    }

    public String dia(LocalDateTime f) {
        return f == null ? "" : f.format(DIA).replace(".", "");
    }

    /** Valor para un input datetime-local. */
    public String input(LocalDateTime f) {
        return f == null ? "" : f.format(INPUT);
    }
}
