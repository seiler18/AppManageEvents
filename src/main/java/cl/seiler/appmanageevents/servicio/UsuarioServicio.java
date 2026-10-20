package cl.seiler.appmanageevents.servicio;

import cl.seiler.appmanageevents.dominio.Rol;
import cl.seiler.appmanageevents.dominio.Usuario;
import cl.seiler.appmanageevents.repositorio.UsuarioRepositorio;
import cl.seiler.appmanageevents.web.formulario.ClaveForm;
import cl.seiler.appmanageevents.web.formulario.PerfilForm;
import cl.seiler.appmanageevents.web.formulario.RegistroForm;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

@Service
@Transactional(readOnly = true)
public class UsuarioServicio implements UserDetailsService {

    private final UsuarioRepositorio usuarios;
    private final PasswordEncoder claves;
    private final ApplicationEventPublisher sucesos;
    private final Clock reloj;

    public UsuarioServicio(UsuarioRepositorio usuarios, PasswordEncoder claves,
                           ApplicationEventPublisher sucesos, Clock reloj) {
        this.usuarios = usuarios;
        this.claves = claves;
        this.sucesos = sucesos;
        this.reloj = reloj;
    }

    @Override
    public UserDetails loadUserByUsername(String email) {
        Usuario u = usuarios.findByEmailIgnoreCase(normalizar(email))
                .orElseThrow(() -> new UsernameNotFoundException("Sin cuenta"));
        return User.withUsername(u.getEmail())
                .password(u.getPasswordHash())
                .roles(u.getRol().name())
                .build();
    }

    public Usuario porEmail(String email) {
        return usuarios.findByEmailIgnoreCase(normalizar(email))
                .orElseThrow(() -> new NoEncontradoException("El usuario"));
    }

    public List<Usuario> todos() {
        return usuarios.findAll(Sort.by(Sort.Direction.DESC, "creadoEn"));
    }

    public long suscritos() {
        return usuarios.countByRecibeNotificacionesTrue();
    }

    public long total() {
        return usuarios.count();
    }

    @Transactional
    public Usuario registrar(RegistroForm form) {
        String email = normalizar(form.getEmail());
        if (usuarios.existsByEmailIgnoreCase(email)) {
            throw new ReglaNegocioException("Ya existe una cuenta con ese correo. ¿Quieres iniciar sesión?");
        }
        Usuario u = crear(email, form.getNombre().trim(), form.getPassword(), Rol.USUARIO);
        u.setRecibeNotificaciones(form.isRecibeNotificaciones());
        sucesos.publishEvent(new Sucesos.UsuarioRegistrado(u.getId()));
        return u;
    }

    /** Para el arranque: crea la cuenta si no existe. No toca una que ya exista. */
    @Transactional
    public Optional<Usuario> crearSiNoExiste(String email, String nombre, String password, Rol rol) {
        if (usuarios.existsByEmailIgnoreCase(normalizar(email))) {
            return Optional.empty();
        }
        return Optional.of(crear(normalizar(email), nombre, password, rol));
    }

    @Transactional
    public void actualizarPerfil(String email, PerfilForm form) {
        Usuario u = porEmail(email);
        u.setNombre(form.getNombre().trim());
        u.setRecibeNotificaciones(form.isRecibeNotificaciones());
    }

    @Transactional
    public void cambiarClave(String email, ClaveForm form) {
        Usuario u = porEmail(email);
        if (!claves.matches(form.getActual(), u.getPasswordHash())) {
            throw new ReglaNegocioException("La contraseña actual no es correcta.");
        }
        u.setPasswordHash(claves.encode(form.getNueva()));
    }

    /** Baja desde el enlace del correo. Devuelve false si el enlace no es válido. */
    @Transactional
    public boolean darDeBaja(String token) {
        return usuarios.findByTokenBaja(token).map(u -> {
            u.setRecibeNotificaciones(false);
            return true;
        }).orElse(false);
    }

    @Transactional
    public void cambiarRol(Long id, Rol rol, String emailQuienPide) {
        Usuario u = usuarios.findById(id).orElseThrow(() -> new NoEncontradoException("El usuario"));
        if (u.getEmail().equalsIgnoreCase(emailQuienPide) && rol != Rol.ADMIN) {
            throw new ReglaNegocioException("No puedes quitarte a ti mismo el rol de administrador.");
        }
        u.setRol(rol);
    }

    private Usuario crear(String email, String nombre, String password, Rol rol) {
        Usuario u = new Usuario();
        u.setEmail(email);
        u.setNombre(nombre);
        u.setPasswordHash(claves.encode(password));
        u.setRol(rol);
        u.setTokenBaja(Codigos.secreto());
        u.setCreadoEn(reloj.instant());
        return usuarios.save(u);
    }

    private static String normalizar(String email) {
        return email == null ? "" : email.trim().toLowerCase(Locale.ROOT);
    }
}
