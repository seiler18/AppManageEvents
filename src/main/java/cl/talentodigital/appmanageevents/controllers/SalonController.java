package cl.talentodigital.appmanageevents.controllers;


import cl.talentodigital.appmanageevents.services.SalonServicio;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;




@Controller
public class SalonController {

    @Autowired
    SalonServicio salonServicio;


}
