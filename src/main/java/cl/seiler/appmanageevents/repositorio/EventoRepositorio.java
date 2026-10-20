package cl.seiler.appmanageevents.repositorio;

import cl.seiler.appmanageevents.dominio.EstadoEvento;
import cl.seiler.appmanageevents.dominio.Evento;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface EventoRepositorio extends JpaRepository<Evento, Long> {

    /**
     * SELECT ... FOR UPDATE sobre la fila del evento. Dos compras simultáneas
     * del último cupo hacen fila aquí: la segunda lee el contador ya movido por
     * la primera y no puede vender de más.
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select e from Evento e where e.id = :id")
    Optional<Evento> bloquear(@Param("id") Long id);

    @EntityGraph(attributePaths = "salon")
    @Query("select e from Evento e where e.id = :id")
    Optional<Evento> buscarConSalon(@Param("id") Long id);

    @EntityGraph(attributePaths = "salon")
    List<Evento> findByEstadoAndFechaInicioAfterOrderByFechaInicioAsc(EstadoEvento estado, LocalDateTime desde);

    @EntityGraph(attributePaths = "salon")
    List<Evento> findAllByOrderByFechaInicioDesc();

    /** Eventos publicados que empiezan dentro de la ventana y aún no avisaron. */
    @Query("""
            select e from Evento e
            where e.estado = cl.seiler.appmanageevents.dominio.EstadoEvento.PUBLICADO
              and e.recordatorioEnviado = false
              and e.fechaInicio > :desde and e.fechaInicio <= :hasta
            """)
    List<Evento> pendientesDeRecordatorio(@Param("desde") LocalDateTime desde, @Param("hasta") LocalDateTime hasta);

    long countByEstadoAndFechaInicioAfter(EstadoEvento estado, LocalDateTime desde);

    boolean existsBySalonId(Long salonId);
}
