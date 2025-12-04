package com.example.springbootagrolink.controllers;

import com.example.springbootagrolink.model.Producto;
import com.example.springbootagrolink.model.Productor;
import com.example.springbootagrolink.model.CategoriaProducto;
import com.example.springbootagrolink.services.ProductoService;
import com.example.springbootagrolink.services.ProductorService;
import com.example.springbootagrolink.services.CategoriaProductoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/productos")
public class ProductoController {
    @Autowired
    private ProductoService productoService;
    @Autowired
    private ProductorService productorService;
    @Autowired
    private CategoriaProductoService categoriaProductoService;

    @GetMapping
    public String listarProductos(Model model) {
        model.addAttribute("productos", productoService.obtenerTodos());
        // Añadir categorías para el formulario de filtros en la vista inicial
        model.addAttribute("categorias", categoriaProductoService.obtenerTodos());
        return "productos/lista";
    }

    // Detalle: solo coincide si {id} son dígitos
    @GetMapping("/{id:\\d+}")
    public String mostrarDetalleProducto(@PathVariable Integer id, Model model) {
        Producto producto = productoService.obtenerPorId(id).orElse(null);
        model.addAttribute("producto", producto);
        return "productos/detalle";
    }

    @GetMapping("/nuevo")
    public String mostrarFormularioCrearProducto(Model model) {
        model.addAttribute("producto", new Producto());
        model.addAttribute("productores", productorService.obtenerTodos());
        model.addAttribute("categorias", categoriaProductoService.obtenerTodos());
        return "productos/crear";
    }

    @PostMapping("/guardar")
    public String guardarProducto(@ModelAttribute Producto producto,
                                  @RequestParam Integer productorId,
                                  @RequestParam Integer categoriaId) {
        // Asociar productor y categoría obligatorios
        Productor productor = productorService.obtenerPorId(productorId)
                .orElseThrow(() -> new RuntimeException("Productor no encontrado con ID: " + productorId));
        CategoriaProducto categoria = categoriaProductoService.obtenerPorId(categoriaId)
                .orElseThrow(() -> new RuntimeException("Categoría no encontrada con ID: " + categoriaId));
        producto.setProductor(productor);
        producto.setCategoria(categoria);
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
            model.addAttribute("productores", productorService.obtenerTodos());
            model.addAttribute("categorias", categoriaProductoService.obtenerTodos());
            return "productos/editar";
        }
    }

    @PostMapping("/actualizar/{id}")
    public String actualizarProducto(@PathVariable Integer id,
                                     @ModelAttribute Producto producto,
                                     @RequestParam(required = false) Integer productorId,
                                     @RequestParam(required = false) Integer categoriaId) {
        // Si se envían nuevos IDs, validar y asignar
        if (productorId != null) {
            Productor productor = productorService.obtenerPorId(productorId)
                    .orElseThrow(() -> new RuntimeException("Productor no encontrado con ID: " + productorId));
            producto.setProductor(productor);
        }
        if (categoriaId != null) {
            CategoriaProducto categoria = categoriaProductoService.obtenerPorId(categoriaId)
                    .orElseThrow(() -> new RuntimeException("Categoría no encontrada con ID: " + categoriaId));
            producto.setCategoria(categoria);
        }
        productoService.actualizar(id, producto);
        return "redirect:/productos";
    }

    @PostMapping("/eliminar/{id}")
    public String eliminarProducto(@PathVariable Integer id) {
        productoService.eliminar(id);
        return "redirect:/productos";
    }

    @GetMapping("/buscar")
    public String buscarProductos(@RequestParam(required = false) String ubicacion,
                                  @RequestParam(required = false) Integer categoriaId,
                                  Model model) {
        // Interpretar 'Todas' (valor 0) como sin filtro
        if (categoriaId != null && categoriaId == 0) {
            categoriaId = null;
        }
        model.addAttribute("productos", productoService.buscarPorUbicacionYCategoria(ubicacion, categoriaId));
        // Para el formulario de filtros
        model.addAttribute("categorias", categoriaProductoService.obtenerTodos());
        model.addAttribute("ubicacion", ubicacion);
        model.addAttribute("categoriaId", categoriaId);
        return "productos/lista";
    }

    @GetMapping("/filtrar")
    public String mostrarFormularioFiltros(Model model) {
        model.addAttribute("categorias", categoriaProductoService.obtenerTodos());
        // Mantener valores previos si se vuelve desde lista
        model.addAttribute("ubicacion", "");
        model.addAttribute("categoriaId", null);
        return "productos/filtrar";
    }
}
