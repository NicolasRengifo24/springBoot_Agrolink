package com.example.springbootagrolink.controllers;

import com.example.springbootagrolink.model.*;
import com.example.springbootagrolink.repository.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

@Controller
@RequestMapping("/admin")
public class AdminController {

    private static final Logger log = LoggerFactory.getLogger(AdminController.class);

    private final ProductoRepository productoRepository;
    private final CompraRepository compraRepository;
    private final TransportistaRepository transportistaRepository;
    private final ProductorRepository productorRepository;
    private final UsuarioRepository usuarioRepository;
    private final ClienteRepository clienteRepository;
    private final AsesorRepository asesorRepository;

    public AdminController(ProductoRepository productoRepository,
                           CompraRepository compraRepository,
                           TransportistaRepository transportistaRepository,
                           ProductorRepository productorRepository,
                           UsuarioRepository usuarioRepository,
                           ClienteRepository clienteRepository,
                           AsesorRepository asesorRepository) {
        this.productoRepository = productoRepository;
        this.compraRepository = compraRepository;
        this.transportistaRepository = transportistaRepository;
        this.productorRepository = productorRepository;
        this.usuarioRepository = usuarioRepository;
        this.clienteRepository = clienteRepository;
        this.asesorRepository = asesorRepository;
    }

    /**
     * Dashboard principal del administrador - Vista premium
     * Ruta: /admin
     */
    @GetMapping
    public String dashboard(Model model) {
        try {
            // Estadísticas generales
            long totalPedidos = compraRepository.count();
            long totalProductos = productoRepository.count();
            long totalProductores = productorRepository.count();
            long totalTransportistas = transportistaRepository.count();

            // Calcular inventario total (suma de stock de todos los productos)
            List<Producto> productos = productoRepository.findAll();
            BigDecimal inventarioTotal = productos.stream()
                .map(p -> BigDecimal.valueOf(p.getStock() != null ? p.getStock() : 0))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

            // Calcular ventas del mes
            BigDecimal ventasMes = calcularVentasMes();

            // Productos con stock bajo (menos de 300 unidades)
            long productosStockBajo = productos.stream()
                .filter(p -> p.getStock() != null && p.getStock() < 300)
                .count();

            // Agregar al modelo
            model.addAttribute("totalPedidos", totalPedidos);
            model.addAttribute("inventarioTotal", inventarioTotal);
            model.addAttribute("ventasMes", ventasMes);
            model.addAttribute("totalProductos", totalProductos);
            model.addAttribute("productosStockBajo", productosStockBajo);
            model.addAttribute("totalProductores", totalProductores);
            model.addAttribute("totalTransportistas", totalTransportistas);

            // Agregar datos de usuarios para la vista integrada
            List<Cliente> clientes = clienteRepository.findAll();
            List<Productor> productores = productorRepository.findAll();
            List<Transportista> transportistas = transportistaRepository.findAll();
            List<Asesor> asesores = asesorRepository.findAll();

            model.addAttribute("clientes", clientes);
            model.addAttribute("productores", productores);
            model.addAttribute("transportistas", transportistas);
            model.addAttribute("asesores", asesores);

            model.addAttribute("totalClientes", clientes.size());
            model.addAttribute("totalProductoresUsuarios", productores.size());
            model.addAttribute("totalTransportistasUsuarios", transportistas.size());
            model.addAttribute("totalAsesores", asesores.size());

            // Agregar productos y categorías para la vista integrada de productos
            model.addAttribute("productos", productos);

            // Obtener categorías (si tienes un repositorio de categorías)
            List<CategoriaProducto> categorias = new ArrayList<>();
            try {
                // Si tienes CategoriaProductoRepository, úsalo aquí
                // categorias = categoriaProductoRepository.findAll();
                // Por ahora, extraemos las categorías únicas de los productos
                Set<CategoriaProducto> categoriasUnicas = new HashSet<>();
                for (Producto p : productos) {
                    if (p.getCategoria() != null) {
                        categoriasUnicas.add(p.getCategoria());
                    }
                }
                categorias = new ArrayList<>(categoriasUnicas);
            } catch (Exception ex) {
                log.warn("No se pudieron cargar las categorías: {}", ex.getMessage());
            }
            model.addAttribute("categorias", categorias);

            log.info("Dashboard admin cargado - Pedidos: {}, Inventario: {} kg, Productos: {}",
                totalPedidos, inventarioTotal, productos.size());

            return "admin/admin";

        } catch (Exception e) {
            log.error("Error al cargar dashboard admin: {}", e.getMessage(), e);
            model.addAttribute("error", "Error al cargar estadísticas");
            return "admin/admin";
        }
    }

