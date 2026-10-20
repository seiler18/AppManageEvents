package cl.seiler.appmanageevents;

import cl.seiler.appmanageevents.dominio.EstadoOrden;
import cl.seiler.appmanageevents.dominio.Orden;
import cl.seiler.appmanageevents.pago.PasarelaPago;
import cl.seiler.appmanageevents.servicio.CompraServicio;
import io.zonky.test.db.postgres.embedded.EmbeddedPostgres;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Las migraciones y el bloqueo de cupo, contra un Postgres 17 real como el de
 * Supabase. H2 en modo PostgreSQL se parece, pero no es lo mismo.
 */
@SpringBootTest
@ActiveProfiles("test")
class PostgresTest {

    private static final EmbeddedPostgres PG;

    static {
        try {
            PG = EmbeddedPostgres.start();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    @DynamicPropertySource
    static void baseDeDatos(DynamicPropertyRegistry r) {
        r.add("spring.datasource.url", () -> PG.getJdbcUrl("postgres", "postgres"));
        r.add("spring.datasource.username", () -> "postgres");
        r.add("spring.datasource.password", () -> "postgres");
    }

    @AfterAll
    static void apagar() throws IOException {
        PG.close();
    }

    @Autowired JdbcTemplate jdbc;
    @Autowired CompraServicio compras;
    @Autowired Fabrica fabrica;

    @MockitoBean PasarelaPago pasarela;

    @Test
    void lasMigracionesCorrenYCierranLaApiDeSupabase() {
        List<String> sinRls = jdbc.queryForList("""
                select relname from pg_class
                where relnamespace = 'public'::regnamespace and relkind = 'r'
                  and relname <> 'flyway_schema_history' and not relrowsecurity
                """, String.class);
        assertThat(sinRls).isEmpty();
        assertThat(jdbc.queryForObject("select count(*) from flyway_schema_history where success", Integer.class))
                .isEqualTo(2);
    }

    @Test
    void laBaseRechazaVenderMasDelCupoAunqueElCodigoFalle() {
        var e = fabrica.evento(1_000, 3);
        Integer filas = null;
        try {
            filas = jdbc.update("update evento set entradas_reservadas = 4 where id = ?", e.getId());
        } catch (org.springframework.dao.DataIntegrityViolationException esperado) {
            // la restricción ck_evento_cupo frenó la escritura
        }
        assertThat(filas).isNull();
    }

    @Test
    void compraGratuitaDeExtremoAExtremo() {
        var e = fabrica.evento(0, 10);
        Orden o = compras.reservar(fabrica.usuario().getId(), e.getId(), 3);
        assertThat(o.getEstado()).isEqualTo(EstadoOrden.PAGADA);
        assertThat(jdbc.queryForObject("select count(*) from entrada where orden_id = ?", Integer.class, o.getId()))
                .isEqualTo(3);
    }
}
