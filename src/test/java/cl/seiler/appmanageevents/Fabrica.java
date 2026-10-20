package cl.seiler.appmanageevents;

import cl.seiler.appmanageevents.dominio.*;
import cl.seiler.appmanageevents.repositorio.EventoRepositorio;
import cl.seiler.appmanageevents.repositorio.UsuarioRepositorio;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Crea datos de prueba. Cada llamada usa nombres únicos: las pruebas comparten
 * la base H2 y así no dependen del orden en que corren ni se pisan.
 */
@Component
public class Fabrica {

    private final UsuarioRepositorio usuarios;
    private final EventoRepositorio eventos;
    private final PasswordEncoder claves;

    public Fabrica(UsuarioRepositorio usuarios, EventoRepositorio eventos, PasswordEncoder claves) {
        this.usuarios = usuarios;
        this.eventos = eventos;
        this.claves = claves;
    }

    public Usuario usuario() {
        return usuario(Rol.USUARIO);
    }

    public Usuario usuario(Rol rol) {
        String id = UUID.randomUUID().toString().substring(0, 8);
        Usuario u = new Usuario();
        u.setEmail("u" + id + "@prueba.cl");
        u.setNombre("Persona " + id);
        u.setPasswordHash(claves.encode("clave12345"));
        u.setRol(rol);
        u.setTokenBaja("t" + UUID.randomUUID().toString().replace("-", ""));
        u.setCreadoEn(Instant.now());
        return usuarios.save(u);
    }

    public Evento evento(int precio, int cupo) {
        Evento e = new Evento();
        e.setTitulo("Evento " + UUID.randomUUID().toString().substring(0, 8));
        e.setDescripcion("Descripción de prueba");
        e.setFechaInicio(LocalDateTime.now().plusDays(10).withNano(0));
        e.setPrecio(precio);
        e.setCupoMaximo(cupo);
        e.setMaxPorCompra(Math.min(10, cupo));
        e.setEstado(EstadoEvento.PUBLICADO);
        e.setCreadoEn(Instant.now());
        return eventos.save(e);
    }
}
