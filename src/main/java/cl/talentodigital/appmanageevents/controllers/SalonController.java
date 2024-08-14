package cl.talentodigital.appmanageevents.controllers;


import cl.talentodigital.appmanageevents.entities.Salon;
import cl.talentodigital.appmanageevents.services.EventoServicio;
import cl.talentodigital.appmanageevents.services.SalonServicio;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;

import java.util.List;
import java.util.Optional;

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
import org.springframework.web.servlet.mvc.support.RedirectAttributes;




@Controller
public class SalonController {

    private static final Logger logger = LoggerFactory.getLogger(SalonController.class);

    @Autowired
    private EventoServicio eventoServicio;

    @Autowired
    private SalonServicio salonServicio;


    @Operation(summary = "Redirige a una nueva pagina para registrar un salon")
    @ApiResponse(responseCode = "200", description = "HTTP STATUS 200")
    @RequestMapping(value = "/nuevoSalon" ,method = RequestMethod.GET)
    public String mostrarFormularioCrearSalon(Model modelo){

        logger.info("=========================Pagina para crear el salon=======================");

        Salon salon = new Salon();
        modelo.addAttribute("salon" , salon);

        logger.info("=========================Salon nuevo =======================");
        logger.info("salon ==>: {}", salon.toString());

        //lista de salones
        List<Salon> salones = salonServicio.salonList();
        modelo.addAttribute("salones", salones);

        return "salones/nuevo_salon";

    }

    @Operation(summary = "Guarda un nuevo salon")
    @ApiResponse(responseCode = "200", description = "HTTP STATUS 200")
    @RequestMapping(value ="guardarSalon" ,method = RequestMethod.POST)
    public String guardarSalon(@ModelAttribute("salon") Salon salon ,RedirectAttributes redirectAttributes ) {
        logger.info("=========================Nuevo salon a guardar =======================");
        logger.info("salon antes de guardar ==>: {}", salon.toString());
    
        Salon savedSalon = salonServicio.save(salon);
    
        logger.info("salon después de guardar ==>: {}", savedSalon.toString());
        logger.info("===============================================");
    
        redirectAttributes.addFlashAttribute("message", "Salón creado satisfactoriamente.");
        redirectAttributes.addFlashAttribute("messageType", "success");

        return "redirect:/nuevoSalon";
    }

    @Operation(summary = "Editar un Salon")
    @ApiResponse(responseCode = "200", description = "HTTP STATUS 200")
    @RequestMapping(value = "/editarSalon/{id}")
    public ModelAndView mostrarFormularioDeEditarSalon(@PathVariable(name = "id") Long id) {
        ModelAndView modelo = new ModelAndView("salones/editar_salon");
        Optional<Salon> optionalSalon = Optional.ofNullable(salonServicio.getById(id));
    
        if (optionalSalon.isPresent()) {
            Salon salon = optionalSalon.get();
            List<Salon> salones = salonServicio.salonList();
    
            modelo.addObject("salon", salon); // Agregar el salón específico a editar
            modelo.addObject("salones", salones); // Agregar lista de salones si es necesario
    
            return modelo;
        } else {
            return new ModelAndView("error");
        }
    }

    @Operation(summary = "Eliminar un salon")
    @ApiResponse(responseCode = "200", description = "HTTP STATUS 200")
    @RequestMapping(value = "/eliminarSalon/{id}")
public String eliminarSalon(@PathVariable(name = "id") Long id, RedirectAttributes redirectAttributes) {
    eventoServicio.delete(id);
    salonServicio.delete(id);

    redirectAttributes.addFlashAttribute("message", "Salón eliminado satisfactoriamente.");
    redirectAttributes.addFlashAttribute("messageType", "success");

    return "redirect:/nuevoSalon";
}





}
