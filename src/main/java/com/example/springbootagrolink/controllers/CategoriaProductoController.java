package com.example.springbootagrolink.controllers;


import com.example.springbootagrolink.model.CategoriaProducto;
import com.example.springbootagrolink.services.CategoriaProductoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/categorias-producto")
public class CategoriaProductoController {

    @Autowired
    private CategoriaProductoService categoriaProductoService;

    // GET - Listar todas las categorías
    @GetMapping
    public String listarCategorias(Model model) {
        model.addAttribute("categorias", categoriaProductoService.obtenerTodos());
        return "categoriasProducto/lista";
    }

    // GET - Detalle por ID
    @GetMapping("/{id}")
    public String obtenerCategoriaPorId(@PathVariable Integer id, Model model) {
        CategoriaProducto categoria = categoriaProductoService.obtenerPorId(id).orElse(null);
        model.addAttribute("categoria", categoria);
        return "categoriasProducto/detalle";
    }

    // GET - Formulario para crear
    @GetMapping("/crear")
    public String mostrarFormularioCrear(Model model) {
        model.addAttribute("categoriaProducto", new CategoriaProducto());
        return "categoriasProducto/crear";
    }

    // POST - Guardar nueva categoría
    @PostMapping("/guardar")
    public String guardarCategoria(@ModelAttribute CategoriaProducto categoriaProducto) {
        categoriaProductoService.guardar(categoriaProducto);
        return "redirect:/categorias-producto";
    }

    // POST - Actualizar categoría
    @PostMapping("/{id}")
    public String actualizarCategoria(
            @PathVariable Integer id,
            @ModelAttribute CategoriaProducto categoriaProducto) {

        categoriaProductoService.actualizar(id, categoriaProducto);
        return "redirect:/categorias-producto";
    }

    // GET - Eliminar
    @GetMapping("/eliminar/{id}")
    public String eliminarCategoria(@PathVariable Integer id) {
        categoriaProductoService.eliminar(id);
        return "redirect:/categorias-producto";
    }
}

