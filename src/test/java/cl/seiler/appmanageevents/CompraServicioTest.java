package cl.seiler.appmanageevents;

import cl.seiler.appmanageevents.dominio.*;
import cl.seiler.appmanageevents.pago.PagoException;
import cl.seiler.appmanageevents.pago.PasarelaPago;
import cl.seiler.appmanageevents.pago.PasarelaPago.ResultadoPago;
import cl.seiler.appmanageevents.pago.PasarelaPago.TransaccionIniciada;
import cl.seiler.appmanageevents.repositorio.EventoRepositorio;
import cl.seiler.appmanageevents.repositorio.OrdenRepositorio;
import cl.seiler.appmanageevents.servicio.CompraServicio;
import cl.seiler.appmanageevents.servicio.CorreoServicio;
import cl.seiler.appmanageevents.servicio.ReglaNegocioException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@SpringBootTest
@ActiveProfiles("test")
class CompraServicioTest {

    @Autowired CompraServicio compras;
    @Autowired EventoRepositorio eventos;
    @Autowired OrdenRepositorio ordenes;
    @Autowired Fabrica fabrica;

    @MockitoBean PasarelaPago pasarela;
    @MockitoBean CorreoServicio correo;

    @BeforeEach
    void webpayResponde() {
        when(pasarela.crear(anyString(), anyString(), anyInt(), anyString()))
                .thenAnswer(inv -> new TransaccionIniciada("tok-" + inv.getArgument(0), "https://webpay.test/init"));
    }

    @Test
    void reservarRetieneCupoYDejaLaOrdenPendiente() {
        Evento e = fabrica.evento(10_000, 50);
        Orden o = compras.reservar(fabrica.usuario().getId(), e.getId(), 3);

        assertThat(o.getEstado()).isEqualTo(EstadoOrden.PENDIENTE);
        assertThat(o.getMonto()).isEqualTo(30_000);
        assertThat(o.getCodigo()).hasSize(17).startsWith("AME");
        assertThat(cupoReservado(e)).isEqualTo(3);
    }

    @Test
    void noVendeMasQueElMaximoDeEntradas() {
        Evento e = fabrica.evento(5_000, 4);
        compras.reservar(fabrica.usuario().getId(), e.getId(), 3);

        assertThatThrownBy(() -> compras.reservar(fabrica.usuario().getId(), e.getId(), 2))
                .isInstanceOf(ReglaNegocioException.class)
                .hasMessageContaining("Solo quedan 1");
        compras.reservar(fabrica.usuario().getId(), e.getId(), 1);
        assertThatThrownBy(() -> compras.reservar(fabrica.usuario().getId(), e.getId(), 1))
                .hasMessageContaining("agotaron");
        assertThat(cupoReservado(e)).isEqualTo(4);
    }

    @Test
    void respetaElMaximoPorCompra() {
        Evento e = fabrica.evento(5_000, 100);
        assertThatThrownBy(() -> compras.reservar(fabrica.usuario().getId(), e.getId(), 11))
                .hasMessageContaining("entre 1 y 10");
        assertThatThrownBy(() -> compras.reservar(fabrica.usuario().getId(), e.getId(), 0))
                .isInstanceOf(ReglaNegocioException.class);
    }

    @Test
    void noVendeEventosEnBorrador() {
        Evento e = fabrica.evento(5_000, 10);
        e.setEstado(EstadoEvento.BORRADOR);
        eventos.save(e);
        assertThatThrownBy(() -> compras.reservar(fabrica.usuario().getId(), e.getId(), 1))
                .hasMessageContaining("no está a la venta");
    }

    @Test
    void unEventoGratuitoEmiteLasEntradasSinPasarPorWebpay() {
        Evento e = fabrica.evento(0, 10);
        Orden o = compras.reservar(fabrica.usuario().getId(), e.getId(), 2);

        assertThat(o.getEstado()).isEqualTo(EstadoOrden.PAGADA);
        assertThat(ordenes.findByCodigo(o.getCodigo()).orElseThrow().getEntradas()).hasSize(2);
        verifyNoInteractions(pasarela);
    }

    @Test
    void pagoAprobadoEmiteUnaEntradaPorCadaUnidad() {
        Evento e = fabrica.evento(12_000, 10);
        Orden o = pagar(e, 2, aprobado(e, 2));

        Orden guardada = ordenes.findByCodigo(o.getCodigo()).orElseThrow();
        assertThat(guardada.getEstado()).isEqualTo(EstadoOrden.PAGADA);
        assertThat(guardada.getEntradas()).hasSize(2)
                .extracting(Entrada::getCodigo).doesNotHaveDuplicates();
        assertThat(guardada.getCodigoAutorizacion()).isEqualTo("1213");
        assertThat(guardada.getTarjetaUltimos4()).isEqualTo("6623");
        assertThat(cupoReservado(e)).isEqualTo(2);
    }

