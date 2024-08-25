package cl.talentodigital.appmanageevents;

import cl.talentodigital.appmanageevents.entities.User;
import cl.talentodigital.appmanageevents.repositories.UserRepository;
import cl.talentodigital.appmanageevents.services.UserDetailsService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.Arrays;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class UserDetailsServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserDetailsService userDetailsService;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void testLoadUserByUsername_UserFound() {
        // Arrange
        User user = new User(1L, "jsmith", "password123", Arrays.asList("USER", "ADMIN"));
        when(userRepository.findByUsername("jsmith")).thenReturn(Optional.of(user));

        // Act
        UserDetails userDetails = userDetailsService.loadUserByUsername("jsmith");

        // Assert
        assertEquals("jsmith", userDetails.getUsername());
        assertEquals("password123", userDetails.getPassword());
        assertEquals(2, userDetails.getAuthorities().size()); // Verifica que hay 2 roles
        verify(userRepository, times(1)).findByUsername("jsmith");
    }

    @Test
    public void testLoadUserByUsername_UserNotFound() {
        // Arrange
        when(userRepository.findByUsername("jsmith")).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(UsernameNotFoundException.class, () -> userDetailsService.loadUserByUsername("jsmith"));
        verify(userRepository, times(1)).findByUsername("jsmith");
    }
}
