package com.example.springbootagrolink.controllers;

import com.example.springbootagrolink.model.ProductoFinca;
import com.example.springbootagrolink.services.FincaService;
import com.example.springbootagrolink.services.ProductoFincaService;
import com.example.springbootagrolink.services.ProductoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Controlador para gestionar la relación Producto-Finca
 *
 * Usa el patrón: String + Model (igual que tus otros controladores)
 *
 * Model: objeto donde agregas datos para la vista
 * String: nombre de la vista a renderizar
 *
 * IMPORTANTE: El uso de @Query en el Repository NO afecta este Controller.
 * El Controller llama al Service, y el Service internamente llama al Repository.
 */
@Controller
@RequestMapping("/producto-finca")
public class ProductoFincaController {

    /**
     * Servicios inyectados con @Autowired (más simple)
     */
    @Autowired
    private ProductoFincaService productoFincaService;

    @Autowired
    private FincaService fincaService;

    @Autowired
    private ProductoService productoService;

    /**
     * GET /producto-finca
     * Lista todas las asociaciones Producto-Finca
     *
     * Model: para pasar datos a la vista
     * return: nombre de la vista (producto_finca/lista.html)
     */
    @GetMapping
    public String listarProductoFinca(Model model) {
        model.addAttribute("productoFincas", productoFincaService.obtenerTodos());
        return "producto_finca/lista";
    }

    /**
     * GET /producto-finca/{id}
     * Ver detalles de una asociación específica
     */
    @GetMapping("/{id}")
    public String verDetalle(@PathVariable Integer id, Model model) {
        ProductoFinca productoFinca = productoFincaService.obtenerPorId(id).orElse(null);
        model.addAttribute("productoFinca", productoFinca);
        return "producto_finca/detalle";
    }

    /**
     * GET /producto-finca/nuevo
     * Muestra formulario para crear nueva asociación
     */
    @GetMapping("/nuevo")
    public String mostrarFormularioCrear(Model model) {
        model.addAttribute("fincas", fincaService.obtenerTodos());
        model.addAttribute("productos", productoService.obtenerTodos());
        model.addAttribute("productoFinca", new ProductoFinca());
        return "producto_finca/crear";
    }

    /**
     * POST /producto-finca/guardar
     * Guarda una nueva asociación Producto-Finca
     *
     * Usa el método inteligente: asociarProductoConFinca()
     * - Si ya existe: actualiza
     * - Si no existe: crea
     */
    @PostMapping("/guardar")
    public String guardar(@RequestParam Integer fincaId,
                          @RequestParam Integer productoId,
                          @RequestParam(required = false) BigDecimal cantidadProduccion,
                          @RequestParam(required = false)
                          @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate fechaCosecha) {

        productoFincaService.asociarProductoConFinca(fincaId, productoId, cantidadProduccion, fechaCosecha);
        return "redirect:/producto-finca";
    }

    /**
     * GET /producto-finca/editar/{id}
     * Muestra formulario para editar asociación existente
     */
    @GetMapping("/editar/{id}")
    public String mostrarFormularioEditar(@PathVariable Integer id, Model model) {
        ProductoFinca productoFinca = productoFincaService.obtenerPorId(id).orElse(null);

        if (productoFinca == null) {
            return "redirect:/producto-finca";
        }

        model.addAttribute("productoFinca", productoFinca);
        model.addAttribute("fincas", fincaService.obtenerTodos());
        model.addAttribute("productos", productoService.obtenerTodos());
        return "producto_finca/editar";
    }

    /**
     * POST /producto-finca/actualizar/{id}
     * Actualiza una asociación existente
     */
    @PostMapping("/actualizar/{id}")
    public String actualizar(@PathVariable Integer id,
                             @ModelAttribute ProductoFinca productoFinca) {
        productoFincaService.actualizar(id, productoFinca);
        return "redirect:/producto-finca";
    }

    /**
     * GET /producto-finca/eliminar/{id}
     * Elimina una asociación
     */
    @GetMapping("/eliminar/{id}")
    public String eliminar(@PathVariable Integer id) {
        productoFincaService.eliminar(id);
        return "redirect:/producto-finca";
    }

    /**
     * GET /producto-finca/finca/{fincaId}/productos
     * Lista productos de una finca específica
     *
     * CASO DE USO: "Ver qué productos cultivo en mi finca"
     */
    @GetMapping("/finca/{fincaId}/productos")
    public String listarProductosDeFinca(@PathVariable Integer fincaId, Model model) {
        model.addAttribute("productoFincas", productoFincaService.obtenerProductosPorFinca(fincaId));
        model.addAttribute("finca", fincaService.obtenerPorId(fincaId).orElse(null));
        return "producto_finca/por-finca";
    }

    /**
     * GET /producto-finca/producto/{productoId}/fincas
     * Lista fincas que cultivan un producto específico
     *
     * CASO DE USO: "¿Dónde puedo comprar Aguacate?"
     */
    @GetMapping("/producto/{productoId}/fincas")
    public String listarFincasDelProducto(@PathVariable Integer productoId, Model model) {
        model.addAttribute("productoFincas", productoFincaService.obtenerFincasPorProducto(productoId));
        model.addAttribute("producto", productoService.obtenerPorId(productoId).orElse(null));
        return "producto_finca/por-producto";
    }

    /**
     * POST /producto-finca/desasociar
     * Elimina la asociación entre una finca y un producto
     */
    @PostMapping("/desasociar")
    public String desasociar(@RequestParam Integer fincaId,
                             @RequestParam Integer productoId) {
        productoFincaService.desasociarProductoDeFinca(fincaId, productoId);
        return "redirect:/producto-finca";
    }
}
