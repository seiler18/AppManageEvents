package cl.talentodigital.appmanageevents.config;

import java.util.Collections;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import cl.talentodigital.appmanageevents.entities.User;
import cl.talentodigital.appmanageevents.repositories.UserRepository;

/**
 * Crea la cuenta de demostracion al arrancar.
 *
 * data.sql siembra salones y eventos pero ningun usuario, y todo lo interesante
 * de la app esta detras del login: sin esto, quien abre el enlace del portafolio
 * solo puede mirar la pantalla de acceso.
 *
 * La contrasena se cifra con el PasswordEncoder de la aplicacion en vez de
 * dejar un hash escrito en data.sql: asi no hay un hash que se quede obsoleto
 * si algun dia cambia el algoritmo.
 */
@Component
public class UsuarioDemo implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(UsuarioDemo.class);

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.demo.password:demo123}")
    private String demoPassword;

    public UsuarioDemo(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(ApplicationArguments args) {
        if (userRepository.findByUsername("demo").isPresent()) {
            return;
        }
        User demo = new User();
        demo.setUsername("demo");
        demo.setPassword(passwordEncoder.encode(demoPassword));
        demo.setRoles(Collections.singletonList("USER"));
        userRepository.save(demo);
        log.info("Cuenta de demostracion creada");
    }
}
