package cl.talentodigital.appmanageevents.controllers;


import cl.talentodigital.appmanageevents.entities.Salon;
import cl.talentodigital.appmanageevents.services.EventoServicio;
import cl.talentodigital.appmanageevents.services.SalonServicio;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;




@Controller
public class SalonController {

    private static final Logger logger = LoggerFactory.getLogger(EventoControlador.class);

    @Autowired
    private EventoServicio eventoServicio;

    @Autowired
    private SalonServicio salonServicio;


    @Operation(summary = "Redirige a una nueva pagina para registrar un evento")
    @ApiResponse(responseCode = "200", description = "HTTP STATUS 200")
    @RequestMapping(value = "/nuevoSalon" ,method = RequestMethod.GET)
    public String crearSalon(Model modelo){

        logger.info("=========================Pagina para crear el salon=======================");

        Salon salon = new Salon();
        modelo.addAttribute("salon" , salon);

        logger.info("=========================Salon nuevo =======================");
        logger.info("salon ==>: {}", salon.toString());

        return "salones/nuevo_salon";

    }

    @Operation(summary = "Guarda un nuevo salon")
    @ApiResponse(responseCode = "200", description = "HTTP STATUS 200")
    @RequestMapping(value ="guardarSalon" ,method = RequestMethod.POST)
    public String guardarSalon(@ModelAttribute("salon") Salon salon){

        logger.info("=========================Nuevo salon a guardar =======================");
        logger.info("salon ==>: {}", salon.toString());
        logger.info("===============================================");

        salonServicio.save(salon);
        return "redirect:/" ;
    }

        @Operation(summary = "Editar un Salon")
    @ApiResponse(responseCode = "200", description = "HTTP STATUS 200")
    @RequestMapping(value = "/editarSalon/{id}")
    public ModelAndView mostrarFormularioDeEditarSalon(@PathVariable(name = "id") Long id){

        ModelAndView modelo = new ModelAndView("salones/editar_salon");
        Salon salon = salonServicio.get(id);
        List<Salon> salones = salonServicio.salonList();

      
        modelo.addObject("salones", salones);

        logger.info("=========================salon editado =======================");
        logger.info("salon ==>  : {}", salon.toString());
        logger.info("================================================");


        return modelo ;
    }





}
