package cl.talentodigital.appmanageevents.repositories;

import cl.talentodigital.appmanageevents.entities.Evento;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface EventoRepositorio extends JpaRepository <Evento, Long>{

    @SuppressWarnings("null")
    public List<Evento> findAll();
    @SuppressWarnings("null")
    public Optional<Evento> findById(Long id);

    @Query("SELECT e FROM Evento e WHERE e.nombre LIKE %?1%")
    List<Evento> findAllByNombre(String palabraClave);

    //Si quisiera hacer mas filters puedo colocar otros
}