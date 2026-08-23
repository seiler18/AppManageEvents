package cl.talentodigital.appmanageevents.controllers;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class ViewController {

    @GetMapping("/")
    public String home() {
        return "redirect:/eventos";
    }

    // home.html existia en el repo pero ningun controlador la devolvia: era
    // codigo muerto. SecurityConfig ya declaraba "/home" como publica, asi que
    // la intencion era esta.
    @GetMapping("/home")
    public String inicio() {
        return "home";
    }

    @GetMapping("/login")
    public String login() {
        return "login";
    }

    @GetMapping("/register")
    public String register() {
        return "register";
    }
}
