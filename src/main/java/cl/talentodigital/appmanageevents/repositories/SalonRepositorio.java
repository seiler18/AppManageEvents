package cl.talentodigital.appmanageevents.repositories;

import cl.talentodigital.appmanageevents.entities.Evento;
import cl.talentodigital.appmanageevents.entities.Salon;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface SalonRepositorio extends JpaRepository<Salon, Long> {

    public List<Salon> findAll();

    public Optional<Salon> findById(Long id);

    // listar todos los salones por nombre
    @Query("SELECT e FROM Salon e WHERE e.nombre LIKE %?1%")
    List<Salon> findAllByNombre(String evento);

    // listar todos los eventos de un salon
    @Query("SELECT e.eventos FROM Salon e WHERE e.id = ?1")
    List<Evento> findAllEventosSalon(Long id);

}
