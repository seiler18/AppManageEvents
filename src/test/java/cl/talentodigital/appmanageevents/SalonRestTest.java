package cl.talentodigital.appmanageevents;

import cl.talentodigital.appmanageevents.controllers.SalonRest;
import cl.talentodigital.appmanageevents.entities.Salon;
import cl.talentodigital.appmanageevents.services.SalonServicio;
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

import java.util.List;

@WebMvcTest(SalonRest.class)
public class SalonRestTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private SalonServicio salonServicio;


    @Test
    @WithMockUser(username = "user", roles = { "USER" })
    public void testListarSalones() throws Exception {
        Salon salon1 = new Salon(1L, "Salón A", null, null, null);
        Salon salon2 = new Salon(2L, "Salón B", null, null, null);
        when(salonServicio.salonList()).thenReturn(List.of(salon1, salon2));

        mockMvc.perform(get("/api/salones"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].nombre").value("Salón A"))
                .andExpect(jsonPath("$[1].id").value(2))
                .andExpect(jsonPath("$[1].nombre").value("Salón B"));
    }

    @Test
    @WithMockUser(username = "user", roles = { "USER" })
    public void testObtenerSalonPorId() throws Exception {
        Salon salon = new Salon(1L, "Salón A", null, null, null);
        when(salonServicio.getById(1L)).thenReturn(salon);

        mockMvc.perform(get("/api/salones/1"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.nombre").value("Salón A"));
    }


    
}
