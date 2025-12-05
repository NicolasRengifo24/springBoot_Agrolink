package com.example.springbootagrolink.controllers;

import com.example.springbootagrolink.model.Cliente;
import com.example.springbootagrolink.model.Producto;
import com.example.springbootagrolink.services.ClienteService;
import com.example.springbootagrolink.services.ProductoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpSession;
import java.util.*;

@Controller
public class ClienteController {

    @Autowired
    private ClienteService clienteService;
    @Autowired
    private ProductoService productoService;

    // Ruta principal para mostrar productos en el index
    @GetMapping("/")
    public String inicio(Model model) {
        List<Producto> productos = productoService.obtenerTodos();
        model.addAttribute("productos", productos); // Corregido: usar "productos" no "producto"
        return "cliente/index";
    }

    @GetMapping("/inicio")
    public String Cliente(Model model) {
        List<Producto> productos = productoService.obtenerTodos();
        model.addAttribute("productos", productos); // Corregido: usar "productos" no "producto"
        return "cliente/index";
    }

    // Nuevo: mostrar detalle de producto en cliente
    @GetMapping("/cliente/producto/{id}")
    public String verProductoCliente(@PathVariable Integer id, Model model) {
        Optional<Producto> productoOpt = productoService.obtenerPorId(id);
        if (productoOpt.isEmpty()) {

        }
        Producto producto = productoOpt.get();
        model.addAttribute("producto", producto);

        // productos relacionados: mismos categoria (excepto el actual), limitar a 4
        List<Producto> relacionados = new ArrayList<>();
        if (producto.getCategoria() != null) {
            List<Producto> todos = productoService.obtenerTodos();
            for (Producto p : todos) {
                if (!Objects.equals(p.getIdProducto(), producto.getIdProducto()) && p.getCategoria() != null
                        && Objects.equals(p.getCategoria().getIdCategoria(), producto.getCategoria().getIdCategoria())) {
                    relacionados.add(p);
                    if (relacionados.size() >= 4) break;
                }
            }
        }
        model.addAttribute("relacionados", relacionados);
        return "cliente/producto";
    }

    // Manejo sencillo de carrito en sesión (map: idProducto -> cantidad)
    @PostMapping("/cliente/carrito/agregar")
    public String agregarAlCarrito(@RequestParam Integer idProducto,
                                   @RequestParam(defaultValue = "1") Integer cantidad,
                                   HttpSession session) {
        @SuppressWarnings("unchecked")
        Map<Integer, Integer> cart = (Map<Integer, Integer>) session.getAttribute("cart");
        if (cart == null) {
            cart = new HashMap<>();
        }
        cart.put(idProducto, cart.getOrDefault(idProducto, 0) + cantidad);
        session.setAttribute("cart", cart);
        return "redirect:/cliente/producto/" + idProducto;
    }

    // GET - Listar todos los clientes
    @GetMapping("/clientes")
    public String obtenerTodosLosClientes(Model model) {
        model.addAttribute("clientes", clienteService.obtenerTodosLosClientes());
        return "clientes/lista"; // Vista HTML para listar clientes
    }

    // GET - Obtener cliente por ID
    @GetMapping("/clientes/{id}")
    public String obtenerClientePorId(@PathVariable Integer id, Model model) {
        Cliente cliente = clienteService.obtenerClientePorId(id).orElse(null);
        model.addAttribute("cliente", cliente);
        return "clientes/detalle"; // Vista HTML para detalle del cliente
    }

    // GET - Mostrar formulario para crear cliente
    @GetMapping("/clientes/crear")
    public String mostrarFormularioCrear(Model model) {
        model.addAttribute("cliente", new Cliente());
        return "clientes/crear"; // Vista HTML para crear cliente
    }

    // POST - Guardar cliente nuevo
    @PostMapping("/clientes/guardar")
    public String crearCliente(@ModelAttribute Cliente cliente) {
        clienteService.crearCliente(cliente);
        return "redirect:/clientes"; // Redirigir a la lista
    }

    // POST - Actualizar cliente
    @PostMapping("/clientes/{id}")
    public String actualizarCliente(@PathVariable Integer id, @ModelAttribute Cliente cliente) {
        clienteService.actualizarCliente(id, cliente);
        return "redirect:/clientes";
    }

    // GET - Eliminar cliente
    @GetMapping("/clientes/eliminar/{id}")
    public String eliminarCliente(@PathVariable Integer id) {
        clienteService.eliminarCliente(id);
        return "redirect:/clientes";
    }
}
