package com.example.springbootagrolink.controllers;

import com.example.springbootagrolink.model.Cliente;
import com.example.springbootagrolink.model.Producto;
import com.example.springbootagrolink.model.CategoriaProducto;
import com.example.springbootagrolink.model.Servicio;
import com.example.springbootagrolink.model.Usuario;
import com.example.springbootagrolink.services.ClienteService;
import com.example.springbootagrolink.services.ProductoService;
import com.example.springbootagrolink.services.CategoriaProductoService;
import com.example.springbootagrolink.services.ServicioService;
import com.example.springbootagrolink.repository.UsuarioRepository;
import com.example.springbootagrolink.repository.ProductoRepository;
import com.example.springbootagrolink.repository.EnvioRepository;
import com.example.springbootagrolink.repository.CalificacionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import jakarta.servlet.http.HttpSession;
import java.util.*;

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
    @Autowired
    private UsuarioRepository usuarioRepository;
    @Autowired
    private ProductoRepository productoRepository;
    @Autowired
    private EnvioRepository envioRepository;
    @Autowired
    private CalificacionRepository calificacionRepository;
    @Autowired
    private com.example.springbootagrolink.repository.CompraRepository compraRepository;
    @Autowired
    private com.example.springbootagrolink.repository.ClienteRepository clienteRepository;

    /**
     * Método que agrega el contador del carrito a todas las vistas automáticamente
     * El carrito es un Map<Integer, Integer> donde la clave es el ID del producto y el valor es la cantidad
     */
    @ModelAttribute("cartCount")
    public Integer getCartCount(HttpSession session) {
        @SuppressWarnings("unchecked")
        Map<Integer, Integer> carrito = (Map<Integer, Integer>) session.getAttribute("carrito");
        if (carrito == null || carrito.isEmpty()) {
            return 0;
        }
        return carrito.values().stream()
            .mapToInt(Integer::intValue)
            .sum();
    }

    /**
     * Método que agrega el nombre del usuario autenticado a todas las vistas automáticamente
     */
    @ModelAttribute("nombreUsuario")
    public String getNombreUsuario() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated() &&
            !"anonymousUser".equals(authentication.getPrincipal())) {
            String username = authentication.getName();
            // Obtener el usuario de la base de datos
            Usuario usuario = usuarioRepository.findByNombreUsuario(username).orElse(null);
            if (usuario != null) {
                return usuario.getNombre();
            }
        }
        return null;
    }

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
        List<Producto> productos;
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

        // --- NUEVO: obtener producto destacado por compras ---
        try {
            Producto productoDestacado = productoService.obtenerProductoMasVendido();
            model.addAttribute("productoDestacado", productoDestacado);
        } catch (Exception e) {
            // En caso de error, agregar atributo nulo para que la vista muestre fallback
            model.addAttribute("productoDestacado", null);
            // Log sencillo para debugging en consola
            System.err.println("Error obteniendo producto destacado: " + e.getMessage());
            e.printStackTrace();
        }
        // --- FIN producto destacado ---

        // --- NUEVO: Estadísticas dinámicas ---
        try {
            // 1. Total de usuarios registrados
            Long totalUsuarios = usuarioRepository.count();
            model.addAttribute("totalUsuarios", totalUsuarios);

            // 2. Productos disponibles (con stock > 0)
            Long productosDisponibles = productoRepository.countProductosDisponibles();
            model.addAttribute("productosDisponibles", productosDisponibles);

            // 3. Porcentaje de entregas exitosas
            Long totalEnvios = envioRepository.count();
            Long enviosFinalizados = envioRepository.countEnviosFinalizados();
            double porcentajeEntregas = 0.0;
            if (totalEnvios > 0) {
                porcentajeEntregas = (enviosFinalizados * 100.0) / totalEnvios;
            }
            model.addAttribute("porcentajeEntregas", porcentajeEntregas);

            // 4. Calificación promedio general
            Double promedioCalificaciones = calificacionRepository.calcularPromedioGeneral();
            if (promedioCalificaciones == null) {
                promedioCalificaciones = 0.0;
            }
            model.addAttribute("promedioCalificaciones", promedioCalificaciones);

        } catch (Exception e) {
            // En caso de error, poner valores por defecto
            model.addAttribute("totalUsuarios", 0L);
            model.addAttribute("productosDisponibles", 0L);
            model.addAttribute("porcentajeEntregas", 0.0);
            model.addAttribute("promedioCalificaciones", 0.0);
            System.err.println("Error calculando estadísticas: " + e.getMessage());
        }
        // --- FIN estadísticas ---

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
     * Ruta específica para clientes autenticados (redirección desde login)
     */
    @GetMapping("/cliente/index")
    public String clienteIndex(Model model) {
        // Reusar la lógica del método inicio
        return inicio(null, null, null, null, null, null, null, model);
    }


    // Endpoint para mostrar el perfil del cliente (con modelo preparado)
    @GetMapping("/cliente/perfil")
    public String perfilClientePreparado(Model model) {
        // Obtener usuario autenticado
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated() || "anonymousUser".equals(authentication.getPrincipal())) {
            return "redirect:/login";
        }
        String username = authentication.getName();
        Usuario usuario = usuarioRepository.findByNombreUsuario(username).orElse(null);
        if (usuario == null) return "redirect:/login";

        // Sólo permitir acceso a este endpoint si el usuario es ROLE_CLIENTE
        if (usuario.getRol() == null || !"ROLE_CLIENTE".equals(usuario.getRol().name())) {
            // Redirigir al controlador general de perfil que maneja distintos roles
            return "redirect:/perfil";
        }

        // Obtener cliente asociado
        Cliente cliente = clienteRepository.findByUsuario(usuario).orElse(null);

        model.addAttribute("usuario", usuario);
        model.addAttribute("title", "Mi Perfil - Cliente");
        model.addAttribute("cliente", cliente);
        model.addAttribute("datosEspecificos", cliente);
        model.addAttribute("templateEspecifico", "cliente/perfil :: cliente-perfil");

        // Asegurar que el layout renderice el fragmento del cliente
        model.addAttribute("mostrarCliente", true);

        // Añadir datos para el navbar/plantilla (categorías y servicios) por consistencia
        try {
            model.addAttribute("categorias", categoriaProductoService.obtenerTodos());
            model.addAttribute("categoriasServicios", categorizarServicios(servicioService.obtenerTodosLosServicios()));
        } catch (Exception ignored) {}

        // Si el cliente existe, asegurarnos de que el usuario en el modelo es el mismo objeto asociado
        if (cliente != null && cliente.getUsuario() != null) {
            model.addAttribute("usuario", cliente.getUsuario());
        }

        // Si no existe el cliente en BD, redirigir al usuario o mostrar mensaje
        if (cliente == null) {
            model.addAttribute("errorMessage", "No se encontró la información de cliente.");
            // Puedes cambiar la redirección si prefieres
            return "layouts/perfil-sin-navbar";
        }

        // Estadísticas simples
        Map<String, Object> estadisticas = new HashMap<>();
        int pedidos = 0;
        try {
            if (cliente != null) {
                pedidos = compraRepository.findByCliente(cliente).size();
            }
        } catch (Exception e) {
            pedidos = 0;
        }
        estadisticas.put("pedidosRealizados", pedidos);
        estadisticas.put("comentariosRealizados", 0);
        estadisticas.put("productosFavoritos", 0);

        model.addAttribute("estadisticas", estadisticas);

        // Usar layout sin navbar para la vista específica del cliente
        return "layouts/perfil-sin-navbar";
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
     * El carrito es un Map<Integer, Integer> donde la clave es el ID del producto y el valor es la cantidad
     */
    @PostMapping("/cliente/carrito/agregar")
    public String agregarAlCarrito(@RequestParam Integer idProducto,
                                   @RequestParam Integer cantidad,
                                   HttpSession session,
                                   RedirectAttributes redirectAttributes) {
        try {
            // Verificar que el producto existe
            Producto producto = productoService.obtenerPorId(idProducto).orElse(null);
            if (producto == null) {
                redirectAttributes.addFlashAttribute("error", "Producto no encontrado");
                return "redirect:/";
            }

            // Obtener o crear el carrito en sesión (Map<IdProducto, Cantidad>)
            @SuppressWarnings("unchecked")
            Map<Integer, Integer> carrito = (Map<Integer, Integer>) session.getAttribute("carrito");
            if (carrito == null) {
                carrito = new HashMap<>();
            }

            // Calcular la nueva cantidad
            int cantidadActual = carrito.getOrDefault(idProducto, 0);
            int nuevaCantidad = cantidadActual + cantidad;

            // Verificar stock disponible
            if (nuevaCantidad > producto.getStock()) {
                redirectAttributes.addFlashAttribute("error",
                    "No hay suficiente stock disponible. Stock actual: " + producto.getStock() +
                    ", en carrito: " + cantidadActual);
                return "redirect:/cliente/producto/" + idProducto;
            }

            // Agregar o actualizar el producto en el carrito
            carrito.put(idProducto, nuevaCantidad);

            // Guardar el carrito en la sesión
            session.setAttribute("carrito", carrito);

            redirectAttributes.addFlashAttribute("success",
                cantidad + " unidad(es) de " + producto.getNombreProducto() + " agregadas al carrito");

            return "redirect:/cliente/producto/" + idProducto;
        } catch (Exception e) {
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("error", "Error al agregar producto al carrito: " + e.getMessage());
            return "redirect:/cliente/producto/" + idProducto;
        }
    }

    /**
     * Ver carrito de compras
     */
    @GetMapping("/cliente/carrito")
    public String verCarrito(HttpSession session, Model model) {
        // Obtener el carrito desde la sesión (Map<IdProducto, Cantidad>)
        @SuppressWarnings("unchecked")
        Map<Integer, Integer> carrito = (Map<Integer, Integer>) session.getAttribute("carrito");

        if (carrito == null || carrito.isEmpty()) {
            model.addAttribute("cartItems", new ArrayList<>());
            model.addAttribute("subtotal", 0.0);
            model.addAttribute("envio", 0.0);
            model.addAttribute("total", 0.0);
            model.addAttribute("cartCount", 0);
            return "cliente/carrito";
        }

        // Convertir el Map a una lista de objetos para la vista
        List<Map<String, Object>> cartItems = new ArrayList<>();
        double subtotal = 0.0;

        for (Map.Entry<Integer, Integer> entry : carrito.entrySet()) {
            Integer idProducto = entry.getKey();
            Integer cantidad = entry.getValue();

            // Obtener el producto desde la base de datos
            Producto producto = productoService.obtenerPorId(idProducto).orElse(null);
            if (producto != null) {
                // Obtener URL de la imagen
                String imagenUrl = "/imag/placeholder.jpg";
                if (producto.getImagenesProducto() != null && !producto.getImagenesProducto().isEmpty()) {
                    imagenUrl = "/" + producto.getImagenesProducto().get(0).getUrlImagen();
                }

                // Calcular subtotal del item
                double precioProducto = producto.getPrecio().doubleValue();
                double subtotalItem = precioProducto * cantidad;
                subtotal += subtotalItem;

                // Crear objeto para la vista
                Map<String, Object> item = new HashMap<>();
                item.put("id", producto.getIdProducto());
                item.put("nombre", producto.getNombreProducto());
                item.put("descripcion", producto.getDescripcionProducto());
                item.put("precio", precioProducto);
                item.put("cantidad", cantidad);
                item.put("subtotal", subtotalItem);
                item.put("imagenUrl", imagenUrl);

                cartItems.add(item);
            }
        }

        // Calcular envío
        double envio = cartItems.isEmpty() ? 0.0 : 7000.0;

        // Calcular total
        double total = subtotal + envio;

        // Calcular cantidad total de items
        int cartCount = carrito.values().stream().mapToInt(Integer::intValue).sum();

        // Agregar atributos al modelo
        model.addAttribute("cartItems", cartItems);
        model.addAttribute("subtotal", subtotal);
        model.addAttribute("envio", envio);
        model.addAttribute("total", total);
        model.addAttribute("cartCount", cartCount);

        return "cliente/carrito";
    }


    /**
     * Actualizar cantidad de un producto en el carrito
     */
    @PostMapping("/cliente/carrito/actualizar")
    public String actualizarCarrito(@RequestParam Integer idProducto,
                                   @RequestParam Integer cantidad,
                                   HttpSession session,
                                   RedirectAttributes redirectAttributes) {
        try {
            @SuppressWarnings("unchecked")
            Map<Integer, Integer> carrito = (Map<Integer, Integer>) session.getAttribute("carrito");

            if (carrito == null || !carrito.containsKey(idProducto)) {
                redirectAttributes.addFlashAttribute("error", "Producto no encontrado en el carrito");
                return "redirect:/cliente/carrito";
            }

            // Verificar stock disponible
            Producto producto = productoService.obtenerPorId(idProducto).orElse(null);
            if (producto != null && cantidad > producto.getStock()) {
                redirectAttributes.addFlashAttribute("error",
                    "Stock insuficiente. Disponible: " + producto.getStock());
                return "redirect:/cliente/carrito";
            }

            // Actualizar cantidad
            carrito.put(idProducto, cantidad);
            session.setAttribute("carrito", carrito);

            redirectAttributes.addFlashAttribute("success", "Cantidad actualizada correctamente");
            return "redirect:/cliente/carrito";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error al actualizar el carrito");
            return "redirect:/cliente/carrito";
        }
    }

    /**
     * Quitar producto del carrito
     */
    @PostMapping("/cliente/carrito/quitar")
    public String quitarDelCarrito(@RequestParam Integer idProducto,
                                   HttpSession session,
                                   RedirectAttributes redirectAttributes) {
        try {
            @SuppressWarnings("unchecked")
            Map<Integer, Integer> carrito = (Map<Integer, Integer>) session.getAttribute("carrito");

            if (carrito == null) {
                redirectAttributes.addFlashAttribute("error", "El carrito está vacío");
                return "redirect:/cliente/carrito";
            }

            // Eliminar el producto del carrito
            carrito.remove(idProducto);
            session.setAttribute("carrito", carrito);

            redirectAttributes.addFlashAttribute("success", "Producto eliminado del carrito");
            return "redirect:/cliente/carrito";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error al eliminar producto del carrito");
            return "redirect:/cliente/carrito";
        }
    }

    /**
     * Vaciar carrito completamente
     */
    @PostMapping("/cliente/carrito/vaciar")
    public String vaciarCarrito(HttpSession session, RedirectAttributes redirectAttributes) {
        session.removeAttribute("carrito");
        redirectAttributes.addFlashAttribute("success", "Carrito vaciado correctamente");
        return "redirect:/cliente/carrito";
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

    /**
     * Ver pedidos del cliente autenticado
     */
    @GetMapping("/cliente/pedidos")
    public String verPedidosCliente(HttpSession session, Model model) {
        try {
            // Obtener el usuario autenticado
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication == null || !authentication.isAuthenticated() ||
                "anonymousUser".equals(authentication.getPrincipal())) {
                return "redirect:/login"; // Redirigir a la página de login si no está autenticado
            }

            String username = authentication.getName();
            Usuario usuario = usuarioRepository.findByNombreUsuario(username).orElse(null);
            if (usuario == null) {
                return "redirect:/login"; // Redirigir a la página de login si no se encuentra el usuario
            }

            // Obtener el cliente asociado al usuario
            Cliente cliente = clienteRepository.findByUsuario(usuario).orElse(null);
            if (cliente == null) {
                return "redirect:/login"; // Redirigir a la página de login si no se encuentra el cliente
            }

            // Obtener los pedidos del cliente
            List<com.example.springbootagrolink.model.Compra> pedidos = compraRepository.findByCliente(cliente);
            model.addAttribute("pedidos", pedidos);

        } catch (Exception e) {
            e.printStackTrace();
            model.addAttribute("error", "Error al cargar los pedidos: " + e.getMessage());
        }

        return "cliente/pedidos";
    }

}
