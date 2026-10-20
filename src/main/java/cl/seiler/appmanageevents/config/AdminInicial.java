package cl.seiler.appmanageevents.config;

import cl.seiler.appmanageevents.dominio.Rol;
import cl.seiler.appmanageevents.servicio.UsuarioServicio;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

/**
 * Crea la primera cuenta administradora con ADMIN_EMAIL y ADMIN_PASSWORD.
 * Si la cuenta ya existe no la toca: cambiar la variable después no cambia
 * la contraseña, y se puede borrar la variable una vez creada la cuenta.
 */
@Component
public class AdminInicial implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(AdminInicial.class);

    private final AppProperties app;
    private final UsuarioServicio usuarios;

    public AdminInicial(AppProperties app, UsuarioServicio usuarios) {
        this.app = app;
        this.usuarios = usuarios;
    }

    @Override
    public void run(ApplicationArguments args) {
        AppProperties.Admin admin = app.admin();
        if (admin == null || !StringUtils.hasText(admin.email()) || !StringUtils.hasText(admin.password())) {
            return;
        }
        usuarios.crearSiNoExiste(admin.email(), "Administración", admin.password(), Rol.ADMIN)
                .ifPresent(u -> log.info("Cuenta administradora creada: {}", u.getEmail()));
    }
}
