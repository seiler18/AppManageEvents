package cl.talentodigital.appmanageevents.controllers;

import cl.talentodigital.appmanageevents.entities.Salon;
import cl.talentodigital.appmanageevents.services.SalonServicio;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/salones")
public class SalonRest {

    @Autowired
    private SalonServicio salonServicio;

    @GetMapping
    public List<Salon> listarSalones() {
        return salonServicio.salonList();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Salon> obtenerSalonPorId(@PathVariable Long id) {
        Salon salon = salonServicio.getById(id);
        return ResponseEntity.ok(salon);
    }

    @PostMapping
    public ResponseEntity<Salon> crearSalon(@RequestBody Salon salon) {
        salonServicio.save(salon);
        return ResponseEntity.ok(salon);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Salon> actualizarSalon(@PathVariable Long id, @RequestBody Salon salonActualizado) {
        Salon salon = salonServicio.getById(id);
        salon.setNombre(salonActualizado.getNombre());
        // actualizar otros campos según sea necesario
        salonServicio.save(salon);
        return ResponseEntity.ok(salon);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminarSalon(@PathVariable Long id) {
        salonServicio.delete(id);
        return ResponseEntity.noContent().build();
    }
}
