package com.example.springbootagrolink.controllers;

import com.example.springbootagrolink.model.Productor;
import com.example.springbootagrolink.services.ProductorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/productores")
public class ProductorController {

    @Autowired
    private ProductorService productorService;

    @GetMapping
    public String listarProductores(Model model) {
        model.addAttribute("productores", productorService.obtenerTodos());
        return "productores/lista";
    }

    @GetMapping("/{id}")
    public String mostrarDetalleProductor(@PathVariable Integer id, Model model) {
        model.addAttribute("productor", productorService.obtenerPorId(id).orElse(null));
        return "productores/detalle";
    }

    @GetMapping("/nuevo")
    public String mostrarFormularioCrearProductor(Model model) {
        model.addAttribute("productor", new Object());
        return "productores/crear";
    }

    @PostMapping("/guardar")
    public String guardarProductor(Productor productor) {
        productorService.guardar(productor);
        return "redirect:/productores";
    }

    @GetMapping("/editar/{id}")
    public String mostrarFormularioEditarProductor(@PathVariable Integer id, Model model) {
        Productor productorActual = productorService.obtenerPorId(id).orElse(null);
        if (productorActual == null) {
            return "redirect:/productores";
        } else {
            model.addAttribute("productor", productorActual);
            return "productores/editar";
        }
    }

    @PostMapping("/actualizar/{id}")
    public String actualizarProductor(@PathVariable Integer id, Productor productor) {
        productorService.actualizar(id, productor);
        return "redirect:/productores";
    }


    @PostMapping("/eliminar/{id}")
    public String eliminarProductor(@PathVariable Integer id) {
        productorService.eliminar(id);
        return "redirect:/productores";
    }
}






