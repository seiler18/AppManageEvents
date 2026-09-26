package cl.talentodigital.appmanageevents.controllers;

import cl.talentodigital.appmanageevents.entities.User;
import cl.talentodigital.appmanageevents.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.nio.charset.StandardCharsets;
import java.util.Collections;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@RequestBody User signUpUser) {
        String username = signUpUser.getUsername();
        String password = signUpUser.getPassword();

        // Sin esta validacion, un cuerpo sin contrasena o sin usuario llegaba
        // hasta el encoder o la base y devolvia un 500. El tope de 60 es el
        // largo de la columna username.
        if (username == null || username.isBlank() || username.length() > 60) {
            return ResponseEntity.badRequest().body("Error: el usuario es obligatorio (maximo 60 caracteres).");
        }

        // El usuario repetido se comprueba antes que la contrasena: es el
        // orden que ya esperaban ControllerTest y el formulario de registro.
        if (userRepository.findByUsername(username).isPresent()) {
            return ResponseEntity
                    .badRequest()
                    .body("Error: Username is already taken!");
        }

        // BCrypt solo mira los primeros 72 bytes: dos contrasenas que
        // compartan ese prefijo serian la misma (CVE-2025-22228), y desde
        // Spring Security 6.3.8 el encoder lanza excepcion por encima de ese
        // largo. Se rechaza aqui con un 400 en vez de un 500.
        if (password == null || password.isBlank()
                || password.getBytes(StandardCharsets.UTF_8).length > 72) {
            return ResponseEntity.badRequest().body("Error: la contrasena es obligatoria (maximo 72 bytes).");
        }

        User user = new User();
        user.setUsername(username);
        user.setPassword(passwordEncoder.encode(password));
        user.setRoles(Collections.singletonList("USER"));
        userRepository.save(user);

        return ResponseEntity.ok("User registered successfully!");
    }
}