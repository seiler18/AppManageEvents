package cl.seiler.appmanageevents.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.header.writers.ReferrerPolicyHeaderWriter.ReferrerPolicy;

@Configuration
@EnableWebSecurity
public class SeguridadConfig {

    // Bootstrap llega de jsDelivr; el formulario de pago se envía a Transbank.
    // Ningún script en línea: todo JS vive en /js, así la CSP no necesita
    // 'unsafe-inline' y una inyección de HTML no puede ejecutar código.
    private static final String CSP = String.join("; ",
            "default-src 'self'",
            "script-src 'self' https://cdn.jsdelivr.net",
            "style-src 'self' https://cdn.jsdelivr.net",
            // Solo atributos style (barras de ocupación); ningún <style> en línea.
            "style-src-attr 'unsafe-inline'",
            "font-src 'self' https://cdn.jsdelivr.net",
            "img-src 'self' https: data:",
            "connect-src 'self'",
            "form-action 'self' https://webpay3g.transbank.cl https://webpay3gint.transbank.cl",
            "frame-ancestors 'none'",
            "base-uri 'self'",
            "object-src 'none'");

    @Bean
    public SecurityFilterChain seguridad(HttpSecurity http) throws Exception {
        http
                .authorizeHttpRequests(a -> a
                        .requestMatchers("/", "/eventos", "/eventos/*", "/login", "/registro",
                                "/pago/retorno", "/notificaciones/baja", "/error",
                                "/css/**", "/js/**", "/img/**", "/favicon.svg",
                                "/actuator/health", "/api/eventos/**",
                                "/api/docs", "/api/swagger-ui/**", "/v3/api-docs", "/v3/api-docs/**").permitAll()
                        .requestMatchers("/admin/**").hasRole("ADMIN")
                        .anyRequest().authenticated())
                .formLogin(f -> f
                        .loginPage("/login")
                        .usernameParameter("email")
                        .failureUrl("/login?error")
                        .defaultSuccessUrl("/", false)
                        .permitAll())
                .logout(l -> l
                        .logoutUrl("/logout")
                        .logoutSuccessUrl("/?salir"))
                // Webpay devuelve al comprador con un POST desde su dominio:
                // ese POST no puede traer nuestro token CSRF. El retorno no
                // confía en nada del formulario: confirma el token contra
                // Transbank o exige el session_id secreto de la orden.
                .csrf(c -> c.ignoringRequestMatchers("/pago/retorno"))
                .headers(h -> h
                        .contentSecurityPolicy(csp -> csp.policyDirectives(CSP))
                        .referrerPolicy(r -> r.policy(ReferrerPolicy.STRICT_ORIGIN_WHEN_CROSS_ORIGIN)));
        return http.build();
    }

    /** Delegante con prefijo {bcrypt}: permite subir el algoritmo más adelante sin invalidar claves. */
    @Bean
    public PasswordEncoder codificadorClaves() {
        return PasswordEncoderFactories.createDelegatingPasswordEncoder();
    }
}
