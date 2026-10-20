package cl.seiler.appmanageevents;

import cl.seiler.appmanageevents.dominio.*;
import cl.seiler.appmanageevents.pago.PasarelaPago;
import cl.seiler.appmanageevents.pago.PasarelaPago.ResultadoPago;
import cl.seiler.appmanageevents.pago.PasarelaPago.TransaccionIniciada;
import cl.seiler.appmanageevents.repositorio.OrdenRepositorio;
import cl.seiler.appmanageevents.repositorio.EventoRepositorio;
import cl.seiler.appmanageevents.repositorio.UsuarioRepositorio;
import cl.seiler.appmanageevents.servicio.CompraServicio;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.RequestPostProcessor;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class WebTest {

    @Autowired MockMvc mvc;
    @Autowired Fabrica fabrica;
    @Autowired CompraServicio compras;
    @Autowired OrdenRepositorio ordenes;
    @Autowired UsuarioRepositorio usuarios;
    @Autowired EventoRepositorio eventos;

    @MockitoBean PasarelaPago pasarela;

    @Test
    void laCarteleraEsPublicaYMuestraLosEventos() throws Exception {
        Evento e = fabrica.evento(15_000, 100);
        mvc.perform(get("/"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString(e.getTitulo())))
                .andExpect(content().string(containsString("$15.000")))
                .andExpect(header().string("Content-Security-Policy", containsString("frame-ancestors 'none'")));
        mvc.perform(get("/eventos/" + e.getId()))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Ingresa para comprar")));
    }

    @Test
    void unBorradorNoExisteParaElPublico() throws Exception {
        Evento e = fabrica.evento(1_000, 10);
        e.setEstado(EstadoEvento.BORRADOR);
        eventos.save(e);
        mvc.perform(get("/eventos/" + e.getId())).andExpect(status().isNotFound());
        mvc.perform(get("/api/eventos/" + e.getId())).andExpect(status().isNotFound());
        mvc.perform(get("/eventos/999999")).andExpect(status().isNotFound());
    }

    @Test
    void elPanelExigeRolAdmin() throws Exception {
        mvc.perform(get("/admin")).andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login"));
        mvc.perform(get("/admin").with(como(fabrica.usuario()))).andExpect(status().isForbidden());
        mvc.perform(get("/admin").with(como(fabrica.usuario(Rol.ADMIN)))).andExpect(status().isOk());
    }

    @Test
    void registroCreaLaCuentaConLaClaveCifrada() throws Exception {
        mvc.perform(post("/registro").with(csrf())
                        .param("nombre", "Ana Pérez")
                        .param("email", "Ana.Perez@Correo.cl")
                        .param("password", "secreta123")
                        .param("confirmacion", "secreta123")
                        .param("recibeNotificaciones", "true"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login"));
        Usuario u = usuarios.findByEmailIgnoreCase("ana.perez@correo.cl").orElseThrow();
        assertThat(u.getEmail()).isEqualTo("ana.perez@correo.cl");
        assertThat(u.getPasswordHash()).startsWith("{bcrypt}").doesNotContain("secreta123");
        assertThat(u.isRecibeNotificaciones()).isTrue();
    }

    @Test
    void registroRechazaClavesDistintas() throws Exception {
        mvc.perform(post("/registro").with(csrf())
                        .param("nombre", "Ana").param("email", "ana2@correo.cl")
                        .param("password", "secreta123").param("confirmacion", "otra12345"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("no coinciden")));
    }

    @Test
    void comprarSinTokenCsrfSeRechaza() throws Exception {
        Evento e = fabrica.evento(1_000, 10);
        mvc.perform(post("/eventos/" + e.getId() + "/comprar").param("cantidad", "1").with(como(fabrica.usuario())))
                .andExpect(status().isForbidden());
    }

    @Test
    void comprarLlevaAWebpayYElRetornoConfirma() throws Exception {
        Evento e = fabrica.evento(4_500, 10);
        Usuario comprador = fabrica.usuario();
        when(pasarela.crear(anyString(), anyString(), anyInt(), eq("http://localhost/pago/retorno")))
                .thenAnswer(inv -> new TransaccionIniciada("tok-" + inv.getArgument(0), "https://webpay3gint.transbank.cl/webpayserver/initTransaction"));

        mvc.perform(post("/eventos/" + e.getId() + "/comprar").param("cantidad", "2")
                        .with(como(comprador)).with(csrf()))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("id=\"form-webpay\"")))
                .andExpect(content().string(containsString("webpay3gint.transbank.cl")));

        Orden o = ordenes.findByUsuarioIdAndEstadoOrderByCreadaEnDesc(comprador.getId(), EstadoOrden.PENDIENTE).getFirst();
        when(pasarela.confirmar(o.getTokenWebpay())).thenReturn(new ResultadoPago("AUTHORIZED", 0, 9_000,
                o.getCodigo(), o.getIdSesion(), "1213", "VN", 0, "6623"));

        // Transbank vuelve con un POST desde su dominio: sin sesión y sin CSRF.
        mvc.perform(post("/pago/retorno").param("token_ws", o.getTokenWebpay()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/compras/" + o.getCodigo()));

        mvc.perform(get("/compras/" + o.getCodigo()).with(como(comprador)))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Tus entradas están confirmadas")))
                .andExpect(content().string(containsString("terminada en 6623")));
    }

    @Test
    void unaOrdenAjenaRespondeComoInexistente() throws Exception {
        Orden o = compras.reservar(fabrica.usuario().getId(), fabrica.evento(0, 5).getId(), 1);
        mvc.perform(get("/compras/" + o.getCodigo()).with(como(fabrica.usuario())))
                .andExpect(status().isNotFound());
        mvc.perform(get("/compras/" + o.getCodigo()).with(como(fabrica.usuario(Rol.ADMIN))))
                .andExpect(status().isOk());
    }

    @Test
    void lasPaginasDeCuentaRenderizan() throws Exception {
        Usuario u = fabrica.usuario();
        Orden o = compras.reservar(u.getId(), fabrica.evento(0, 5).getId(), 2);
        String codigo = ordenes.findByCodigo(o.getCodigo()).orElseThrow().getEntradas().getFirst().getCodigo();

        mvc.perform(get("/cuenta/entradas").with(como(u))).andExpect(status().isOk())
                .andExpect(content().string(containsString("Vigente")));
        mvc.perform(get("/cuenta/entradas/" + codigo).with(como(u))).andExpect(status().isOk())
                .andExpect(content().string(containsString(codigo)));
        mvc.perform(get("/cuenta/entradas/" + codigo + "/qr.png").with(como(u)))
                .andExpect(status().isOk()).andExpect(content().contentType(MediaType.IMAGE_PNG));
        mvc.perform(get("/cuenta/entradas/" + codigo).with(como(fabrica.usuario()))).andExpect(status().isNotFound());
        mvc.perform(get("/cuenta/perfil").with(como(u))).andExpect(status().isOk());
    }

    @Test
    void elPanelCompletoRenderiza() throws Exception {
        Usuario admin = fabrica.usuario(Rol.ADMIN);
        Evento e = fabrica.evento(0, 8);
        Orden o = compras.reservar(fabrica.usuario().getId(), e.getId(), 1);
        String entrada = ordenes.findByCodigo(o.getCodigo()).orElseThrow().getEntradas().getFirst().getCodigo();

        for (String ruta : new String[]{"/admin", "/admin/eventos", "/admin/eventos/nuevo",
                "/admin/eventos/" + e.getId(), "/admin/eventos/" + e.getId() + "/editar",
                "/admin/salones", "/admin/salones/nuevo", "/admin/ordenes", "/admin/ordenes?estado=PAGADA",
                "/admin/validar", "/admin/validar?codigo=" + entrada, "/admin/usuarios"}) {
            mvc.perform(get(ruta).with(como(admin))).andExpect(status().isOk());
        }
        mvc.perform(get("/admin/eventos/" + e.getId() + "/asistentes.csv").with(como(admin)))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString(entrada)));
    }

    @Test
    void elAdminCreaYPublicaUnEvento() throws Exception {
        Usuario admin = fabrica.usuario(Rol.ADMIN);
        mvc.perform(post("/admin/eventos").with(como(admin)).with(csrf())
                        .param("titulo", "Festival de prueba")
                        .param("fechaInicio", "2099-03-01T20:00")
                        .param("precio", "25000")
                        .param("cupoMaximo", "120")
                        .param("maxPorCompra", "6"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("/admin/eventos/*"));

        mvc.perform(post("/admin/eventos").with(como(admin)).with(csrf())
                        .param("titulo", "").param("precio", "-1").param("cupoMaximo", "0").param("maxPorCompra", "1"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Ponle un título")));
    }

    @Test
    void validarUnaEntradaSoloFuncionaUnaVez() throws Exception {
        Usuario admin = fabrica.usuario(Rol.ADMIN);
        Orden o = compras.reservar(fabrica.usuario().getId(), fabrica.evento(0, 5).getId(), 1);
        String entrada = ordenes.findByCodigo(o.getCodigo()).orElseThrow().getEntradas().getFirst().getCodigo();

        mvc.perform(post("/admin/validar").param("codigo", entrada).with(como(admin)).with(csrf()))
                .andExpect(content().string(containsString("Ingreso registrado")));
        mvc.perform(post("/admin/validar").param("codigo", entrada).with(como(admin)).with(csrf()))
                .andExpect(content().string(containsString("Ya fue usada")));
    }

    @Test
    void laApiPublicaEntregaJson() throws Exception {
        Evento e = fabrica.evento(3_000, 30);
        mvc.perform(get("/api/eventos"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(content().string(containsString(e.getTitulo())));
        mvc.perform(get("/api/eventos/" + e.getId()))
                .andExpect(jsonPath("$.precio").value(3000))
                .andExpect(jsonPath("$.disponibles").value(30));
    }

    @Test
    void laBajaDeAvisosPideConfirmacion() throws Exception {
        Usuario u = fabrica.usuario();
        mvc.perform(get("/notificaciones/baja").param("t", u.getTokenBaja())).andExpect(status().isOk());
        assertThat(usuarios.findById(u.getId()).orElseThrow().isRecibeNotificaciones()).isTrue();
        mvc.perform(post("/notificaciones/baja").param("t", u.getTokenBaja()).with(csrf()))
                .andExpect(content().string(containsString("no te enviaremos más avisos")));
        assertThat(usuarios.findById(u.getId()).orElseThrow().isRecibeNotificaciones()).isFalse();
    }

    private static RequestPostProcessor como(Usuario u) {
        return SecurityMockMvcRequestPostProcessors.user(u.getEmail()).roles(u.getRol().name());
    }
}
