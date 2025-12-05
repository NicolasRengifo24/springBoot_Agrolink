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
import java.math.BigDecimal;
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
            model.addAttribute("idBuscado", id);
            return "cliente/producto-no-encontrado";
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
        // Log simple para depuración: imprimir el contenido del carrito
        System.out.println("[DEBUG] Carrito actual: " + cart);
        return "redirect:/cliente/producto/" + idProducto;
    }

    // Endpoint de depuración para inspeccionar el carrito en sesión (dev only)
    @GetMapping("/cliente/carrito/raw")
    @ResponseBody
    public Map<Integer, Integer> verCarritoRaw(HttpSession session) {
        @SuppressWarnings("unchecked")
        Map<Integer, Integer> cart = (Map<Integer, Integer>) session.getAttribute("cart");
        if (cart == null) return Collections.emptyMap();
        return cart;
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

    // Clase interna para representar item del carrito en la vista (DTO ligero para evitar LazyInitialization)
    public static class CartItem {
        private final Integer id;
        private final String nombre;
        private final String descripcion;
        private final java.math.BigDecimal precio;
        private final Integer cantidad;
        private final String imagenUrl;
        private final java.math.BigDecimal subtotal;

        public CartItem(Integer id, String nombre, String descripcion, java.math.BigDecimal precio, Integer cantidad, String imagenUrl) {
            this.id = id;
            this.nombre = nombre;
            this.descripcion = descripcion;
            this.precio = precio;
            this.cantidad = cantidad;
            this.imagenUrl = imagenUrl;
            this.subtotal = (precio == null) ? java.math.BigDecimal.ZERO : precio.multiply(java.math.BigDecimal.valueOf(cantidad));
        }

        public Integer getId() { return id; }
        public String getNombre() { return nombre; }
        public String getDescripcion() { return descripcion; }
        public java.math.BigDecimal getPrecio() { return precio; }
        public Integer getCantidad() { return cantidad; }
        public String getImagenUrl() { return imagenUrl; }
        public java.math.BigDecimal getSubtotal() { return subtotal; }
        public java.math.BigDecimal getSubtotalOld() {
            if (precio == null) return java.math.BigDecimal.ZERO;
            return precio.multiply(java.math.BigDecimal.valueOf(cantidad));
        }
    }

    // Mostrar carrito (obtiene ids y cantidades desde la sesión y carga productos desde BD)
    @GetMapping("/cliente/carrito")
    public String verCarrito(HttpSession session, Model model) {
        @SuppressWarnings("unchecked")
        Map<Integer, Integer> cart = (Map<Integer, Integer>) session.getAttribute("cart");
        List<CartItem> items = new ArrayList<>();
        java.math.BigDecimal subtotal = java.math.BigDecimal.ZERO;
        if (cart != null && !cart.isEmpty()) {
            for (Map.Entry<Integer, Integer> e : cart.entrySet()) {
                Integer idProd = e.getKey();
                Integer qty = e.getValue();
                productoService.obtenerPorId(idProd).ifPresent(prod -> {
                    String img = "/imag/placeholder.jpg";
                    if (prod.getImagenesProducto() != null && !prod.getImagenesProducto().isEmpty()) {
                        img = "/" + prod.getImagenesProducto().get(0).getUrlImagen();
                    }
                    items.add(new CartItem(prod.getIdProducto(), prod.getNombreProducto(), prod.getDescripcionProducto(), prod.getPrecio(), qty, img));
                });
            }
            for (CartItem it : items) {
                subtotal = subtotal.add(it.getSubtotal());
            }
        }
        // Debug: imprimir resumen del carrito y tamaño
        System.out.println("[DEBUG] verCarrito - items=" + items.size() + " cartMap=" + cart);
        java.math.BigDecimal envio = java.math.BigDecimal.valueOf(7000);
        model.addAttribute("cartItems", items);
        model.addAttribute("subtotal", subtotal);
        model.addAttribute("envio", envio);
        model.addAttribute("total", subtotal.add(envio));
        int cartCount = 0;
        if (cart != null) {
            for (Integer q : cart.values()) cartCount += q == null ? 0 : q;
        }
        model.addAttribute("cartCount", cartCount);
        return "cliente/carrito";
    }

    // Actualizar cantidad de un producto en el carrito
    @PostMapping("/cliente/carrito/actualizar")
    public String actualizarCantidadCarrito(@RequestParam Integer idProducto,
                                            @RequestParam Integer cantidad,
                                            HttpSession session) {
        @SuppressWarnings("unchecked")
        Map<Integer, Integer> cart = (Map<Integer, Integer>) session.getAttribute("cart");
        if (cart == null) cart = new HashMap<>();
        if (cantidad != null && cantidad > 0) {
            cart.put(idProducto, cantidad);
        } else {
            cart.remove(idProducto);
        }
        session.setAttribute("cart", cart);
        return "redirect:/cliente/carrito";
    }

    // Quitar un producto del carrito
    @PostMapping("/cliente/carrito/quitar")
    public String quitarDelCarrito(@RequestParam Integer idProducto, HttpSession session) {
        @SuppressWarnings("unchecked")
        Map<Integer, Integer> cart = (Map<Integer, Integer>) session.getAttribute("cart");
        if (cart != null) {
            cart.remove(idProducto);
            session.setAttribute("cart", cart);
        }
        return "redirect:/cliente/carrito";
    }

    // Endpoint de prueba que añade 2 productos al carrito (solo para debug/testing local)
    @GetMapping("/cliente/carrito/add-test")
    public String addTestItemsToCart(HttpSession session) {
        @SuppressWarnings("unchecked")
        Map<Integer, Integer> cart = (Map<Integer, Integer>) session.getAttribute("cart");
        if (cart == null) cart = new HashMap<>();
        // intenta añadir ids 1 y 2 (si no existen en BD al mostrar se ignorarán)
        cart.put(1, cart.getOrDefault(1, 0) + 2);
        cart.put(2, cart.getOrDefault(2, 0) + 1);
        session.setAttribute("cart", cart);
        System.out.println("[DEBUG] Añadidos items de prueba: " + cart);
        return "redirect:/cliente/carrito";
    }
}