    @Test
    void pagoRechazadoDevuelveElCupo() {
        Evento e = fabrica.evento(12_000, 10);
        Orden o = pagar(e, 4, new ResultadoPago("FAILED", -1, 48_000, null, null, null, "VD", 0, "6623"));

        assertThat(ordenes.findByCodigo(o.getCodigo()).orElseThrow().getEstado()).isEqualTo(EstadoOrden.RECHAZADA);
        assertThat(cupoReservado(e)).isZero();
    }

    @Test
    void confirmarDosVecesNoDuplicaEntradasNiVuelveALlamarAWebpay() {
        Evento e = fabrica.evento(12_000, 10);
        Orden o = pagar(e, 1, aprobado(e, 1));
        compras.confirmarPago(o.getTokenWebpay());

        verify(pasarela, times(1)).confirmar(o.getTokenWebpay());
        assertThat(ordenes.findByCodigo(o.getCodigo()).orElseThrow().getEntradas()).hasSize(1);
    }

    @Test
    void siLaConfirmacionFallaSeConsultaElEstadoReal() {
        Evento e = fabrica.evento(9_000, 10);
        Orden o = iniciar(e, 1);
        when(pasarela.confirmar(o.getTokenWebpay())).thenThrow(new PagoException("422", null));
        ResultadoPago ok = aprobado(e, 1);
        when(pasarela.consultar(o.getTokenWebpay()))
                .thenReturn(new ResultadoPago(ok.estado(), 0, 9_000, o.getCodigo(), o.getIdSesion(), "1213", "VD", 0, "6623"));

        compras.confirmarPago(o.getTokenWebpay());
        assertThat(ordenes.findByCodigo(o.getCodigo()).orElseThrow().estaPagada()).isTrue();
    }

    @Test
    void unPagoQueNoCuadraConLaOrdenSeRechazaYSeDevuelve() {
        Evento e = fabrica.evento(12_000, 10);
        Orden o = iniciar(e, 1);
        when(pasarela.confirmar(o.getTokenWebpay()))
                .thenReturn(new ResultadoPago("AUTHORIZED", 0, 100, o.getCodigo(), o.getIdSesion(), "1", "VD", 0, "1"));

        compras.confirmarPago(o.getTokenWebpay());
        assertThat(ordenes.findByCodigo(o.getCodigo()).orElseThrow().getEstado()).isEqualTo(EstadoOrden.RECHAZADA);
        verify(pasarela).reembolsar(o.getTokenWebpay(), 100);
        assertThat(cupoReservado(e)).isZero();
    }

    @Test
    void abandonoLegitimoAnulaYLiberaCupo() {
        Evento e = fabrica.evento(7_000, 10);
        Orden o = iniciar(e, 2);

        compras.anularPorAbandono(o.getCodigo(), null, o.getTokenWebpay());
        assertThat(ordenes.findByCodigo(o.getCodigo()).orElseThrow().getEstado()).isEqualTo(EstadoOrden.ANULADA);
        assertThat(cupoReservado(e)).isZero();
    }

    @Test
    void nadieAnulaUnaCompraAjenaConSoloElCodigo() {
        Evento e = fabrica.evento(7_000, 10);
        Orden o = iniciar(e, 2);

        assertThatThrownBy(() -> compras.anularPorAbandono(o.getCodigo(), "sesion-falsa", null))
                .isInstanceOf(ReglaNegocioException.class);
        assertThatThrownBy(() -> compras.anularPorAbandono(o.getCodigo(), null, "token-falso"))
                .isInstanceOf(ReglaNegocioException.class);
        assertThat(ordenes.findByCodigo(o.getCodigo()).orElseThrow().getEstado()).isEqualTo(EstadoOrden.PENDIENTE);
        // Con el session_id verdadero (abandono por tiempo) sí se anula.
        compras.anularPorAbandono(o.getCodigo(), o.getIdSesion(), null);
        assertThat(cupoReservado(e)).isZero();
    }