    /**
     * Obtener datos para el gráfico de ventas semanales
     */
    @GetMapping("/ventas-semanales")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> obtenerVentasSemanales() {
        try {
            // Últimos 5 días (Lun-Vie)
            List<String> dias = Arrays.asList("Lun", "Mar", "Mié", "Jue", "Vie");

            // TODO: Calcular ventas reales desde compraRepository
            // Por ahora usamos datos de ejemplo para demostración
            List<BigDecimal> ventas = Arrays.asList(
                BigDecimal.valueOf(12000),
                BigDecimal.valueOf(22000),
                BigDecimal.valueOf(18000),
                BigDecimal.valueOf(32000),
                BigDecimal.valueOf(45000)
            );

            Map<String, Object> resultado = new HashMap<>();
            resultado.put("labels", dias);
            resultado.put("data", ventas);

            return ResponseEntity.ok(resultado);

        } catch (Exception e) {
            log.error("Error al obtener ventas semanales: {}", e.getMessage(), e);
            return ResponseEntity.status(500).body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Obtener productos con stock crítico
     */
    @GetMapping("/productos-criticos")
    @ResponseBody
    public ResponseEntity<List<Map<String, Object>>> obtenerProductosCriticos() {
        try {
            List<Producto> productos = productoRepository.findAll();
            List<Map<String, Object>> productosCriticos = new ArrayList<>();

            for (Producto p : productos) {
                if (p.getStock() != null && p.getStock() < 500) {
                    Map<String, Object> productoMap = new HashMap<>();
                    productoMap.put("idProducto", p.getIdProducto());
                    productoMap.put("nombreProducto", p.getNombreProducto());
                    productoMap.put("stock", p.getStock());
                    productoMap.put("precio", p.getPrecio());
                    productoMap.put("productor", p.getProductor() != null && p.getProductor().getUsuario() != null ?
                        p.getProductor().getUsuario().getNombreUsuario() : "Sin productor");
                    productoMap.put("estado", p.getStock() < 300 ? "Bajo Stock" : "Disponible");

                    productosCriticos.add(productoMap);
                }
            }

            return ResponseEntity.ok(productosCriticos);

        } catch (Exception e) {
            log.error("Error al obtener productos críticos: {}", e.getMessage(), e);
            return ResponseEntity.status(500).body(new ArrayList<>());
        }
    }

    /**
     * Obtener pedidos recientes
     */
    @GetMapping("/pedidos-recientes")
    @ResponseBody
    public ResponseEntity<List<Map<String, Object>>> obtenerPedidosRecientes() {
        try {
            List<Compra> compras = compraRepository.findAll();
            List<Map<String, Object>> pedidosRecientes = new ArrayList<>();

            // Ordenar por fecha descendente y tomar los últimos 5
            compras.stream()
                .filter(c -> c.getFechaHoraCompra() != null)
                .sorted((c1, c2) -> c2.getFechaHoraCompra().compareTo(c1.getFechaHoraCompra()))
                .limit(5)
                .forEach(c -> {
                    Map<String, Object> pedidoMap = new HashMap<>();
                    pedidoMap.put("idCompra", c.getIdCompra());
                    pedidoMap.put("cliente", c.getCliente() != null && c.getCliente().getUsuario() != null ?
                        c.getCliente().getUsuario().getNombre() : "Cliente");
                    pedidoMap.put("total", c.getTotal());
                    pedidoMap.put("fecha", c.getFechaHoraCompra());
                    pedidoMap.put("metodoPago", c.getMetodoPago());

                    pedidosRecientes.add(pedidoMap);
                });

            return ResponseEntity.ok(pedidosRecientes);

        } catch (Exception e) {
            log.error("Error al obtener pedidos recientes: {}", e.getMessage(), e);
            return ResponseEntity.status(500).body(new ArrayList<>());
        }
    }


    /**
     * Vista de gestión de usuarios con tabs para cada rol
     */
    @GetMapping("/usuarios")
    public String gestionUsuarios(Model model) {
        log.info("=== Accediendo a /admin/usuarios ===");
        try {
            // Obtener todos los usuarios por rol con protección contra null
            log.info("Cargando usuarios de la base de datos...");

            List<Usuario> todosUsuarios = Optional.ofNullable(usuarioRepository.findAll())
                .orElseGet(Collections::emptyList);
            log.info("Total usuarios en BD: {}", todosUsuarios.size());

            List<Cliente> clientes = Optional.ofNullable(clienteRepository.findAll())
                .orElseGet(Collections::emptyList);
            log.info("Total clientes: {}", clientes.size());

            List<Productor> productores = Optional.ofNullable(productorRepository.findAll())
                .orElseGet(Collections::emptyList);
            log.info("Total productores: {}", productores.size());

            List<Transportista> transportistas = Optional.ofNullable(transportistaRepository.findAll())
                .orElseGet(Collections::emptyList);
            log.info("Total transportistas: {}", transportistas.size());

            List<Asesor> asesores = Optional.ofNullable(asesorRepository.findAll())
                .orElseGet(Collections::emptyList);
            log.info("Total asesores: {}", asesores.size());

            // Agregar al modelo con listas garantizadas no nulas
            model.addAttribute("todosUsuarios", todosUsuarios);
            model.addAttribute("clientes", clientes);
            model.addAttribute("productores", productores);
            model.addAttribute("transportistas", transportistas);
            model.addAttribute("asesores", asesores);

            model.addAttribute("totalUsuarios", todosUsuarios.size());
            model.addAttribute("totalClientes", clientes.size());
            model.addAttribute("totalProductores", productores.size());
            model.addAttribute("totalTransportistas", transportistas.size());
            model.addAttribute("totalAsesores", asesores.size());

            log.info("✅ Vista admin/usuarios cargada exitosamente");
            log.info("📊 Resumen: {} clientes, {} productores, {} transportistas, {} asesores",
                     clientes.size(), productores.size(), transportistas.size(), asesores.size());

            return "admin/usuarios";

        } catch (Exception e) {
            log.error("=== ❌ ERROR AL CARGAR USUARIOS ===");
            log.error("Tipo de error: {}", e.getClass().getName());
            log.error("Mensaje: {}", e.getMessage());
            log.error("Stack trace completo: ", e);

            // En caso de error, enviar listas vacías para evitar errores en la vista
            model.addAttribute("todosUsuarios", Collections.emptyList());
            model.addAttribute("clientes", Collections.emptyList());
            model.addAttribute("productores", Collections.emptyList());
            model.addAttribute("transportistas", Collections.emptyList());
            model.addAttribute("asesores", Collections.emptyList());
            model.addAttribute("totalUsuarios", 0);
            model.addAttribute("totalClientes", 0);
            model.addAttribute("totalProductores", 0);
            model.addAttribute("totalTransportistas", 0);
            model.addAttribute("totalAsesores", 0);
            model.addAttribute("error", "Error al cargar usuarios: " + e.getMessage());

            return "admin/usuarios";
        }
    }

    /**
     * Endpoint JSON para obtener usuarios por rol
     */
    @GetMapping("/usuarios/por-rol/{rol}")
    @ResponseBody
    public ResponseEntity<List<Map<String, Object>>> obtenerUsuariosPorRol(@PathVariable String rol) {
        try {
            List<Usuario> usuarios = usuarioRepository.findAll().stream()
                .filter(u -> u.getRol() != null && u.getRol().name().equals(rol))
                .toList();

            List<Map<String, Object>> usuariosData = new ArrayList<>();

            for (Usuario u : usuarios) {
                Map<String, Object> userData = new HashMap<>();
                userData.put("idUsuario", u.getIdUsuario());
                userData.put("nombre", u.getNombre());
                userData.put("apellido", u.getApellido());
                userData.put("correo", u.getCorreo());
                userData.put("nombreUsuario", u.getNombreUsuario());
                userData.put("ciudad", u.getCiudad());
                userData.put("departamento", u.getDepartamento());
                userData.put("telefono", u.getTelefono());
                userData.put("rol", u.getRol() != null ? u.getRol().name() : "");

                usuariosData.add(userData);
            }

            return ResponseEntity.ok(usuariosData);
        } catch (Exception e) {
            log.error("Error al obtener usuarios por rol: {}", e.getMessage(), e);
            return ResponseEntity.status(500).body(new ArrayList<>());
        }
    }

    /**
     * Vista de gestión de productos
     */
    @GetMapping("/productos")
    public String gestionProductos(Model model) {
        log.info("=== Accediendo a /admin/productos ===");
        try {
            // Obtener todos los productos
            List<Producto> productos = productoRepository.findAll();
            log.info("Total productos encontrados: {}", productos.size());

            // Inicializar imágenes para evitar lazy loading issues
            for (Producto p : productos) {
                if (p.getImagenesProducto() != null) {
                    p.getImagenesProducto().size(); // Forzar carga de imágenes
                    log.debug("Producto: {} - Imágenes: {}", p.getNombreProducto(), p.getImagenesProducto().size());
                }
                if (p.getProductor() != null && p.getProductor().getUsuario() != null) {
                    p.getProductor().getUsuario().getNombre(); // Forzar carga de productor
                }
            }

            // Datos para la vista
            model.addAttribute("productos", productos);

            // Obtener categorías únicas
            Set<CategoriaProducto> categoriasUnicas = new HashSet<>();
            for (Producto p : productos) {
                if (p.getCategoria() != null) {
                    categoriasUnicas.add(p.getCategoria());
                }
            }
            model.addAttribute("categorias", new ArrayList<>(categoriasUnicas));
            log.info("Total categorías únicas: {}", categoriasUnicas.size());

            // Stats para KPIs
            long bajoStock = productos.stream().filter(p -> p.getStock() != null && p.getStock() < 10).count();
            long disponibles = productos.stream().filter(p -> p.getStock() != null && p.getStock() >= 10).count();
            long totalProductores = productorRepository.count();

            model.addAttribute("totalProductos", productos.size());
            model.addAttribute("productosBajoStock", bajoStock);
            model.addAttribute("productosDisponibles", disponibles);
            model.addAttribute("totalProductores", totalProductores);

            log.info("✅ Vista de productos admin cargada exitosamente");
            log.info("📊 Stats: {} productos, {} disponibles, {} bajo stock, {} productores",
                     productos.size(), disponibles, bajoStock, totalProductores);

            return "admin/productos";

        } catch (Exception e) {
            log.error("=== ❌ ERROR AL CARGAR PRODUCTOS ===");
            log.error("Tipo de error: {}", e.getClass().getName());
            log.error("Mensaje: {}", e.getMessage());
            log.error("Stack trace: ", e);

            model.addAttribute("productos", Collections.emptyList());
            model.addAttribute("categorias", Collections.emptyList());
            model.addAttribute("totalProductos", 0);
            model.addAttribute("productosBajoStock", 0);
            model.addAttribute("productosDisponibles", 0);
            model.addAttribute("totalProductores", 0);
            model.addAttribute("error", "Error al cargar productos: " + e.getMessage());

            return "admin/productos";
        }
    }

    // ==================== MÉTODOS AUXILIARES ====================

    private BigDecimal calcularVentasMes() {
        try {
            List<Compra> compras = compraRepository.findAll();
            LocalDateTime inicioMes = LocalDateTime.now().withDayOfMonth(1).withHour(0).withMinute(0).withSecond(0);

            return compras.stream()
                .filter(c -> c.getFechaHoraCompra() != null && c.getFechaHoraCompra().isAfter(inicioMes))
                .map(Compra::getTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        } catch (Exception e) {
            log.error("Error al calcular ventas del mes: {}", e.getMessage());
            return BigDecimal.valueOf(248000000); // Valor por defecto
        }
    }
}

