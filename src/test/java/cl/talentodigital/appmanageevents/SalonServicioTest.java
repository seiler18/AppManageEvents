package cl.talentodigital.appmanageevents;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import cl.talentodigital.appmanageevents.entities.Salon;
import cl.talentodigital.appmanageevents.repositories.SalonRepositorio;
import cl.talentodigital.appmanageevents.services.SalonServicio;

@ExtendWith(MockitoExtension.class)
class SalonServicioTest {

    @Mock // REPOSITORIO
    private SalonRepositorio salonRepositorio;

    @InjectMocks // SERVICIO
    private SalonServicio salonServicio;

    @Test
    // Test para listar todos los nombres de salones
    void testSalonListByName() {
        Salon salon1 = new Salon(1L, "Salon 1", null, null, null);
        Salon salon2 = new Salon(2L, "Salon 2", null, null, null);

        when(salonRepositorio.findAll()).thenReturn(Arrays.asList(salon1, salon2));

        List<String> nombresSalones = salonServicio.salonListByName();

        assertEquals(2, nombresSalones.size());
        assertEquals("Salon 1", nombresSalones.get(0));
        assertEquals("Salon 2", nombresSalones.get(1));
        verify(salonRepositorio, times(1)).findAll();
    }

    @Test
    // Test para listar todos los salones
    void testSalonList() {
        Salon salon1 = new Salon(1L, "Salon 1", null, null, null);
        Salon salon2 = new Salon(2L, "Salon 2", null, null, null);

        when(salonRepositorio.findAll()).thenReturn(Arrays.asList(salon1, salon2));

        List<Salon> salones = salonServicio.salonList();

        assertEquals(2, salones.size());
        assertEquals("Salon 1", salones.get(0).getNombre());
        assertEquals("Salon 2", salones.get(1).getNombre());
        verify(salonRepositorio, times(1)).findAll();
    }

    @Test
    // Test para guardar un salón
    void testSave() {
        Salon salon = new Salon(1L, "Salon Test", null, null, null);

        when(salonRepositorio.save(salon)).thenReturn(salon);

        Salon salonGuardado = salonServicio.save(salon);

        assertEquals(salon, salonGuardado);
        verify(salonRepositorio, times(1)).save(salon);
    }

    @Test
    // Test para eliminar un salón
    void testDelete() {
        Long id = 1L;

        salonServicio.delete(id);

        verify(salonRepositorio, times(1)).deleteById(id);
    }

    @Test
    // Test para obtener un salón por id
    void testGetById() {
        Long id = 1L;
        Salon salon = new Salon(id, "Salon Test", null, null, null);

        when(salonRepositorio.findById(id)).thenReturn(Optional.of(salon));

        Salon salonObtenido = salonServicio.getById(id);

        assertEquals(id, salonObtenido.getId());
        assertEquals("Salon Test", salonObtenido.getNombre());
        verify(salonRepositorio, times(1)).findById(id);
    }
}
