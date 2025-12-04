package com.example.springbootagrolink.controllers;

import com.example.springbootagrolink.model.Compra;
import com.example.springbootagrolink.model.DetalleCompra;
import com.example.springbootagrolink.services.CompraService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/compras")
public class CompraController {

    private final CompraService compraService;

    public CompraController(CompraService compraService) {
        this.compraService = compraService;
    }

    // -----------------------------------------------------------
    // GET - Listar compras
    // -----------------------------------------------------------
    @GetMapping
    public String listarCompras(
            Model model,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        Page<Compra> compras = compraService.buscarCompras(null, PageRequest.of(page, size));
        model.addAttribute("compras", compras);
        return "compras/lista";
    }

    // -----------------------------------------------------------
    // GET - Mostrar detalle de una compra
    // -----------------------------------------------------------
    @GetMapping("/{id}")
    public String detalleCompra(@PathVariable Integer id, Model model) {
        Compra compra = compraService.buscarCompras(
                (Specification<Compra>) (root, query, cb) ->
                        cb.equal(root.get("idCompra"), id),
                PageRequest.of(0, 1)
        ).stream().findFirst().orElse(null);

        model.addAttribute("compra", compra);
        return "compras/detalle";
    }

    // -----------------------------------------------------------
    // GET - Formulario para crear compra
    // -----------------------------------------------------------
    @GetMapping("/crear")
    public String mostrarFormularioCrear(Model model) {
        model.addAttribute("compra", new Compra());
        return "compras/crear";
    }

    // -----------------------------------------------------------
    // POST - Crear compra
    // -----------------------------------------------------------
    @PostMapping("/guardar")
    public String crearCompra(
            @RequestParam Integer idCliente,
            @RequestParam String metodoPago,
            @RequestParam String direccionEntrega
    ) {
        Compra nuevaCompra = compraService.crearCompra(idCliente, metodoPago, direccionEntrega);
        return "redirect:/compras/" + nuevaCompra.getIdCompra();
    }

    // -----------------------------------------------------------
    // POST - Agregar producto a una compra
    // -----------------------------------------------------------
    @PostMapping("/{idCompra}/agregar-producto")
    public String agregarProducto(
            @PathVariable Integer idCompra,
            @RequestParam Integer idProducto,
            @RequestParam Integer cantidad
    ) {
        DetalleCompra d = compraService.agregarProducto(idCompra, idProducto, cantidad);
        return "redirect:/compras/" + idCompra;
    }

    // -----------------------------------------------------------
    // GET - Cancelar compra
    // -----------------------------------------------------------
    @GetMapping("/cancelar/{idCompra}")
    public String cancelarCompra(@PathVariable Integer idCompra) {
        compraService.cancelarCompra(idCompra);
        return "redirect:/compras";
    }
}


