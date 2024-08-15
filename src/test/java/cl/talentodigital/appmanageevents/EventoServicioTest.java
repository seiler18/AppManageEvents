package cl.talentodigital.appmanageevents;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import cl.talentodigital.appmanageevents.entities.Evento;
import cl.talentodigital.appmanageevents.repositories.EventoRepositorio;
import cl.talentodigital.appmanageevents.services.EventoServicio;

@ExtendWith(MockitoExtension.class)
class EventoServicioTest {

    @Mock // REPOSITORIO
    private EventoRepositorio eventoRepositorio;

    @InjectMocks // SERVICIO
    private EventoServicio eventoServicio;

    @Test
    // Test para listar todos los eventos con keyword
    void testListAllWithKeyword() {

        String keyword = "Test";
        Evento evento1 = new Evento(1L, "Test Event 1", LocalDate.now(), null, null);
        Evento evento2 = new Evento(2L, "Test Event 2", LocalDate.now(), null, null);

        when(eventoRepositorio.findAllByNombre(keyword)).thenReturn(Arrays.asList(evento1, evento2));

        List<Evento> eventos = eventoServicio.listAll(keyword);

        assertEquals(2, eventos.size());
        verify(eventoRepositorio, times(1)).findAllByNombre(keyword);
    }

    @Test
    // Test para listar todos los eventos sin keyword
    void testListAllWithoutKeyword() {
        Evento evento1 = new Evento(1L, "Event 1", LocalDate.now(), null, null);
        Evento evento2 = new Evento(2L, "Event 2", LocalDate.now(), null, null);

        when(eventoRepositorio.findAll()).thenReturn(Arrays.asList(evento1, evento2));

        List<Evento> eventos = eventoServicio.listAll(null);

        assertEquals(2, eventos.size());
        verify(eventoRepositorio, times(1)).findAll();
    }

    @Test
    // Test para obtener un evento por id
    void testGetById() {
        Long id = 1L;
        Evento evento = new Evento(id, "Test Event", LocalDate.now(), null, null);

        when(eventoRepositorio.findById(id)).thenReturn(java.util.Optional.of(evento));

        Evento eventoObtenido = eventoServicio.getById(id);

        assertEquals(id, eventoObtenido.getId());
        verify(eventoRepositorio, times(1)).findById(id);
    }

    @Test
    // Test para guardar un evento
    void testSave() {
        Evento evento = new Evento(1L, "Test Event", LocalDate.now(), null, null);

        eventoServicio.save(evento);

        verify(eventoRepositorio, times(1)).save(evento);
    }

    @Test
    // Test para eliminar un evento
    void testDelete() {
        Long id = 1L;

        eventoServicio.delete(id);

        verify(eventoRepositorio, times(1)).deleteById(id);
    }

}
