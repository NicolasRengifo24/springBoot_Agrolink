package com.example.springbootagrolink.controllers;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * Controlador para las vistas del Transportista
 */
@Controller
@RequestMapping("/transportista")
public class TransportistaController {

    /**
     * Dashboard principal del transportista
     */
    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        // Aquí puedes agregar la lógica para obtener datos del transportista
        // Por ejemplo: estadísticas, envíos activos, ganancias, etc.

        return "transportista/dashboard";
    }

    /**
     * Ver envíos disponibles para aceptar
     */
    @GetMapping("/envios")
    public String enviosDisponibles(Model model) {
        // Lógica para obtener envíos disponibles
        // model.addAttribute("envios", envioService.obtenerEnviosDisponibles());

        return "transportista/envios";
    }

    /**
     * Ver envíos aceptados por el transportista
     */
    @GetMapping("/mis-envios")
    public String misEnvios(Model model) {
        // Lógica para obtener los envíos del transportista
        // model.addAttribute("envios", envioService.obtenerEnviosPorTransportista(id));

        return "transportista/mis-envios";
    }

    /**
     * Gestión de vehículos del transportista
     */
    @GetMapping("/vehiculos")
    public String vehiculos(Model model) {
        // Lógica para obtener vehículos del transportista
        // model.addAttribute("vehiculos", vehiculoService.obtenerVehiculosPorTransportista(id));

        return "transportista/vehiculos";
    }

    /**
     * Seguimiento de envíos en tiempo real
     */
    @GetMapping("/seguimiento")
    public String seguimiento(Model model) {
        // Lógica para obtener datos de seguimiento
        // model.addAttribute("enviosActivos", envioService.obtenerEnviosActivosPorTransportista(id));

        return "transportista/seguimiento";
    }

    /**
     * Análisis de rentabilidad y estadísticas financieras
     */
    @GetMapping("/rentabilidad")
    public String rentabilidad(Model model) {
        // Lógica para obtener datos de rentabilidad
        // model.addAttribute("ingresos", reporteService.calcularIngresos(id));
        // model.addAttribute("gastos", reporteService.calcularGastos(id));

        return "transportista/rentabilidad";
    }

    /**
     * Perfil del transportista
     */
    @GetMapping("/perfil")
    public String perfil(Model model) {
        // Lógica para obtener datos del perfil del transportista
        // model.addAttribute("transportista", transportistaService.obtenerPorId(id));

        return "transportista/perfil";
    }
}

