package cl.talentodigital.appmanageevents;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import cl.talentodigital.appmanageevents.entities.Evento;
import cl.talentodigital.appmanageevents.entities.Salon;
import cl.talentodigital.appmanageevents.entities.User;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class EntitiesTest {

    @Test
    public void testEvento() {
        // Crear una instancia de Evento
        Salon salon = new Salon(1L, "Salon Principal", 100, "Un gran salón");
        Evento evento = new Evento(1L, "Conferencia de Java", LocalDate.of(2024, 8, 25), 50, salon);

        // Validar getters
        assertEquals(1L, evento.getId());
        assertEquals("Conferencia de Java", evento.getNombre());
        assertEquals(LocalDate.of(2024, 8, 25), evento.getFecha());
        assertEquals(50, evento.getInvitados());
        assertEquals(salon, evento.getSalon());

        // Validar setters
        evento.setNombre("Conferencia de Spring Boot");
        assertEquals("Conferencia de Spring Boot", evento.getNombre());

        // Validar toString
        String expectedToString = "Evento{id=1, nombre='Conferencia de Spring Boot', fecha=2024-08-25, invitados=50, salon=Salon{id=1, nombre='Salon Principal', capacidad=100, descripcion='Un gran salón'}}";
        assertEquals(expectedToString, evento.toString());
    }

    @Test
    public void testSalon() {
        // Crear una instancia de Salon
        Salon salon = new Salon(1L, "Salon Principal", 100, "Un gran salón");

        // Validar getters
        assertEquals(1L, salon.getId());
        assertEquals("Salon Principal", salon.getNombre());
        assertEquals(100, salon.getCapacidad());
        assertEquals("Un gran salón", salon.getDescripcion());

        // Validar setters
        salon.setCapacidad(150);
        assertEquals(150, salon.getCapacidad());

        // Validar toString
        String expectedToString = "Salon{id=1, nombre='Salon Principal', capacidad=150, descripcion='Un gran salón'}";
        assertEquals(expectedToString, salon.toString());
    }

    @Test
    public void testUser() {
        // Crear una instancia de User
        List<String> roles = new ArrayList<>(Collections.singletonList("USER"));
        User user = new User(1L, "jsmith", "password123", roles);

        // Validar getters
        assertEquals(1L, user.getId());
        assertEquals("jsmith", user.getUsername());
        assertEquals("password123", user.getPassword());
        assertEquals(roles, user.getRoles());

        // Validar setters
        user.setUsername("jdoe");
        assertEquals("jdoe", user.getUsername());

        // Validar getAuthorities
        assertEquals(1, user.getAuthorities().size());
        assertEquals("ROLE_USER", user.getAuthorities().get(0).getAuthority());

        // Validar toString
        String expectedToString = "User{id=1, username='jdoe', password='password123', roles=[USER]}";
        assertEquals(expectedToString, user.toString());
    }
}

