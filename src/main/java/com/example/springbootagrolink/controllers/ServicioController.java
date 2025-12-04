package com.example.springbootagrolink.controllers;

import com.example.springbootagrolink.model.Servicio;
import com.example.springbootagrolink.services.ServicioService;
import com.example.springbootagrolink.services.AsesorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/servicios")
public class ServicioController {

    @Autowired
    private ServicioService servicioService;

    @Autowired
    private AsesorService asesorService;

    // GET - Listar todos los servicios
    @GetMapping
    public String obtenerTodosLosServicios(Model model) {
        model.addAttribute("servicios", servicioService.obtenerTodosLosServicios());
        return "servicios/lista";
    }

    // GET - Obtener servicio por ID
    @GetMapping("/{id}")
    public String obtenerServicioPorId(@PathVariable Integer id, Model model) {
        Servicio servicio = servicioService.obtenerServicioPorId(id).orElse(null);
        model.addAttribute("servicio", servicio);
        return "servicios/detalle";
    }

    // GET - Mostrar formulario crear servicio
    @GetMapping("/crear")
    public String mostrarFormularioCrear(Model model) {
        model.addAttribute("servicio", new Servicio());
        model.addAttribute("asesores", asesorService.obtenerTodosLosAsesores());
        return "servicios/crear";
    }

    // POST - Guardar servicio nuevo
    @PostMapping("/guardar")
    public String crearServicio(@ModelAttribute Servicio servicio) {
        servicioService.crearServicio(servicio);
        return "redirect:/servicios";
    }

    // POST - Actualizar servicio
    @PostMapping("/{id}")
    public String actualizarServicio(@PathVariable Integer id, @ModelAttribute Servicio servicio) {
        servicioService.actualizarServicio(id, servicio);
        return "redirect:/servicios";
    }

    // GET - Eliminar servicio
    @GetMapping("/eliminar/{id}")
    public String eliminarServicio(@PathVariable Integer id) {
        servicioService.eliminarServicio(id);
        return "redirect:/servicios";
    }
}

