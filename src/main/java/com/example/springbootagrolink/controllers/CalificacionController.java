package com.example.springbootagrolink.controllers;

import com.example.springbootagrolink.model.Calificacion;
import com.example.springbootagrolink.services.CalificacionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

@Controller
@RequestMapping("/calificaciones")
public class CalificacionController {

    @Autowired
    private CalificacionService calificacionService;

    // Listar todas
    @GetMapping
    public String listar(Model model) {
        model.addAttribute("calificaciones", calificacionService.obtenerTodos());
        return "calificaciones/lista";
    }

    // Formulario de nueva calificación
    @GetMapping("/nuevo")
    public String nuevo(Model model) {
        model.addAttribute("calificacion", new Calificacion());
        return "calificaciones/crear";
    }

    // Guardar nueva calificación
    @PostMapping
    public String guardar(@ModelAttribute Calificacion calificacion) {
        calificacionService.guardar(calificacion);
        return "redirect:/calificaciones";
    }

    // Formulario de edición
    @GetMapping("/editar/{id}")
    public String editar(@PathVariable Integer id, Model model) {
        Calificacion cal = calificacionService.obtenerPorId(id).orElse(null);
        if (cal == null) return "redirect:/calificaciones";
        model.addAttribute("calificacion", cal);
        return "calificaciones/editar";
    }

    // Actualizar
    @PostMapping("/actualizar/{id}")
    public String actualizar(@PathVariable Integer id, @ModelAttribute Calificacion calificacion) {
        calificacionService.actualizar(id, calificacion);
        return "redirect:/calificaciones";
    }

    // Eliminar
    @GetMapping("/eliminar/{id}")
    public String eliminar(@PathVariable Integer id) {
        calificacionService.eliminar(id);
        return "redirect:/calificaciones";
    }

    // Búsqueda multicriterio por rangos de puntaje y promedio
    @GetMapping("/buscar")
    public String buscar(@RequestParam(required = false) BigDecimal puntajeMin,
                         @RequestParam(required = false) BigDecimal puntajeMax,
                         @RequestParam(required = false) BigDecimal promedioMin,
                         @RequestParam(required = false) BigDecimal promedioMax,
                         Model model) {
        model.addAttribute("calificaciones", calificacionService.buscar(puntajeMin, puntajeMax, promedioMin, promedioMax));
        // Reenviar los filtros para mantenerlos en la vista
        model.addAttribute("puntajeMin", puntajeMin);
        model.addAttribute("puntajeMax", puntajeMax);
        model.addAttribute("promedioMin", promedioMin);
        model.addAttribute("promedioMax", promedioMax);
        return "calificaciones/lista";
    }
}