    @Test
    void lasReservasVencidasDevuelvenSuCupo() {
        Evento e = fabrica.evento(7_000, 10);
        Orden o = iniciar(e, 3);
        vencer(o);

        compras.expirarReservas();
        assertThat(ordenes.findByCodigo(o.getCodigo()).orElseThrow().getEstado()).isEqualTo(EstadoOrden.EXPIRADA);
        assertThat(cupoReservado(e)).isZero();
    }

    @Test
    void quienPagaTardeSinCupoRecibeElReembolso() {
        Evento e = fabrica.evento(5_000, 2);
        Orden tarde = iniciar(e, 2);
        vencer(tarde);
        compras.reservar(fabrica.usuario().getId(), e.getId(), 2); // libera la vencida y se queda con el cupo
        when(pasarela.confirmar(tarde.getTokenWebpay()))
                .thenReturn(new ResultadoPago("AUTHORIZED", 0, 10_000, tarde.getCodigo(), tarde.getIdSesion(), "1", "VD", 0, "1"));

        compras.confirmarPago(tarde.getTokenWebpay());
        assertThat(ordenes.findByCodigo(tarde.getCodigo()).orElseThrow().getEstado()).isEqualTo(EstadoOrden.REEMBOLSADA);
        verify(pasarela).reembolsar(tarde.getTokenWebpay(), 10_000);
        assertThat(cupoReservado(e)).isEqualTo(2);
    }

    @Test
    void elReembolsoDelPanelDevuelveDineroYCupo() {
        Evento e = fabrica.evento(8_000, 10);
        Orden o = pagar(e, 2, aprobado(e, 2));

        compras.reembolsar(o.getCodigo());
        verify(pasarela).reembolsar(o.getTokenWebpay(), 16_000);
        assertThat(ordenes.findByCodigo(o.getCodigo()).orElseThrow().getEstado()).isEqualTo(EstadoOrden.REEMBOLSADA);
        assertThat(cupoReservado(e)).isZero();
    }

    @Test
    void comprasSimultaneasNoSobrepasanElCupo() throws Exception {
        Evento e = fabrica.evento(1_000, 5);
        List<Long> compradores = new ArrayList<>();
        for (int i = 0; i < 20; i++) {
            compradores.add(fabrica.usuario().getId());
        }
        AtomicInteger vendidas = new AtomicInteger();
        CountDownLatch largada = new CountDownLatch(1);
        ExecutorService hilos = Executors.newFixedThreadPool(20);
        List<Future<?>> tareas = new ArrayList<>();
        for (Long u : compradores) {
            tareas.add(hilos.submit(() -> {
                largada.await();
                try {
                    compras.reservar(u, e.getId(), 1);
                    vendidas.incrementAndGet();
                } catch (ReglaNegocioException agotado) {
                    // esperado para 15 de los 20
                }
                return null;
            }));
        }
        largada.countDown();
        for (Future<?> t : tareas) {
            t.get(30, TimeUnit.SECONDS);
        }
        hilos.shutdown();

        assertThat(vendidas.get()).isEqualTo(5);
        assertThat(cupoReservado(e)).isEqualTo(5);
    }

    // ---------- ayudantes ----------

    private Orden iniciar(Evento e, int cantidad) {
        Orden o = compras.reservar(fabrica.usuario().getId(), e.getId(), cantidad);
        compras.iniciarPago(o.getCodigo());
        return ordenes.findByCodigo(o.getCodigo()).orElseThrow();
    }

    private Orden pagar(Evento e, int cantidad, ResultadoPago respuesta) {
        Orden o = iniciar(e, cantidad);
        ResultadoPago conOrden = new ResultadoPago(respuesta.estado(), respuesta.codigoRespuesta(), respuesta.monto(),
                o.getCodigo(), o.getIdSesion(), respuesta.codigoAutorizacion(), respuesta.tipoPago(),
                respuesta.cuotas(), respuesta.ultimos4());
        when(pasarela.confirmar(o.getTokenWebpay())).thenReturn(conOrden);
        compras.confirmarPago(o.getTokenWebpay());
        return o;
    }

    private ResultadoPago aprobado(Evento e, int cantidad) {
        return new ResultadoPago("AUTHORIZED", 0, e.getPrecio() * cantidad, null, null, "1213", "VD", 0, "6623");
    }

    private void vencer(Orden o) {
        Orden fresca = ordenes.findById(o.getId()).orElseThrow();
        fresca.setExpiraEn(Instant.now().minusSeconds(60));
        ordenes.save(fresca);
    }

    private int cupoReservado(Evento e) {
        return eventos.findById(e.getId()).orElseThrow().getEntradasReservadas();
    }
}
