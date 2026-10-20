package cl.seiler.appmanageevents.config;

import cl.seiler.appmanageevents.dominio.EstadoEvento;
import cl.seiler.appmanageevents.dominio.Evento;
import cl.seiler.appmanageevents.dominio.Rol;
import cl.seiler.appmanageevents.dominio.Salon;
import cl.seiler.appmanageevents.repositorio.EventoRepositorio;
import cl.seiler.appmanageevents.repositorio.SalonRepositorio;
import cl.seiler.appmanageevents.servicio.UsuarioServicio;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

/**
 * Datos para probar en local: dos cuentas, salones y eventos con fechas
 * relativas a hoy (así nunca quedan en el pasado). Solo con el perfil dev,
 * que es el de por defecto; producción corre con el perfil prod.
 */
@Component
@Profile("dev")
public class DatosDemo implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(DatosDemo.class);

    private final SalonRepositorio salones;
    private final EventoRepositorio eventos;
    private final UsuarioServicio usuarios;
    private final Clock reloj;

    public DatosDemo(SalonRepositorio salones, EventoRepositorio eventos, UsuarioServicio usuarios, Clock reloj) {
        this.salones = salones;
        this.eventos = eventos;
        this.usuarios = usuarios;
        this.reloj = reloj;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        usuarios.crearSiNoExiste("admin@demo.cl", "Admin Demo", "admin12345", Rol.ADMIN);
        usuarios.crearSiNoExiste("usuario@demo.cl", "Usuario Demo", "usuario12345", Rol.USUARIO);
        if (salones.count() > 0) {
            return;
        }
        Salon teatro = salon("Teatro Central", "Av. Providencia 1234, Santiago", 300);
        Salon auditorio = salon("Auditorio Norte", "Calle Los Aromos 55, Viña del Mar", 120);
        Salon terraza = salon("Terraza Andes", "Camino a Farellones km 3", 60);
        salones.saveAll(List.of(teatro, auditorio, terraza));

        LocalDate hoy = LocalDate.now(reloj);
        eventos.saveAll(List.of(
                evento("Concierto de Jazz de Primavera", "Una noche con cuarteto en vivo y estándares del jazz.",
                        hoy.plusDays(12), LocalTime.of(20, 30), teatro, 15000, 250, EstadoEvento.PUBLICADO),
                evento("Taller de Fotografía Urbana", "Salida práctica por el centro con un fotógrafo profesional.",
                        hoy.plusDays(5), LocalTime.of(10, 0), auditorio, 8000, 25, EstadoEvento.PUBLICADO),
                evento("Charla abierta: IA en la empresa", "Charla gratuita; cupos limitados.",
                        hoy.plusDays(20), LocalTime.of(18, 0), auditorio, 0, 100, EstadoEvento.PUBLICADO),
                evento("Cata de vinos al atardecer", "Seis cepas chilenas con maridaje.",
                        hoy.plusDays(30), LocalTime.of(19, 0), terraza, 32000, 40, EstadoEvento.BORRADOR)));
        log.info("Datos de demo cargados: admin@demo.cl / admin12345 y usuario@demo.cl / usuario12345");
    }

    private Salon salon(String nombre, String direccion, int capacidad) {
        Salon s = new Salon();
        s.setNombre(nombre);
        s.setDireccion(direccion);
        s.setCapacidad(capacidad);
        return s;
    }

    private Evento evento(String titulo, String descripcion, LocalDate dia, LocalTime hora, Salon salon,
                          int precio, int cupo, EstadoEvento estado) {
        Evento e = new Evento();
        e.setTitulo(titulo);
        e.setDescripcion(descripcion);
        e.setFechaInicio(dia.atTime(hora));
        e.setSalon(salon);
        e.setPrecio(precio);
        e.setCupoMaximo(cupo);
        e.setMaxPorCompra(Math.min(10, cupo));
        e.setEstado(estado);
        e.setCreadoEn(reloj.instant());
        return e;
    }
}
