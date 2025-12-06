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
                        @RequestParam(value = "ubicacion", required = false) String ubicacion,
                        @RequestParam(value = "precioMin", required = false) Double precioMin,
                        @RequestParam(value = "precioMax", required = false) Double precioMax,
                        @RequestParam(value = "soloDisponibles", required = false) Boolean soloDisponibles,
                        @RequestParam(value = "organicosBPA", required = false) Boolean organicosBPA,
                        Model model) {
        List<Producto> productos = productoService.obtenerTodos(); // Inicializar por defecto
        List<CategoriaProducto> categorias = categoriaProductoService.obtenerTodos();
        List<Servicio> servicios = servicioService.obtenerTodosLosServicios();
        Map<String, List<Servicio>> categoriasServicios = categorizarServicios(servicios);
        String tituloSeccion = "Productos Frescos del Campo";
        String subtituloSeccion = "Directamente desde nuestros productores certificados";

        // Determinar si hay filtros aplicados
        boolean hayFiltros = categoriaId != null ||
                           (busqueda != null && !busqueda.trim().isEmpty()) ||
                           (ubicacion != null && !ubicacion.trim().isEmpty()) ||
                           precioMin != null || precioMax != null ||
                           Boolean.TRUE.equals(soloDisponibles) ||
                           Boolean.TRUE.equals(organicosBPA);

        if (hayFiltros) {
            // Aplicar filtros combinados
            productos = aplicarFiltrosCombinados(busqueda, categoriaId, ubicacion, precioMin, precioMax, soloDisponibles, organicosBPA);
            tituloSeccion = "Productos Filtrados";
            subtituloSeccion = "Se encontraron " + productos.size() + " productos según los filtros aplicados";
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

        // Agregar todos los parámetros de filtros al modelo para mantenerlos en la vista
        model.addAttribute("ubicacionActual", ubicacion);
        model.addAttribute("precioMinActual", precioMin);
        model.addAttribute("precioMaxActual", precioMax);
        model.addAttribute("soloDisponiblesActual", soloDisponibles);
        model.addAttribute("organicosBPAActual", organicosBPA);
        model.addAttribute("hayFiltrosActivos", hayFiltros);

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

    /**
     * Ruta específica para búsquedas del navbar con normalización de texto
     */
    @GetMapping("/buscar")
    public String buscarDesdeNavbar(@RequestParam(value = "busqueda", required = false) String busqueda,
                                   Model model) {
        System.out.println("=== MÉTODO /buscar EJECUTADO ===");
        System.out.println("Parámetro recibido: '" + busqueda + "'");

        try {
            List<CategoriaProducto> categorias = categoriaProductoService.obtenerTodos();
            List<Servicio> servicios = servicioService.obtenerTodosLosServicios();
            Map<String, List<Servicio>> categoriasServicios = categorizarServicios(servicios);

            List<Producto> productos;
            String tituloSeccion = "Productos Frescos del Campo";
            String subtituloSeccion = "Directamente desde nuestros productores certificados";

            if (busqueda != null && !busqueda.trim().isEmpty()) {
                System.out.println("Ejecutando búsqueda para: " + busqueda.trim());
                // Aplicar búsqueda mejorada con normalización de texto
                productos = busquedaAvanzadaConNormalizacion(busqueda.trim());
                tituloSeccion = "Resultados de búsqueda";
                subtituloSeccion = "Se encontraron " + productos.size() + " productos para: \"" + busqueda.trim() + "\"";
                System.out.println("Productos encontrados: " + productos.size());
            } else {
                System.out.println("Búsqueda vacía, mostrando todos los productos");
                productos = productoService.obtenerTodos();
            }

            // Agregar atributos necesarios para el navbar
            model.addAttribute("productos", productos);
            model.addAttribute("categorias", categorias);
            model.addAttribute("categoriasServicios", categoriasServicios);
            model.addAttribute("tituloSeccion", tituloSeccion);
            model.addAttribute("subtituloSeccion", subtituloSeccion);
            model.addAttribute("busquedaActual", busqueda);
            model.addAttribute("categoriaSeleccionada", null);
            model.addAttribute("ubicacionActual", null);
            model.addAttribute("hayFiltrosActivos", busqueda != null && !busqueda.trim().isEmpty());

            System.out.println("Modelo configurado correctamente, retornando vista");
            return "cliente/index";

        } catch (Exception e) {
            System.err.println("Error en búsqueda: " + e.getMessage());
            e.printStackTrace();
            return "redirect:/";
        }
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

    /**
     * Método para aplicar filtros combinados de productos
     */
    private List<Producto> aplicarFiltrosCombinados(String busqueda, Integer categoriaId, String ubicacion,
                                                   Double precioMin, Double precioMax, Boolean soloDisponibles, Boolean organicosBPA) {
        // Comenzar with todos los productos
        List<Producto> productos = productoService.obtenerTodos();

        // DEBUG: Verificar si hay productos de Medellín
        long productosMedellin = productos.stream()
            .filter(p -> p.getProductor() != null &&
                       p.getProductor().getUsuario() != null &&
                       p.getProductor().getUsuario().getCiudad() != null &&
                       normalizarTexto(p.getProductor().getUsuario().getCiudad()).contains("medellin"))
            .count();
        System.out.println("=== Productos de Medellín en BD: " + productosMedellin);

        // Aplicar filtro de búsqueda (nombre o descripción)
        if (busqueda != null && !busqueda.trim().isEmpty()) {
            String terminoBusqueda = busqueda.trim().toLowerCase();
            productos = productos.stream()
                .filter(p -> (p.getNombreProducto() != null && p.getNombreProducto().toLowerCase().contains(terminoBusqueda)) ||
                           (p.getDescripcionProducto() != null && p.getDescripcionProducto().toLowerCase().contains(terminoBusqueda)))
                .toList();
        }

        // Aplicar filtro por categoría
        if (categoriaId != null) {
            productos = productos.stream()
                .filter(p -> p.getCategoria() != null && p.getCategoria().getIdCategoria().equals(categoriaId))
                .toList();
        }

        // Aplicar filtro por ubicación (ciudad del productor) - Mejorado para acentos y variaciones
        if (ubicacion != null && !ubicacion.trim().isEmpty()) {
            String ubicacionBusqueda = normalizarTexto(ubicacion.trim());

            // Debug: Imprimir ciudades disponibles (temporal)
            System.out.println("=== DEBUG: Buscando ubicación: " + ubicacion + " (normalizado: " + ubicacionBusqueda + ")");
            System.out.println("Ciudades disponibles en la BD:");
            productos.stream()
                .filter(p -> p.getProductor() != null &&
                           p.getProductor().getUsuario() != null &&
                           p.getProductor().getUsuario().getCiudad() != null)
                .map(p -> p.getProductor().getUsuario().getCiudad())
                .distinct()
                .forEach(ciudad -> System.out.println("- " + ciudad));

            productos = productos.stream()
                .filter(p -> {
                    if (p.getProductor() == null ||
                        p.getProductor().getUsuario() == null ||
                        p.getProductor().getUsuario().getCiudad() == null) {
                        return false;
                    }

                    String ciudadProductor = normalizarTexto(p.getProductor().getUsuario().getCiudad());
                    boolean coincide = ciudadProductor.contains(ubicacionBusqueda);

                    // También buscar en departamento/estado si existe
                    if (!coincide && p.getProductor().getUsuario().getDepartamento() != null) {
                        String departamentoProductor = normalizarTexto(p.getProductor().getUsuario().getDepartamento());
                        coincide = departamentoProductor.contains(ubicacionBusqueda);
                    }

                    return coincide;
                })
                .toList();

            System.out.println("Productos encontrados después del filtro de ubicación: " + productos.size());
        }

        // Aplicar filtro por precio mínimo
        if (precioMin != null) {
            productos = productos.stream()
                .filter(p -> p.getPrecio() != null && p.getPrecio().doubleValue() >= precioMin)
                .toList();
        }

        // Aplicar filtro por precio máximo
        if (precioMax != null) {
            productos = productos.stream()
                .filter(p -> p.getPrecio() != null && p.getPrecio().doubleValue() <= precioMax)
                .toList();
        }

        // Aplicar filtro solo productos disponibles
        if (Boolean.TRUE.equals(soloDisponibles)) {
            productos = productos.stream()
                .filter(p -> p.getStock() != null && p.getStock() > 0)
                .toList();
        }

        // Aplicar filtro orgánicos/BPA (simulado por ahora)
        if (Boolean.TRUE.equals(organicosBPA)) {
            productos = productos.stream()
                .filter(p -> p.getDescripcionProducto() != null &&
                           (p.getDescripcionProducto().toLowerCase().contains("orgánico") ||
                            p.getDescripcionProducto().toLowerCase().contains("bpa") ||
                            p.getDescripcionProducto().toLowerCase().contains("certificado")))
                .toList();
        }

        return productos;
    }

    /**
     * Método de búsqueda avanzada con normalización de texto
     */
    private List<Producto> busquedaAvanzadaConNormalizacion(String terminoBusqueda) {
        System.out.println("=== INICIANDO BÚSQUEDA AVANZADA ===");

        List<Producto> todosProductos = productoService.obtenerTodos();
        System.out.println("Total de productos en BD: " + todosProductos.size());

        String terminoNormalizado = normalizarTexto(terminoBusqueda);
        System.out.println("Término de búsqueda: '" + terminoBusqueda + "' → normalizado: '" + terminoNormalizado + "'");

        // DEBUG: Mostrar algunos productos disponibles
        System.out.println("=== PRIMEROS 5 PRODUCTOS EN BD ===");
        todosProductos.stream().limit(5).forEach(p -> {
            String ciudad = (p.getProductor() != null && p.getProductor().getUsuario() != null)
                ? p.getProductor().getUsuario().getCiudad() : "Sin ciudad";
            String productor = (p.getProductor() != null && p.getProductor().getUsuario() != null)
                ? p.getProductor().getUsuario().getNombre() : "Sin productor";
            System.out.println("- " + p.getNombreProducto() + " | Ciudad: " + ciudad + " | Productor: " + productor);
        });

        List<Producto> productosEncontrados = new ArrayList<>();

        for (Producto producto : todosProductos) {
            boolean encontrado = false;
            String razonEncontrado = "";

            try {
                // Buscar en nombre del producto
                if (producto.getNombreProducto() != null) {
                    String nombreNormalizado = normalizarTexto(producto.getNombreProducto());
                    if (nombreNormalizado.contains(terminoNormalizado)) {
                        encontrado = true;
                        razonEncontrado = "nombre: " + producto.getNombreProducto();
                    }
                }

                // Buscar en descripción del producto
                if (!encontrado && producto.getDescripcionProducto() != null) {
                    String descripcionNormalizada = normalizarTexto(producto.getDescripcionProducto());
                    if (descripcionNormalizada.contains(terminoNormalizado)) {
                        encontrado = true;
                        razonEncontrado = "descripción: " + producto.getDescripcionProducto();
                    }
                }

                // Buscar en ciudad del productor
                if (!encontrado && producto.getProductor() != null &&
                    producto.getProductor().getUsuario() != null &&
                    producto.getProductor().getUsuario().getCiudad() != null) {
                    String ciudadNormalizada = normalizarTexto(producto.getProductor().getUsuario().getCiudad());
                    if (ciudadNormalizada.contains(terminoNormalizado)) {
                        encontrado = true;
                        razonEncontrado = "ciudad: " + producto.getProductor().getUsuario().getCiudad();
                    }
                }

                // Buscar en nombre del productor
                if (!encontrado && producto.getProductor() != null &&
                    producto.getProductor().getUsuario() != null &&
                    producto.getProductor().getUsuario().getNombre() != null) {
                    String nombreProductorNormalizado = normalizarTexto(producto.getProductor().getUsuario().getNombre());
                    if (nombreProductorNormalizado.contains(terminoNormalizado)) {
                        encontrado = true;
                        razonEncontrado = "productor: " + producto.getProductor().getUsuario().getNombre();
                    }
                }

                // Buscar en categoría
                if (!encontrado && producto.getCategoria() != null) {
                    String categoriaNormalizada = normalizarTexto(producto.getCategoria().getNombreCategoria());
                    if (categoriaNormalizada.contains(terminoNormalizado)) {
                        encontrado = true;
                        razonEncontrado = "categoría: " + producto.getCategoria().getNombreCategoria();
                    }
                }

                if (encontrado) {
                    productosEncontrados.add(producto);
                    System.out.println("✓ Producto encontrado: " + producto.getNombreProducto() + " (" + razonEncontrado + ")");
                }

            } catch (Exception e) {
                System.err.println("Error procesando producto ID " + producto.getIdProducto() + ": " + e.getMessage());
            }
        }

        System.out.println("=== RESULTADO BÚSQUEDA: " + productosEncontrados.size() + " productos encontrados ===");

        return productosEncontrados;
    }

    /**
     * Método auxiliar para normalizar texto eliminando acentos y convirtiendo a minúsculas
     */
    private String normalizarTexto(String texto) {
        if (texto == null) {
            return "";
        }

        // Convertir a minúsculas y eliminar acentos comunes
        return texto.toLowerCase()
                   .replace("á", "a").replace("à", "a").replace("ä", "a")
                   .replace("é", "e").replace("è", "e").replace("ë", "e")
                   .replace("í", "i").replace("ì", "i").replace("ï", "i")
                   .replace("ó", "o").replace("ò", "o").replace("ö", "o")
                   .replace("ú", "u").replace("ù", "u").replace("ü", "u")
                   .replace("ñ", "n")
                   .trim();
    }
}
