package cl.talentodigital.appmanageevents.controllers;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class ViewController {

    // La raiz devuelve la portada publica. Antes redirigia a /eventos, que
    // exige sesion, asi que quien llegaba desde el CV caia directo en el
    // formulario de acceso sin saber que era esta app.
    @GetMapping("/")
    public String raiz() {
        return "home";
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
