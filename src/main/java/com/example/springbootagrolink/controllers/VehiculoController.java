package com.example.springbootagrolink.controllers;

import com.example.springbootagrolink.model.Vehiculo;
import com.example.springbootagrolink.services.VehiculoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/vehiculos")
public class VehiculoController {

    @Autowired
    private VehiculoService vehiculoService;

    // GET - Listar vehículos
    @GetMapping
    public String listarVehiculos(Model model) {
        model.addAttribute("vehiculos", vehiculoService.obtenerTodos());
        return "vehiculos/lista";
    }

    // GET - Mostrar detalle de un vehículo
    @GetMapping("/{id}")
    public String obtenerVehiculoPorId(@PathVariable Integer id, Model model) {
        Vehiculo vehiculo = vehiculoService.obtenerPorId(id).orElse(null);
        model.addAttribute("vehiculo", vehiculo);
        return "vehiculos/detalle";
    }

    // GET - Formulario de creación
    @GetMapping("/crear")
    public String mostrarFormularioCrear(Model model) {
        model.addAttribute("vehiculo", new Vehiculo());
        return "vehiculos/crear";
    }

    // POST - Crear vehículo
    @PostMapping("/guardar")
    public String crearVehiculo(@ModelAttribute Vehiculo vehiculo) {
        vehiculoService.crear(vehiculo);
        return "redirect:/vehiculos";
    }

    // POST - Actualizar vehículo
    @PostMapping("/{id}")
    public String actualizarVehiculo(@PathVariable Integer id, @ModelAttribute Vehiculo vehiculo) {
        vehiculoService.actualizar(id, vehiculo);
        return "redirect:/vehiculos";
    }

    // GET - Eliminar vehículo
    @GetMapping("/eliminar/{id}")
    public String eliminarVehiculo(@PathVariable Integer id) {
        vehiculoService.eliminar(id);
        return "redirect:/vehiculos";
    }
}

