package cl.talentodigital.appmanageevents.services;


import cl.talentodigital.appmanageevents.entities.Evento;
import cl.talentodigital.appmanageevents.repositories.EventoRepositorio;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class EventoServicio {

    @Autowired
    private EventoRepositorio eventoRepositorio;

    //Metodo para listar todos los eventos con filtro
    public List<Evento> listAll(String palabraClave) {
        if (palabraClave != null) {
            return eventoRepositorio.findAllByNombre(palabraClave);
        }
        return eventoRepositorio.findAll();
    }

    //Metodo para listar todos los eventos
    public List<Evento> listAll() {
        return eventoRepositorio.findAll();
    }

    //Metodo para guardar un evento
    public void save(Evento evento){
        eventoRepositorio.save(evento);
    }

    //Metodo para obtener un evento por id
    public Evento getById(Long id){
        return eventoRepositorio.findById(id).get() ;
    }

    //Metodo para eliminar un evento por id
    public void delete(Long id){

        eventoRepositorio.deleteById(id);
    }


}
