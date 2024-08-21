package cl.talentodigital.appmanageevents.controllers;


import cl.talentodigital.appmanageevents.entities.Evento;
import cl.talentodigital.appmanageevents.services.EventoServicio;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/eventos")
public class EventoRest {

    @Autowired
    private EventoServicio eventoServicio;

    // Listar todos los eventos 
    @GetMapping
    public List<Evento> listarEventos() {
        return eventoServicio.listAll();
    }
   
    // Obtener un evento por ID
    @GetMapping("/{id}")
    public ResponseEntity<Evento> obtenerEventoPorId(@PathVariable Long id) {
        Evento evento = eventoServicio.getById(id);
        return ResponseEntity.ok(evento);
    }

    // Crear un nuevo evento
    @PostMapping
    public ResponseEntity<Evento> crearEvento(@RequestBody Evento evento) {
        eventoServicio.save(evento);
        return ResponseEntity.ok(evento);
    }

    // Actualizar un evento existente
    @PutMapping("/{id}")
    public ResponseEntity<Evento> actualizarEvento(@PathVariable Long id, @RequestBody Evento eventoActualizado) {
        Evento evento = eventoServicio.getById(id);
        evento.setNombre(eventoActualizado.getNombre());
        evento.setFecha(eventoActualizado.getFecha());
        evento.setInvitados(eventoActualizado.getInvitados());
        evento.setSalon(eventoActualizado.getSalon());
        eventoServicio.save(evento);
        return ResponseEntity.ok(evento);
    }

    // Eliminar un evento por ID
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminarEvento(@PathVariable Long id) {
        eventoServicio.delete(id);
        return ResponseEntity.noContent().build();
    }
}

