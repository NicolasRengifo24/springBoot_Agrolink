package com.example.springbootagrolink.controllers;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class ServicioController {

    @GetMapping("/servicio/dashboard")
    public String dashboard(Model model) {
        model.addAttribute("mensaje", "Bienvenido al panel de Prestadores de Servicio");
        return "servicio/dashboard";
    }
}

