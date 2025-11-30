package com.example.springbootagrolink.controllers;

import com.example.springbootagrolink.model.Administrador;
import com.example.springbootagrolink.services.AdministradorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/administradores")
public class AdministradorController {

    @Autowired
    private AdministradorService administradorService;

    // GET - Obtener todos los Adminsitradores
    @GetMapping
    public String obtenerTodosLosAdministradores( Model model) {
        model.addAttribute("administradores", administradorService.obtenerTodosLosAdministradores());
        return "administradores/lista"; // Nombre de la vista (HTML/JSP) que mostrará la lista de administradores
    }

    //GET - Obtener administrador por ID
    @GetMapping("/{id}")
    public String ObtenerAdministradorPorId(@PathVariable Integer id, Model model){
        Administrador administrador = administradorService.obtenerAdministradorPorId(id).orElse(null);
        model.addAttribute("administrador", administrador);
        return "administradores/detalle"; // Nombre de la vista (HTML/JSP)
    }

    // GET - Mostrar formulario para crear nuevo administrador
    @GetMapping("/crear")
    public String mostrarFormularioCrear(Model model) {
        model.addAttribute("administrador", new Administrador());
        return "administradores/crear"; // Nombre de la vista (HTML/JSP) para crear un nuevo administrador
    }

    // POST - Guardar nuevo administrador (procesar el formulario)
    @PostMapping("/guardar")
    public String crearAdministrador(@ModelAttribute Administrador administrador) {
        administradorService.crearAdministrador(administrador);
        return "redirect:/administradores"; // Redirigir a la lista de administradores después de crear
    }

    // POST - Actualizar administrador
    @PostMapping("/{id}")
    public String actualizarAdministrador(@PathVariable Integer id, @ModelAttribute Administrador administrador) {
        administradorService.actualizarAdministrador(id, administrador);
        return "redirect:/administradores"; // Redirigir a la lista de administradores después de actualizar
    }

    //Delete - Eliminar administrador
    @GetMapping("/eliminar/{id}")
    public String eliminarAdministrador(@PathVariable Integer id) {
        administradorService.eliminarAdministrador(id);
        return "redirect:/administradores"; // Redirigir a la lista de administradores después de eliminar
    }



}
