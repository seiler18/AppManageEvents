package cl.seiler.appmanageevents.dominio;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Entity
@Getter
@Setter
public class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 160)
    private String email;

    @Column(nullable = false, length = 120)
    private String nombre;

    @Column(name = "password_hash", nullable = false, length = 100)
    private String passwordHash;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Rol rol = Rol.USUARIO;

    @Column(name = "recibe_notificaciones", nullable = false)
    private boolean recibeNotificaciones = true;

    /** Va en el enlace de baja de cada correo: deja desuscribirse sin iniciar sesión. */
    @Column(name = "token_baja", nullable = false, length = 40)
    private String tokenBaja;

    @Column(name = "creado_en", nullable = false)
    private Instant creadoEn;

    public boolean esAdmin() {
        return rol == Rol.ADMIN;
    }
}
