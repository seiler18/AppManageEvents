package cl.talentodigital.appmanageevents.repositories;

import cl.talentodigital.appmanageevents.entities.Evento;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface EventoRepositorio extends JpaRepository <Evento, Long>{
//    @Query("SELECT p from Evento p WHERE" +
//            " CONCAT(p.id , p.nombre , p.fecha)"
//            + "LIKE %?1%")
//
//    public List<Evento> findAll(String palabraClave);
    @Query("SELECT e FROM Evento e WHERE e.nombre LIKE %?1%")
    List<Evento> findAllByNombre(String palabraClave);

    //Si quisiera hacer mas filters puedo colocar otros
}