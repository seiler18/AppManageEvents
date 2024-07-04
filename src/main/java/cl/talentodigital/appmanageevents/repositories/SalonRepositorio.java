package cl.talentodigital.appmanageevents.repositories;

import cl.talentodigital.appmanageevents.entities.Salon;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface SalonRepositorio extends JpaRepository<Salon, Long> {

    public List<Salon> findAll();
    public Optional<Salon> findById(Long id);
}
