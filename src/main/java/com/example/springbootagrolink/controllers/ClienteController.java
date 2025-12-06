package com.example.springbootagrolink.controllers;

import com.example.springbootagrolink.model.Cliente;
import com.example.springbootagrolink.model.Producto;
import com.example.springbootagrolink.model.CategoriaProducto;
import com.example.springbootagrolink.model.Servicio;
import com.example.springbootagrolink.services.ClienteService;
import com.example.springbootagrolink.services.ProductoService;
import com.example.springbootagrolink.services.CategoriaProductoService;
import com.example.springbootagrolink.services.ServicioService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.ArrayList;

@Controller
public class ClienteController {

    @Autowired
    private ClienteService clienteService;
    @Autowired
    private ProductoService productoService;
    @Autowired
    private CategoriaProductoService categoriaProductoService;
    @Autowired
    private ServicioService servicioService;

    // Ruta principal para mostrar productos en el index
    @GetMapping("/")
    public String inicio(@RequestParam(value = "categoria", required = false) Integer categoriaId,
                        @RequestParam(value = "busqueda", required = false) String busqueda,
                        Model model) {
        List<Producto> productos = productoService.obtenerTodos(); // Inicializar por defecto
        List<CategoriaProducto> categorias = categoriaProductoService.obtenerTodos();
        List<Servicio> servicios = servicioService.obtenerTodosLosServicios();
        Map<String, List<Servicio>> categoriasServicios = categorizarServicios(servicios);
        String tituloSeccion = "Productos Frescos del Campo";
        String subtituloSeccion = "Directamente desde nuestros productores certificados";

        // Determinar qué tipo de filtro aplicar
        boolean hayFiltros = categoriaId != null || (busqueda != null && !busqueda.trim().isEmpty());

        if (hayFiltros) {
            // Usar filtros múltiples si hay más de uno activo
            if ((categoriaId != null ? 1 : 0) +
                (busqueda != null && !busqueda.trim().isEmpty() ? 1 : 0) > 1) {

                productos = productoService.buscarPorUbicacionYCategoria(busqueda, categoriaId);
                tituloSeccion = "Productos Filtrados";
                subtituloSeccion = "Resultados según los filtros aplicados";

            } else {
                // Aplicar filtro individual
                if (categoriaId != null) {
                    productos = productoService.obtenerPorCategoria(categoriaId);
                    CategoriaProducto categoria = categoriaProductoService.obtenerPorId(categoriaId).orElse(null);
                    if (categoria != null) {
                        tituloSeccion = categoria.getNombreCategoria();
                        subtituloSeccion = "Productos frescos de la categoría " + categoria.getNombreCategoria().toLowerCase();
                    }
                } else if (busqueda != null && !busqueda.trim().isEmpty()) {
                    String terminoBusqueda = busqueda.trim();
                    productos = productoService.busquedaAvanzada(terminoBusqueda);
                    tituloSeccion = "Resultados de búsqueda";
                    subtituloSeccion = "Se encontraron " + productos.size() + " productos para: \"" + terminoBusqueda + "\"";
                }
            }
        } else {
            // Mostrar todos los productos por defecto
            productos = productoService.obtenerTodos();
        }

        model.addAttribute("productos", productos);
        model.addAttribute("categorias", categorias);
        model.addAttribute("categoriasServicios", categoriasServicios);
        model.addAttribute("tituloSeccion", tituloSeccion);
        model.addAttribute("subtituloSeccion", subtituloSeccion);
        model.addAttribute("categoriaSeleccionada", categoriaId);
        model.addAttribute("busquedaActual", busqueda);

        return "cliente/index";
    }

