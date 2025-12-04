package com.example.springbootagrolink.controllers;

import com.example.springbootagrolink.model.Producto;
import com.example.springbootagrolink.services.ProductoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/productos")
public class ProductoController {
    @Autowired
    private ProductoService productoService;

    @GetMapping
    public String listarProductos(Model model) {
        model.addAttribute("productos", productoService.obtenerTodos());
        return "productos/lista";
    }

    @GetMapping("/{id}")
    public String mostrarDetalleProducto(@PathVariable Integer id, Model model) {
        Producto producto = productoService.obtenerPorId(id).orElse(null);
        model.addAttribute("producto", producto);
        return "productos/detalle";
    }

    @GetMapping("/nuevo)")
    public String mostrarFormularioCrearProducto(Model model) {
        model.addAttribute("producto", new Producto());
        return "productos/crear";
    }

    @PostMapping("/guardar")
    public String guardarProducto(Producto producto) {
        productoService.guardar(producto);
        return "redirect:/productos";
    }

    @GetMapping("/editar/{id}")
    public String mostrarFormularioEditarProducto(@PathVariable Integer id, Model model) {
        Producto productoActual = productoService.obtenerPorId(id).orElse(null);
        if(productoActual == null){
            return "redirect:/productos";
        } else {
            model.addAttribute("producto", productoActual);
            return "productos/editar";
        }
    }

    @PostMapping("/actualizar/{id}")
    public String actualizarProducto(@PathVariable Integer id, Producto producto) {
        productoService.actualizar(id, producto);
        return "redirect:/productos";
    }

    @PostMapping("/eliminar/{id}")
    public String eliminarProducto(@PathVariable Integer id) {
        productoService.eliminar(id);
        return "redirect:/productos";
    }


}
