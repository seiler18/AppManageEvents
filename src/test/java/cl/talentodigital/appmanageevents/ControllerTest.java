package cl.talentodigital.appmanageevents;

import cl.talentodigital.appmanageevents.controllers.AuthController;
import cl.talentodigital.appmanageevents.controllers.EventoControlador;
import cl.talentodigital.appmanageevents.controllers.SalonController;
import cl.talentodigital.appmanageevents.entities.Evento;
import cl.talentodigital.appmanageevents.entities.Salon;
import cl.talentodigital.appmanageevents.entities.User;
import cl.talentodigital.appmanageevents.repositories.UserRepository;
import cl.talentodigital.appmanageevents.services.EventoServicio;
import cl.talentodigital.appmanageevents.services.SalonServicio;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.ui.Model;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

public class ControllerTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private EventoServicio eventoServicio;

    @Mock
    private SalonServicio salonServicio;

    @Mock
    private Model modelo;

    @Mock
    private RedirectAttributes redirectAttributes;

    @InjectMocks
    private AuthController authController;

    @InjectMocks
    private EventoControlador eventoControlador;

    @InjectMocks
    private SalonController salonController;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void testRegisterUser_UserAlreadyExists() {
        // Arrange
        User existingUser = new User();
        existingUser.setUsername("existingUser");

        when(userRepository.findByUsername("existingUser")).thenReturn(Optional.of(existingUser));

        User signUpUser = new User();
        signUpUser.setUsername("existingUser");

        // Act
        ResponseEntity<?> response = authController.registerUser(signUpUser);

        // Assert
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("Error: Username is already taken!", response.getBody());
        verify(userRepository, times(1)).findByUsername("existingUser");
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    public void testRegisterUser_NewUser() {
        // Arrange
        User signUpUser = new User();
        signUpUser.setUsername("newUser");
        signUpUser.setPassword("password123");

        when(userRepository.findByUsername("newUser")).thenReturn(Optional.empty());
        when(passwordEncoder.encode("password123")).thenReturn("encodedPassword");

        // Act
        ResponseEntity<?> response = authController.registerUser(signUpUser);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("User registered successfully!", response.getBody());
        verify(userRepository, times(1)).findByUsername("newUser");
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    @WithMockUser(username = "user", roles = { "USER" })
    public void testVerPaginaDeInicio() {
        // Arrange
        String palabraClave = "Conferencia";
        List<Evento> eventos = Arrays.asList(new Evento());
        when(eventoServicio.findAllByNombre(palabraClave)).thenReturn(eventos);

        // Act
        String viewName = eventoControlador.verPaginaDeInicio(modelo, palabraClave);

        // Assert
        assertEquals("index", viewName);
        verify(modelo).addAttribute("palabraClave", palabraClave);
        verify(modelo).addAttribute("listarEventos", eventos);
    }

    @Test
    @WithMockUser(username = "user", roles = { "USER" })
    public void testMostrarFormularioDeRegistrarProducto() {
        // Arrange
        List<Salon> salones = Arrays.asList(new Salon());
        when(salonServicio.salonList()).thenReturn(salones);

        // Act
        String viewName = eventoControlador.mostrarFormularioDeRegistrarProducto(modelo);

        // Assert
        assertEquals("eventos/nuevo_evento", viewName);
        verify(modelo).addAttribute(eq("evento"), any(Evento.class));
        verify(modelo).addAttribute("salones", salones);
    }

    @Test
    @WithMockUser(username = "user", roles = { "USER" })
    public void testGuardarEvento() {
        // Arrange
        Evento evento = new Evento();

        // Act
        String viewName = eventoControlador.guardarEvento(evento);

        // Assert
        assertEquals("redirect:/eventos", viewName);
        verify(eventoServicio).save(evento);
    }

    @Test
    @WithMockUser(username = "user", roles = { "USER" })
    public void testMostrarFormularioDeEditarEvento() {
        // Arrange
        Long id = 1L;
        Evento evento = new Evento();
        List<Salon> salones = Arrays.asList(new Salon());
        when(eventoServicio.getById(id)).thenReturn(evento);
        when(salonServicio.salonList()).thenReturn(salones);

        // Act
        ModelAndView modelAndView = eventoControlador.mostrarFormularioDeEditarEvento(id);

        // Assert
        assertEquals("eventos/editar_evento", modelAndView.getViewName());
        assertEquals(evento, modelAndView.getModel().get("evento"));
        assertEquals(salones, modelAndView.getModel().get("salones"));
    }

    @Test
    @WithMockUser(username = "user", roles = { "USER" })
    public void testEliminarProducto() {
        // Arrange
        Long id = 1L;

        // Act
        String viewName = eventoControlador.EliminarProducto(id);

        // Assert
        assertEquals("redirect:/eventos", viewName);
        verify(eventoServicio).delete(id);
    }

    @Test
    @WithMockUser(username = "user", roles = {"USER"})
    public void testMostrarFormularioCrearSalon() {
        // Arrange
        List<Salon> salones = Arrays.asList(new Salon());
        when(salonServicio.salonList()).thenReturn(salones);

        // Act
        String viewName = salonController.mostrarFormularioCrearSalon(modelo);

        // Assert
        assertEquals("salones/nuevo_salon", viewName);
        verify(modelo).addAttribute(eq("salon"), any(Salon.class));
        verify(modelo).addAttribute("salones", salones);
    }

    @Test
    @WithMockUser(username = "user", roles = {"USER"})
    public void testGuardarSalon() {
        // Arrange
        Salon salon = new Salon();
        when(salonServicio.save(salon)).thenReturn(salon);

        // Act
        String viewName = salonController.guardarSalon(salon, redirectAttributes);

        // Assert
        assertEquals("redirect:/nuevoSalon", viewName);
        verify(salonServicio).save(salon);
        verify(redirectAttributes).addFlashAttribute("message", "Salón creado satisfactoriamente.");
        verify(redirectAttributes).addFlashAttribute("messageType", "success");
    }

    @Test
    @WithMockUser(username = "user", roles = {"USER"})
    public void testMostrarFormularioDeEditarSalon() {
        // Arrange
        Long id = 1L;
        Salon salon = new Salon();
        List<Salon> salones = Arrays.asList(new Salon());
        when(salonServicio.getById(id)).thenReturn(salon);
        when(salonServicio.salonList()).thenReturn(salones);

        // Act
        ModelAndView modelAndView = salonController.mostrarFormularioDeEditarSalon(id);

        // Assert
        assertEquals("salones/editar_salon", modelAndView.getViewName());
        assertEquals(salon, modelAndView.getModel().get("salon"));
        assertEquals(salones, modelAndView.getModel().get("salones"));
    }

    @Test
    @WithMockUser(username = "user", roles = {"USER"})
    public void testEliminarSalon() {
        // Arrange
        Long id = 1L;

        // Act
        String viewName = salonController.eliminarSalon(id, redirectAttributes);

        // Assert
        assertEquals("redirect:/nuevoSalon", viewName);
        verify(eventoServicio).delete(id);
        verify(salonServicio).delete(id);
        verify(redirectAttributes).addFlashAttribute("message", "Salón eliminado satisfactoriamente.");
        verify(redirectAttributes).addFlashAttribute("messageType", "success");
    }


}