    @GetMapping("/clientes/cliente-index")
    public String Cliente(Model model) {
        List<Producto> productos = productoService.obtenerTodos();
        List<CategoriaProducto> categorias = categoriaProductoService.obtenerTodos();
        List<Servicio> servicios = servicioService.obtenerTodosLosServicios();
        Map<String, List<Servicio>> categoriasServicios = categorizarServicios(servicios);

        model.addAttribute("productos", productos);
        model.addAttribute("categorias", categorias);
        model.addAttribute("categoriasServicios", categoriasServicios);
        model.addAttribute("tituloSeccion", "Productos Frescos del Campo");
        model.addAttribute("subtituloSeccion", "Directamente desde nuestros productores certificados");
        return "cliente/index";
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

    // Método privado para categorizar servicios por palabras clave en la descripción
    private Map<String, List<Servicio>> categorizarServicios(List<Servicio> servicios) {
        Map<String, List<Servicio>> categoriasServicios = new HashMap<>();

        // Inicializar categorías predefinidas
        categoriasServicios.put("Veterinaria", new ArrayList<>());
        categoriasServicios.put("Asesoría Agrícola", new ArrayList<>());
        categoriasServicios.put("Transporte", new ArrayList<>());
        categoriasServicios.put("Capacitación", new ArrayList<>());
        categoriasServicios.put("Maquinaria", new ArrayList<>());
        categoriasServicios.put("Otros", new ArrayList<>());

        for (Servicio servicio : servicios) {
            if (servicio.getDescripcion() != null && servicio.getEstado() == Servicio.EstadoServicio.Activo) {
                String descripcion = servicio.getDescripcion().toLowerCase();

                // Categorizar por palabras clave
                if (descripcion.contains("veterinari") || descripcion.contains("animal") ||
                    descripcion.contains("mascota") || descripcion.contains("ganad")) {
                    categoriasServicios.get("Veterinaria").add(servicio);
                } else if (descripcion.contains("asesor") || descripcion.contains("agricul") ||
                          descripcion.contains("cultivo") || descripcion.contains("siembra")) {
                    categoriasServicios.get("Asesoría Agrícola").add(servicio);
                } else if (descripcion.contains("transport") || descripcion.contains("envío") ||
                          descripcion.contains("logística") || descripcion.contains("entrega")) {
                    categoriasServicios.get("Transporte").add(servicio);
                } else if (descripcion.contains("capacita") || descripcion.contains("curso") ||
                          descripcion.contains("entrena") || descripcion.contains("taller")) {
                    categoriasServicios.get("Capacitación").add(servicio);
                } else if (descripcion.contains("maquina") || descripcion.contains("tracto") ||
                        descripcion.contains("mecanica") || descripcion.contains("motor")) {
                    categoriasServicios.get("Maquinaria").add(servicio);
                } else {
                    categoriasServicios.get("Otros").add(servicio);
                }
            }
        }

        // Remover categorías vacías
        categoriasServicios.entrySet().removeIf(entry -> entry.getValue().isEmpty());

        return categoriasServicios;
    }

    // NUEVA FUNCIONALIDAD PARA VISTAS DE CLIENTE

    /**
     * Mostrar vista individual de producto para clientes
     */
    @GetMapping("/cliente/producto/{id}")
    public String verProductoCliente(@PathVariable Integer id, Model model) {
        // Obtener el producto por ID
        Producto producto = productoService.obtenerPorId(id).orElse(null);
        if (producto == null) {
            return "cliente/producto-no-encontrado";
        }

        // Obtener productos relacionados (misma categoría, excluyendo el actual)
        List<Producto> productosRelacionados = new ArrayList<>();
        if (producto.getCategoria() != null) {
            List<Producto> todosDeLaCategoria = productoService.obtenerPorCategoria(producto.getCategoria().getIdCategoria());
            productosRelacionados = todosDeLaCategoria.stream()
                .filter(p -> !p.getIdProducto().equals(id))
                .limit(4)
                .toList();
        }

        // AGREGAR DATOS NECESARIOS PARA EL NAVBAR (igual que en el index)
        List<CategoriaProducto> categorias = categoriaProductoService.obtenerTodos();
        List<Servicio> servicios = servicioService.obtenerTodosLosServicios();
        Map<String, List<Servicio>> categoriasServicios = categorizarServicios(servicios);

        // Agregar al modelo
        model.addAttribute("producto", producto);
        model.addAttribute("relacionados", productosRelacionados);
        model.addAttribute("categorias", categorias);
        model.addAttribute("categoriasServicios", categoriasServicios);

        return "cliente/producto";
    }

    /**
     * Agregar producto al carrito
     */
    @PostMapping("/cliente/carrito/agregar")
    public String agregarAlCarrito(@RequestParam Integer idProducto,
                                   @RequestParam Integer cantidad,
                                   RedirectAttributes redirectAttributes) {
        try {
            // Verificar que el producto existe
            Producto producto = productoService.obtenerPorId(idProducto).orElse(null);
            if (producto == null) {
                redirectAttributes.addFlashAttribute("error", "Producto no encontrado");
                return "redirect:/";
            }

            // Verificar stock disponible
            if (cantidad > producto.getStock()) {
                redirectAttributes.addFlashAttribute("error",
                    "Cantidad solicitada (" + cantidad + ") supera el stock disponible (" + producto.getStock() + ")");
                return "redirect:/cliente/producto/" + idProducto;
            }

            // Por ahora, solo mostrar mensaje de éxito (implementación básica)
            // En el futuro aquí se integrará con el sistema de sesiones/usuarios
            redirectAttributes.addFlashAttribute("success",
                cantidad + " unidad(es) de " + producto.getNombreProducto() + " agregadas al carrito");

            return "redirect:/cliente/producto/" + idProducto;
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error al agregar producto al carrito");
            return "redirect:/cliente/producto/" + idProducto;
        }
    }

    /**
     * Ver carrito de compras
     */
    @GetMapping("/cliente/carrito")
    public String verCarrito(Model model) {
        // Por ahora retornar vista básica del carrito
        // En el futuro aquí se obtendrán los productos del carrito desde la sesión/base de datos
        model.addAttribute("carritoItems", new ArrayList<>());
        model.addAttribute("total", 0.0);
        return "cliente/carrito";
    }
}
