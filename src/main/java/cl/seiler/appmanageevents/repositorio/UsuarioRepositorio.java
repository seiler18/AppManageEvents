package cl.seiler.appmanageevents.repositorio;

import cl.seiler.appmanageevents.dominio.Usuario;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UsuarioRepositorio extends JpaRepository<Usuario, Long> {

    Optional<Usuario> findByEmailIgnoreCase(String email);

    boolean existsByEmailIgnoreCase(String email);

    Optional<Usuario> findByTokenBaja(String tokenBaja);

    List<Usuario> findByRecibeNotificacionesTrue();

    long countByRecibeNotificacionesTrue();

    List<Usuario> findAll(Sort orden);
}
