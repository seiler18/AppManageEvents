package cl.talentodigital.appmanageevents;

import cl.talentodigital.appmanageevents.controllers.EventoRest;
import cl.talentodigital.appmanageevents.entities.Evento;
import cl.talentodigital.appmanageevents.services.EventoServicio;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.time.LocalDate;
import java.util.List;

@WebMvcTest(EventoRest.class)
public class EventoRestTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private EventoServicio eventoServicio;

    @Test
    @WithMockUser(username = "user", roles = { "USER" })
    public void testListarEventos() throws Exception {
        Evento evento1 = new Evento(1L, "Evento A", LocalDate.now(), null, null);
        Evento evento2 = new Evento(2L, "Evento B", LocalDate.now().plusDays(1), null, null);
        when(eventoServicio.listAll()).thenReturn(List.of(evento1, evento2));

        mockMvc.perform(get("/api/eventos"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].nombre").value("Evento A"))
                .andExpect(jsonPath("$[1].id").value(2))
                .andExpect(jsonPath("$[1].nombre").value("Evento B"));
    }

    @Test
    @WithMockUser(username = "user", roles = { "USER" })
    public void testObtenerEventoPorId() throws Exception {
        Evento evento = new Evento(1L, "Evento A", LocalDate.now(), null, null);
        when(eventoServicio.getById(1L)).thenReturn(evento);

        mockMvc.perform(get("/api/eventos/1"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.nombre").value("Evento A"));
    }
}
