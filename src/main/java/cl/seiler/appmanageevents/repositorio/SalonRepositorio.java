package cl.seiler.appmanageevents.repositorio;

import cl.seiler.appmanageevents.dominio.Salon;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SalonRepositorio extends JpaRepository<Salon, Long> {

    List<Salon> findAllByOrderByNombreAsc();
}
