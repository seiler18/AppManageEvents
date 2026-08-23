package cl.talentodigital.appmanageevents.config;

import java.time.LocalDate;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import cl.talentodigital.appmanageevents.entities.Evento;
import cl.talentodigital.appmanageevents.entities.Salon;
import cl.talentodigital.appmanageevents.repositories.EventoRepositorio;
import cl.talentodigital.appmanageevents.repositories.SalonRepositorio;

/**
 * Siembra salones y eventos de muestra, pero SOLO si la base esta vacia.
 *
 * En desarrollo esto no hace nada: con H2, data.sql ya los inserta al arrancar
 * y aqui se encuentra la base poblada. Existe por produccion: data.sql son
 * INSERT fijos sin condicion, asi que con una base que persiste se volverian a
 * insertar en cada reinicio y los salones se irian duplicando. Por eso el
 * perfil prod pone spring.sql.init.mode=never y siembra esta clase.
 */
@Component
@Order(1) // antes que UsuarioDemo, para que el log se lea en orden
public class DatosDemo implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(DatosDemo.class);

    private final SalonRepositorio salonRepositorio;
    private final EventoRepositorio eventoRepositorio;

    public DatosDemo(SalonRepositorio salonRepositorio, EventoRepositorio eventoRepositorio) {
        this.salonRepositorio = salonRepositorio;
        this.eventoRepositorio = eventoRepositorio;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        if (salonRepositorio.count() > 0) {
            return;
        }

        Salon a = salon("Salon A", 100, "Un salon amplio y moderno.");
        Salon b = salon("Salon B", 200, "Salon con excelente iluminacion natural.");
        Salon c = salon("Salon C", 150, "Salon equipado con tecnologia avanzada.");
        Salon d = salon("Salon D", 250, "Gran salon con vistas panoramicas.");
        Salon e = salon("Salon E", 50, "Salon pequeno pero acogedor.");
        salonRepositorio.saveAll(List.of(a, b, c, d, e));

        eventoRepositorio.saveAll(List.of(
                evento("Conferencia Tech", LocalDate.of(2023, 7, 15), 98, a),
                evento("Feria de Empleo", LocalDate.of(2023, 8, 20), 200, b),
                evento("Congreso Medico", LocalDate.of(2023, 9, 10), 129, c),
                evento("Reunion Anual", LocalDate.of(2023, 10, 5), 249, d),
                evento("Seminario de Educacion", LocalDate.of(2023, 11, 25), 39, e),
                evento("Evento Adicional", LocalDate.of(2023, 12, 10), 75, a)));

        log.info("Datos de demo cargados: 5 salones y 6 eventos");
    }

    private Salon salon(String nombre, int capacidad, String descripcion) {
        Salon s = new Salon();
        s.setNombre(nombre);
        s.setCapacidad(capacidad);
        s.setDescripcion(descripcion);
        return s;
    }

    private Evento evento(String nombre, LocalDate fecha, int invitados, Salon salon) {
        Evento ev = new Evento();
        ev.setNombre(nombre);
        ev.setFecha(fecha);
        ev.setInvitados(invitados);
        ev.setSalon(salon);
        return ev;
    }
}
