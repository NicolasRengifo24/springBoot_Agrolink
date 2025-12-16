package com.example.springbootagrolink.controllers;

import com.example.springbootagrolink.model.ImagenProducto;
import com.example.springbootagrolink.services.ImagenProductoService;
import com.example.springbootagrolink.services.ProductoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Controller
@RequestMapping("/imagenes-producto")
public class ImgenProductoController {

    @Autowired
    private ImagenProductoService imagenProductoService;

    @Autowired
    private ProductoService productoService;

    @GetMapping
    public String listar(Model model) {
        model.addAttribute("imagenes", imagenProductoService.obtenerTodos());
        model.addAttribute("productos", productoService.obtenerTodos());
        return "imagenes/lista"; // Asegúrate de tener esta vista Thymeleaf
    }

    @GetMapping("/producto/{productoId}")
    public String listarPorProducto(@PathVariable Integer productoId, Model model) {
        List<ImagenProducto> imagenes = imagenProductoService.listarPorProducto(productoId);
        model.addAttribute("imagenes", imagenes);
        model.addAttribute("productoId", productoId);
        return "imagenes/lista";
    }

    @GetMapping("/nuevo")
    public String mostrarFormularioCrear(Model model) {
        model.addAttribute("imagen", new ImagenProducto());
        model.addAttribute("productos", productoService.obtenerTodos());
        return "imagenes/crear"; // Vista para crear imagen
    }

    @PostMapping("/guardar")
    public String guardar(@ModelAttribute ImagenProducto imagen,
                          @RequestParam Integer productoId,
                          @RequestParam(value = "archivo", required = false) MultipartFile archivo) {
        if (archivo != null && !archivo.isEmpty()) {
            imagenProductoService.guardar(imagen, productoId, archivo);
        } else {
            imagenProductoService.guardar(imagen, productoId);
        }
        return "redirect:/imagenes-producto";
    }

    @GetMapping("/editar/{id}")
    public String mostrarFormularioEditar(@PathVariable Integer id, Model model) {
        ImagenProducto imagen = imagenProductoService.obtenerPorId(id).orElse(null);
        if (imagen == null) {
            return "redirect:/imagenes-producto";
        }
        model.addAttribute("imagen", imagen);
        model.addAttribute("productos", productoService.obtenerTodos());
        return "imagenes/editar"; // Vista para editar imagen
    }

    @PostMapping("/actualizar/{id}")
    public String actualizar(@PathVariable Integer id,
                             @ModelAttribute ImagenProducto imagen,
                             @RequestParam(required = false) Integer productoId) {
        imagenProductoService.actualizar(id, imagen, productoId);
        return "redirect:/imagenes-producto";
    }

    @PostMapping("/eliminar/{id}")
    public String eliminar(@PathVariable Integer id) {
        imagenProductoService.eliminar(id);
        return "redirect:/imagenes-producto";
    }
}
