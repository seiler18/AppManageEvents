package cl.seiler.appmanageevents.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Clock;
import java.time.ZoneId;

@Configuration
public class RelojConfig {

    /**
     * Todo lo que mira la hora pasa por este reloj: las fechas de los eventos
     * son hora de Chile aunque el servidor corra en UTC, y las pruebas pueden
     * fijarlo para simular que vence una reserva.
     */
    @Bean
    public Clock reloj(AppProperties app) {
        return Clock.system(ZoneId.of(app.zonaHoraria()));
    }
}
