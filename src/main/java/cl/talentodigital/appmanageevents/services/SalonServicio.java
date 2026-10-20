package cl.talentodigital.appmanageevents.services;

import cl.talentodigital.appmanageevents.entities.Evento;
import cl.talentodigital.appmanageevents.entities.Salon;
import cl.talentodigital.appmanageevents.repositories.SalonRepositorio;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class SalonServicio {


    @Autowired
    private SalonRepositorio salonRepositorio;

    public List<String> salonListByName(){

        List<Salon> salones = salonRepositorio.findAll();
        return salones.stream().map(Salon::getNombre).collect(Collectors.toList());
    }

    public List<Salon> salonList(){

        return salonRepositorio.findAll();
    }

    public Salon save(Salon salon) {
        return salonRepositorio.save(salon);
    }

    public void delete(Long id){
        salonRepositorio.deleteById(id);
    }

    public Salon getById(Long id) {
        return salonRepositorio.findById(id).get() ;
        
    }

    public List<Salon> findAllByNombre(String nombre){
        return salonRepositorio.findAllByNombre(nombre);
    }

    public List<Evento> findAllEventosSalon(Long id){
        return salonRepositorio.findAllEventosSalon(id);
    }


}
