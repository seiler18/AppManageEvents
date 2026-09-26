package cl.talentodigital.appmanageevents.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // CSRF activo para las vistas web: sin el, cualquier pagina ajena
                // podia enviar un formulario a esta app con la sesion del
                // visitante. Thymeleaf inyecta el token solo en los formularios
                // con th:action, asi que las vistas no necesitan nada mas.
                // /api/** queda fuera: register.js le manda JSON sin token. Las
                // escrituras de la API exigen sesion, pero siguen a salvo de
                // CSRF porque solo aceptan application/json (o DELETE/PUT), y
                // eso obliga al navegador a una preflight CORS que esta app no
                // autoriza para ningun origen ajeno. Si algun dia se anade
                // CORS abierto o un endpoint que acepte formularios en
                // /api/**, esto deja de ser cierto.
                .csrf(csrf -> csrf.ignoringRequestMatchers("/api/**"))
                .authorizeHttpRequests(authorize -> authorize
                        .requestMatchers("/", "/home", "/login", "/register", "/api/auth/**").permitAll()
                        // Solo la LECTURA de la API es publica. Antes el permitAll
                        // cubria todos los metodos y, con CSRF desactivado en
                        // /api/**, cualquiera sin cuenta podia crear, modificar o
                        // borrar eventos y salones del sitio publicado con un
                        // simple curl. POST/PUT/DELETE caen en anyRequest() y
                        // exigen sesion, igual que las vistas web.
                        .requestMatchers(HttpMethod.GET, "/api/eventos/**", "/api/salones/**").permitAll()
                        .requestMatchers("/css/**", "/js/**", "/img/**").permitAll() // Permite acceso a recursos estáticos
                        .requestMatchers("/actuator/prometheus", "/actuator/metrics").permitAll() // Permitir acceso a métricas de Actuator
                        .anyRequest().authenticated())
                .formLogin(form -> form
                        .loginPage("/login")
                        .failureUrl("/login?error=true")
                        .defaultSuccessUrl("/eventos", true)
                        .permitAll())
                .logout(logout -> logout
                        .logoutUrl("/logout")
                        .logoutSuccessUrl("/login?logout=true")
                        .permitAll());

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration)
            throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }

    @Bean
    // Crea el Cliente de registro para google, github y facebook
    public ClientRegistrationRepository clientRegistrationRepository() {
        return new ClientRegistrationRepository() {
            @Override
            public ClientRegistration findByRegistrationId(String registrationId) {
                return null;
            }
        };
    }
}
