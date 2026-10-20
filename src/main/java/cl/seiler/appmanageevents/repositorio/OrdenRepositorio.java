package cl.seiler.appmanageevents.repositorio;

import cl.seiler.appmanageevents.dominio.EstadoOrden;
import cl.seiler.appmanageevents.dominio.Orden;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface OrdenRepositorio extends JpaRepository<Orden, Long> {

    /** Se pide siempre después de bloquear su evento: mismo orden de bloqueos, sin interbloqueos. */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select o from Orden o where o.id = :id")
    Optional<Orden> bloquear(@Param("id") Long id);

    @Query("select new cl.seiler.appmanageevents.repositorio.RefOrden(o.id, o.evento.id) from Orden o where o.tokenWebpay = :token")
    Optional<RefOrden> referenciaPorToken(@Param("token") String token);

    @Query("select new cl.seiler.appmanageevents.repositorio.RefOrden(o.id, o.evento.id) from Orden o where o.codigo = :codigo")
    Optional<RefOrden> referenciaPorCodigo(@Param("codigo") String codigo);

    @Query("select new cl.seiler.appmanageevents.repositorio.RefOrden(o.id, o.evento.id) from Orden o where o.estado = :estado and o.expiraEn < :ahora")
    List<RefOrden> vencidas(@Param("estado") EstadoOrden estado, @Param("ahora") Instant ahora);

    @Query("select o.id from Orden o where o.evento.id = :eventoId and o.estado = :estado and o.expiraEn < :ahora")
    List<Long> vencidasDeEvento(@Param("eventoId") Long eventoId, @Param("estado") EstadoOrden estado, @Param("ahora") Instant ahora);

    boolean existsByEventoId(Long eventoId);

    @EntityGraph(attributePaths = {"evento", "evento.salon", "usuario", "entradas"})
    Optional<Orden> findByCodigo(String codigo);

    @EntityGraph(attributePaths = {"evento", "usuario"})
    Optional<Orden> findByTokenWebpay(String token);

    @EntityGraph(attributePaths = {"evento", "evento.salon", "entradas"})
    List<Orden> findByUsuarioIdAndEstadoOrderByCreadaEnDesc(Long usuarioId, EstadoOrden estado);

    @EntityGraph(attributePaths = {"evento", "usuario"})
    List<Orden> findTop200ByOrderByCreadaEnDesc();

    @EntityGraph(attributePaths = {"evento", "usuario"})
    List<Orden> findTop200ByEstadoOrderByCreadaEnDesc(EstadoOrden estado);

    @EntityGraph(attributePaths = {"usuario", "entradas"})
    List<Orden> findByEventoIdOrderByCreadaEnDesc(Long eventoId);

    @EntityGraph(attributePaths = {"usuario"})
    List<Orden> findByEventoIdAndEstado(Long eventoId, EstadoOrden estado);

    @Query("select coalesce(sum(o.monto), 0) from Orden o where o.estado = cl.seiler.appmanageevents.dominio.EstadoOrden.PAGADA")
    long recaudacionTotal();

    @Query("select coalesce(sum(o.cantidad), 0) from Orden o where o.estado = cl.seiler.appmanageevents.dominio.EstadoOrden.PAGADA")
    long entradasVendidas();

    @Query("select coalesce(sum(o.monto), 0) from Orden o where o.evento.id = :eventoId and o.estado = cl.seiler.appmanageevents.dominio.EstadoOrden.PAGADA")
    long recaudacionEvento(@Param("eventoId") Long eventoId);
}
