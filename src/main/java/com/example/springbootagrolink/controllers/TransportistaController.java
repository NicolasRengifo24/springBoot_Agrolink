package com.example.springbootagrolink.controllers;

import com.example.springbootagrolink.model.Transportista;
import com.example.springbootagrolink.services.TransportistaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/transportistas")
public class TransportistaController {

    @Autowired
    private TransportistaService transportistaService;

    // LISTAR
    @GetMapping
    public String listar(Model model) {
        model.addAttribute("transportistas", transportistaService.obtenerTodos());
        return "transportistas/lista";
    }

    // DETALLE
    @GetMapping("/{id}")
    public String detalle(@PathVariable Integer id, Model model) {
        model.addAttribute("transportista",
                transportistaService.obtenerPorId(id).orElse(null));
        return "transportistas/detalle";
    }

    // FORMULARIO CREAR
    @GetMapping("/crear")
    public String crearForm(Model model) {
        model.addAttribute("transportista", new Transportista());
        return "transportistas/crear";
    }

    // GUARDAR NUEVO
    @PostMapping("/guardar")
    public String guardar(@ModelAttribute Transportista transportista) {
        transportistaService.crear(transportista);
        return "redirect:/transportistas";
    }

    // ACTUALIZAR
    @PostMapping("/{id}")
    public String actualizar(@PathVariable Integer id, @ModelAttribute Transportista transportista) {
        transportistaService.actualizar(id, transportista);
        return "redirect:/transportistas";
    }

    // ELIMINAR
    @GetMapping("/eliminar/{id}")
    public String eliminar(@PathVariable Integer id) {
        transportistaService.eliminar(id);
        return "redirect:/transportistas";
    }
}


