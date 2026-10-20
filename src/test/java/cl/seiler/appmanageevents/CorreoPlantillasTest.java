package cl.seiler.appmanageevents;

import cl.seiler.appmanageevents.dominio.*;
import cl.seiler.appmanageevents.servicio.CorreoServicio;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/** Cada plantilla de correo se arma sin errores y lleva lo esencial. */
@SpringBootTest
@ActiveProfiles("test")
class CorreoPlantillasTest {

    @Autowired CorreoServicio correo;

    @Test
    void todasLasPlantillasSeArman() {
        Usuario u = new Usuario();
        u.setNombre("Ana");
        u.setEmail("ana@correo.cl");
        u.setRecibeNotificaciones(true);

        Salon s = new Salon();
        s.setNombre("Teatro Central");
        s.setDireccion("Av. Siempre Viva 742");

        Evento e = new Evento();
        e.setId(7L);
        e.setTitulo("Concierto de prueba");
        e.setDescripcion("Una noche especial");
        e.setFechaInicio(LocalDateTime.of(2099, 5, 20, 21, 0));
        e.setSalon(s);
        e.setPrecio(15_000);
        e.setCupoMaximo(100);

        Orden o = new Orden();
        o.setCodigo("AME990520ABCDEFGH");
        o.setEvento(e);
        o.setUsuario(u);
        o.setCantidad(2);
        o.setMonto(30_000);
        o.setCodigoAutorizacion("1213");
        o.setPagadaEn(Instant.now());
        Entrada en = new Entrada();
        en.setCodigo("0f8e2f7c-1111-2222-3333-444455556666");
        en.setOrden(o);

        Map<String, Object> base = Map.of("usuario", u, "evento", e, "orden", o, "entradas", List.of(en),
                "urlEntradas", "http://localhost/cuenta/entradas", "urlEvento", "http://localhost/eventos/7",
                "urlBaja", "http://localhost/notificaciones/baja?t=x", "texto", "Hola\nmundo");

        for (String plantilla : List.of("bienvenida", "compra", "nuevo-evento", "recordatorio", "cancelado",
                "reembolso", "mensaje")) {
            String html = correo.armar("Asunto", plantilla, base);
            assertThat(html).as(plantilla).contains("Ana").contains("http://localhost");
        }
        assertThat(correo.armar("x", "compra", base))
                .contains("$30.000").contains("0f8e2f7c-1111-2222-3333-444455556666").contains("Teatro Central");
        assertThat(correo.armar("x", "nuevo-evento", base)).contains("$15.000").contains("notificaciones/baja");
    }
}
