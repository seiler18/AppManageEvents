package cl.talentodigital.appmanageevents.repositories;

import cl.talentodigital.appmanageevents.entities.Salon;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface SalonRepositorio extends JpaRepository<Salon, Long> {

    @SuppressWarnings("null")
    public List<Salon> findAll();
    @SuppressWarnings("null")
    public Optional<Salon> findById(Long id);

@Query("SELECT e FROM Salon e WHERE e.nombre LIKE %?1%")
    List<Salon> findAllByNombre(String evento);

}
