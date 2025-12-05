package com.example.springbootagrolink.controllers;

import com.example.springbootagrolink.model.Maquina;
import com.example.springbootagrolink.services.MaquinaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/maquinas")
public class MaquinaController {

    @Autowired
    private MaquinaService maquinaService;

    // GET - Listar todas las máquinas
    @GetMapping
    public String listarMaquinas(Model model) {
        model.addAttribute("maquinas", maquinaService.obtenerTodasLasMaquinas());
        return "maquinas/lista";
    }

    // GET - Detalle por ID
    @GetMapping("/{id}")
    public String obtenerMaquinaPorId(@PathVariable Integer id, Model model) {
        Maquina maquina = maquinaService.obtenerMaquinaPorId(id).orElse(null);
        model.addAttribute("maquina", maquina);
        return "maquinas/detalle";
    }

    // GET - Formulario crear
    @GetMapping("/crear")
    public String mostrarFormularioCrear(Model model) {
        model.addAttribute("maquina", new Maquina());
        return "maquinas/crear";
    }

    // POST - Guardar nueva máquina
    @PostMapping("/guardar")
    public String guardarMaquina(@ModelAttribute Maquina maquina) {
        maquinaService.guardarMaquina(maquina);
        return "redirect:/maquinas";
    }

    // POST - Actualizar una máquina existente
    @PostMapping("/{id}")
    public String actualizarMaquina(@PathVariable Integer id, @ModelAttribute Maquina maquina) {
        maquina.setIdMaquina(id);
        maquinaService.guardarMaquina(maquina);
        return "redirect:/maquinas";
    }

    // GET - Eliminar
    @GetMapping("/eliminar/{id}")
    public String eliminarMaquina(@PathVariable Integer id) {
        maquinaService.eliminarMaquina(id);
        return "redirect:/maquinas";
    }
}


