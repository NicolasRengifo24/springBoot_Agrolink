package com.example.springbootagrolink.controllers;

import com.example.springbootagrolink.model.Envio;
import com.example.springbootagrolink.services.EnvioService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/envios")
public class EnvioController {

    @Autowired
    private EnvioService envioService;

    // GET - Listar todos los envíos
    @GetMapping
    public String listarEnvios(Model model) {
        model.addAttribute("envios", envioService.obtenerTodos());
        return "envios/lista";
    }

    // GET - Ver detalles de un envío
    @GetMapping("/{id}")
    public String obtenerEnvioPorId(@PathVariable Integer id, Model model) {
        Envio envio = envioService.obtenerPorId(id).orElse(null);
        model.addAttribute("envio", envio);
        return "envios/detalle";
    }

    // GET - Mostrar formulario para crear
    @GetMapping("/crear")
    public String mostrarFormularioCrear(Model model) {
        model.addAttribute("envio", new Envio());
        return "envios/crear";
    }

    // POST - Guardar nuevo envío
    @PostMapping("/guardar")
    public String crearEnvio(@ModelAttribute Envio envio) {
        envioService.crear(envio);
        return "redirect:/envios";
    }

    // POST - Actualizar envío
    @PostMapping("/{id}")
    public String actualizarEnvio(@PathVariable Integer id, @ModelAttribute Envio envio) {
        envioService.actualizar(id, envio);
        return "redirect:/envios";
    }

    // GET - Eliminar envío
    @GetMapping("/eliminar/{id}")
    public String eliminarEnvio(@PathVariable Integer id) {
        envioService.eliminar(id);
        return "redirect:/envios";
    }
}

