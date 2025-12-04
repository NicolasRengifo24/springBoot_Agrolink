// java
package com.example.springbootagrolink.controllers;

import com.example.springbootagrolink.model.Asesor;
import com.example.springbootagrolink.services.AsesorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/asesores")
public class AsesorController {

    @Autowired
    private AsesorService asesorService;

    // GET - Obtener todos los asesores
    @GetMapping
    public String obtenerTodosLosAsesores(Model model) {
        model.addAttribute("asesores", asesorService.obtenerTodosLosAsesores());
        return "asesores/lista";
    }

    // GET - Obtener asesor por ID
    @GetMapping("/{id}")
    public String obtenerAsesorPorId(@PathVariable Integer id, Model model) {
        Asesor asesor = asesorService.obtenerAsesorPorId(id).orElse(null);
        model.addAttribute("asesor", asesor);
        return "asesores/detalle";
    }

    // GET - Mostrar formulario para crear un nuevo asesor
    @GetMapping("/crear")
    public String mostrarFormularioCrear(Model model) {
        model.addAttribute("asesor", new Asesor());
        return "asesores/crear";
    }

    // POST - Guardar nuevo asesor
    @PostMapping("/guardar")
    public String crearAsesor(@ModelAttribute Asesor asesor) {
        asesorService.crearAsesor(asesor);
        return "redirect:/asesores";
    }

    // POST - Actualizar asesor
    @PostMapping("/{id}")
    public String actualizarAsesor(@PathVariable Integer id, @ModelAttribute Asesor asesor) {
        asesorService.actualizarAsesor(id, asesor);
        return "redirect:/asesores";
    }

    // GET - Eliminar asesor
    @GetMapping("/eliminar/{id}")
    public String eliminarAsesor(@PathVariable Integer id) {
        asesorService.eliminarAsesor(id);
        return "redirect:/asesores";
    }

}

