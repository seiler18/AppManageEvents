package cl.seiler.appmanageevents.repositorio;

import cl.seiler.appmanageevents.dominio.Entrada;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.Optional;

public interface EntradaRepositorio extends JpaRepository<Entrada, Long> {

    @EntityGraph(attributePaths = {"orden", "orden.evento", "orden.evento.salon", "orden.usuario"})
    Optional<Entrada> findByCodigo(String codigo);

    @Modifying
    @Query("update Entrada e set e.usadaEn = :ahora where e.id = :id and e.usadaEn is null")
    int marcarUsada(@Param("id") Long id, @Param("ahora") Instant ahora);
}
