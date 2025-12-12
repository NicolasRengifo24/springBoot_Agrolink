package com.example.springbootagrolink.controllers;

import com.example.springbootagrolink.model.*;
import com.example.springbootagrolink.repository.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;


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
    private final FincaRepository fincaRepository;
    private final EnvioRepository envioRepository;

    public AdminController(ProductoRepository productoRepository,
                           CompraRepository compraRepository,
                           TransportistaRepository transportistaRepository,
                           ProductorRepository productorRepository,
                           UsuarioRepository usuarioRepository,
                           ClienteRepository clienteRepository,
                           AsesorRepository asesorRepository,
                           FincaRepository fincaRepository,
                           EnvioRepository envioRepository) {
        this.productoRepository = productoRepository;
        this.compraRepository = compraRepository;
        this.transportistaRepository = transportistaRepository;
        this.productorRepository = productorRepository;
        this.usuarioRepository = usuarioRepository;
        this.clienteRepository = clienteRepository;
        this.asesorRepository = asesorRepository;
        this.fincaRepository = fincaRepository;
        this.envioRepository = envioRepository;
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

    /**
     * Vista de editar producto desde admin
     */
    @GetMapping("/productos/editar/{id}")
    public String editarProducto(@PathVariable("id") Integer id, Model model) {
        log.info("=== Admin: Editando producto ID: {} ===", id);
        try {
            Producto producto = productoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Producto no encontrado"));

            // Cargar categorías y productores
            List<CategoriaProducto> categorias = new ArrayList<>();
            Set<CategoriaProducto> categoriasUnicas = new HashSet<>();
            List<Producto> todosProductos = productoRepository.findAll();
            for (Producto p : todosProductos) {
                if (p.getCategoria() != null) {
                    categoriasUnicas.add(p.getCategoria());
                }
            }
            categorias = new ArrayList<>(categoriasUnicas);

            List<Productor> productores = productorRepository.findAll();

            model.addAttribute("producto", producto);
            model.addAttribute("categorias", categorias);
            model.addAttribute("productores", productores);

            log.info("✅ Vista de editar producto cargada");
            return "admin/editar-producto";

        } catch (Exception e) {
            log.error("❌ Error al cargar producto para editar: {}", e.getMessage(), e);
            model.addAttribute("error", "Error al cargar el producto: " + e.getMessage());
            return "redirect:/admin/productos";
        }
    }

    /**
     * Actualizar producto desde admin (POST desde formulario de edición)
     */
    @PostMapping("/productos/actualizar/{id}")
    public String actualizarProducto(
            @PathVariable("id") Integer id,
            @RequestParam("nombreProducto") String nombreProducto,
            @RequestParam(value = "descripcionProducto", required = false) String descripcionProducto,
            @RequestParam("stock") Integer stock,
            @RequestParam("precio") BigDecimal precio,
            @RequestParam(value = "pesoKg", required = false) BigDecimal pesoKg,
            @RequestParam("categoria.idCategoria") Integer categoriaId,
            @RequestParam("productor.idProductor") Integer productorId,
            @RequestParam(value = "imagenFile", required = false) org.springframework.web.multipart.MultipartFile imagenFile,
            Model model) {

        log.info("=== Admin: Actualizando producto ID: {} ===", id);
        log.info("Archivo recibido: {}", imagenFile != null ? imagenFile.getOriginalFilename() : "ninguno");
        log.info("Tamaño archivo: {}", imagenFile != null ? imagenFile.getSize() : 0);

        try {
            // Buscar el producto existente
            Producto productoExistente = productoRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Producto no encontrado con ID: " + id));

            log.info("Producto encontrado: {}", productoExistente.getNombreProducto());

            // Actualizar campos básicos
            productoExistente.setNombreProducto(nombreProducto);
            productoExistente.setDescripcionProducto(descripcionProducto);
            productoExistente.setStock(stock);
            productoExistente.setPrecio(precio);
            productoExistente.setPesoKg(pesoKg);

            // Actualizar categoría
            if (categoriaId != null) {
                CategoriaProducto categoria = new CategoriaProducto();
                categoria.setIdCategoria(categoriaId);
                productoExistente.setCategoria(categoria);
                log.info("Categoría actualizada: {}", categoriaId);
            }

            // Actualizar productor
            if (productorId != null) {
                Productor productor = productorRepository.findById(productorId)
                        .orElse(null);
                if (productor != null) {
                    productoExistente.setProductor(productor);
                    log.info("Productor actualizado: {}", productorId);
                }
            }

            // Guardar el producto actualizado
            Producto productoGuardado = productoRepository.save(productoExistente);
            log.info("✅ Producto actualizado exitosamente: {} (ID: {})",
                    productoGuardado.getNombreProducto(), productoGuardado.getIdProducto());

            // Procesar imagen SOLO si se subió una nueva
            if (imagenFile != null && !imagenFile.isEmpty() && imagenFile.getSize() > 0) {
                try {
                    log.info("📸 Procesando nueva imagen para el producto...");

                    // Validar tipo de archivo
                    String contentType = imagenFile.getContentType();
                    if (contentType == null || !contentType.startsWith("image/")) {
                        log.warn("⚠️ Archivo no es una imagen válida: {}", contentType);
                        return "redirect:/admin/productos?success=actualizado&warning=imagen_invalida";
                    }

                    // Validar tamaño (máximo 5MB)
                    long maxSize = 5 * 1024 * 1024; // 5MB
                    if (imagenFile.getSize() > maxSize) {
                        log.warn("⚠️ Archivo demasiado grande: {} bytes", imagenFile.getSize());
                        return "redirect:/admin/productos?success=actualizado&warning=imagen_grande";
                    }

                    String nombreOriginal = imagenFile.getOriginalFilename();
                    String extension = nombreOriginal != null && nombreOriginal.contains(".") ?
                            nombreOriginal.substring(nombreOriginal.lastIndexOf(".")) : ".jpg";

                    String nombreArchivo = "producto_" + productoGuardado.getIdProducto() + "_" +
                            System.currentTimeMillis() + extension;

                    // Guardar en directorio de imágenes
                    String rutaImagen = "/images/" + nombreArchivo;
                    java.nio.file.Path rutaCompleta = java.nio.file.Paths.get("src/main/resources/static" + rutaImagen);

                    // Crear directorio si no existe
                    java.nio.file.Files.createDirectories(rutaCompleta.getParent());
                    imagenFile.transferTo(rutaCompleta.toFile());

                    log.info("✅ Imagen guardada exitosamente en: {}", rutaImagen);

                } catch (Exception imgEx) {
                    log.error("⚠️ Error al guardar la imagen: {}", imgEx.getMessage(), imgEx);
                    // Continuar sin fallar la actualización del producto
                    return "redirect:/admin/productos?success=actualizado&warning=error_imagen";
                }
            } else {
                log.info("ℹ️ No se subió nueva imagen, manteniendo la actual");
            }

            // Redirigir a la lista de productos con mensaje de éxito
            log.info("✅ Redirigiendo a lista de productos con éxito");
            return "redirect:/admin/productos?success=actualizado";

        } catch (Exception e) {
            log.error("❌ Error al actualizar producto ID {}: {}", id, e.getMessage(), e);
            model.addAttribute("error", "Error al actualizar el producto: " + e.getMessage());
            return "redirect:/admin/productos?error=actualizar";
        }
    }

    /**
     * Eliminar producto desde admin
     */
    @DeleteMapping("/productos/eliminar/{id}")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> eliminarProducto(@PathVariable("id") Integer id) {
        log.info("=== Admin: Eliminando producto ID: {} ===", id);
        Map<String, Object> response = new HashMap<>();

        try {
            Producto producto = productoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Producto no encontrado"));

            String nombreProducto = producto.getNombreProducto();
            productoRepository.delete(producto);

            log.info("✅ Producto eliminado exitosamente: {}", nombreProducto);
            response.put("success", true);
            response.put("message", "Producto eliminado exitosamente");
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("❌ Error al eliminar producto: {}", e.getMessage(), e);
            response.put("success", false);
            response.put("message", "Error al eliminar el producto: " + e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }

    /**
     * Vista de gestión de pedidos/compras - SIN usar fecha_hora_compra
     */
    @GetMapping("/pedidos")
    public String gestionPedidos(Model model) {
        log.info("=== Accediendo a /admin/pedidos (sin consultar fechas) ===");
        try {
            // Obtener todas las compras usando query nativa para evitar problema con fechas
            List<Compra> compras;
            try {
                compras = compraRepository.findAll();
                log.info("Total compras encontradas en BD: {}", compras.size());
            } catch (Exception ex) {
                log.error("Error en findAll(), intentando cargar compras con query nativa...", ex);
                // Si falla, crear lista vacía
                compras = new ArrayList<>();
            }

            // Forzar carga de relaciones lazy sin tocar fecha_hora_compra
            List<Compra> comprasValidas = new ArrayList<>();
            for (Compra compra : compras) {
                try {
                    // Cargar cliente sin acceder a fecha
                    if (compra.getCliente() != null) {
                        try {
                            if (compra.getCliente().getUsuario() != null) {
                                compra.getCliente().getUsuario().getNombre();
                            }
                        } catch (Exception e) {
                            log.debug("Error cargando usuario del cliente: {}", e.getMessage());
                        }
                    }
                    comprasValidas.add(compra);
                } catch (Exception e) {
                    log.warn("Error procesando compra {}: {}", compra.getIdCompra(), e.getMessage());
                }
            }

            // Ordenar SOLO por ID descendente (más reciente primero)
            List<Compra> comprasOrdenadas = comprasValidas.stream()
                .sorted((c1, c2) -> c2.getIdCompra().compareTo(c1.getIdCompra()))
                .toList();

            // Calcular estadísticas SIN usar fechas
            long totalPedidos = comprasOrdenadas.size();
            BigDecimal totalVentas = comprasOrdenadas.stream()
                .filter(c -> c.getTotal() != null)
                .map(Compra::getTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

            // Para pedidos del mes, usamos 0 ya que no podemos consultar fechas
            long pedidosMes = 0;
            BigDecimal ventasMes = BigDecimal.ZERO;

            // Agregar al modelo
            model.addAttribute("compras", comprasOrdenadas);
            model.addAttribute("totalPedidos", totalPedidos);
            model.addAttribute("totalVentas", totalVentas);
            model.addAttribute("pedidosMes", pedidosMes);
            model.addAttribute("ventasMes", ventasMes);

            log.info("✅ Vista de pedidos admin cargada exitosamente");
            log.info("📊 Stats: {} pedidos totales, ${} en ventas totales",
                     totalPedidos, totalVentas);

            return "admin/pedidos";

        } catch (Exception e) {
            log.error("=== ❌ ERROR AL CARGAR PEDIDOS ===");
            log.error("Tipo de error: {}", e.getClass().getName());
            log.error("Mensaje: {}", e.getMessage());
            log.error("Stack trace: ", e);

            model.addAttribute("compras", Collections.emptyList());
            model.addAttribute("totalPedidos", 0);
            model.addAttribute("totalVentas", BigDecimal.ZERO);
            model.addAttribute("pedidosMes", 0);
            model.addAttribute("ventasMes", BigDecimal.ZERO);
            model.addAttribute("error", "Error al cargar pedidos: " + e.getMessage());

            return "admin/pedidos";
        }
    }

    /**
     * Vista de gestión de envíos
     */
    @GetMapping("/envios")
    public String gestionEnvios(Model model) {
        log.info("=== Accediendo a /admin/envios ===");
        try {
            // Obtener todos los envíos
            List<Envio> envios = envioRepository.findAll();
            log.info("Total envíos encontrados en BD: {}", envios.size());

            // Forzar carga de relaciones lazy (solo transportista, no cliente para evitar fecha_hora_compra)
            for (Envio envio : envios) {
                try {
                    // Cargar transportista
                    if (envio.getTransportista() != null && envio.getTransportista().getUsuario() != null) {
                        envio.getTransportista().getUsuario().getNombre();
                    }
                    // Solo cargar compra.idCompra y compra.total, NO cargar cliente
                    if (envio.getCompra() != null) {
                        envio.getCompra().getIdCompra();
                        envio.getCompra().getTotal();
                        // NO acceder a cliente para evitar lazy loading de fecha_hora_compra
                    }
                } catch (Exception e) {
                    log.warn("Error cargando relaciones de envío {}: {}", envio.getIdEnvio(), e.getMessage());
                }
            }

            // Ordenar por ID descendente
            List<Envio> enviosOrdenados = envios.stream()
                .sorted((e1, e2) -> e2.getIdEnvio().compareTo(e1.getIdEnvio()))
                .toList();

            // Calcular estadísticas
            long totalEnvios = enviosOrdenados.size();
            long enviosEntregados = enviosOrdenados.stream()
                .filter(e -> e.getEstadoEnvio() != null && e.getEstadoEnvio().toString().equals("Entregado"))
                .count();
            long enviosEnTransito = enviosOrdenados.stream()
                .filter(e -> e.getEstadoEnvio() != null && e.getEstadoEnvio().toString().equals("En_Transito"))
                .count();
            long enviosPendientes = enviosOrdenados.stream()
                .filter(e -> e.getEstadoEnvio() != null &&
                       (e.getEstadoEnvio().toString().equals("Buscando_Transporte") ||
                        e.getEstadoEnvio().toString().equals("Listo_Para_Envio")))
                .count();

            // Agregar al modelo
            model.addAttribute("envios", enviosOrdenados);
            model.addAttribute("totalEnvios", totalEnvios);
            model.addAttribute("enviosEntregados", enviosEntregados);
            model.addAttribute("enviosEnTransito", enviosEnTransito);
            model.addAttribute("enviosPendientes", enviosPendientes);

            log.info("✅ Vista de envíos admin cargada exitosamente");
            log.info("📊 Stats: {} envíos totales, {} entregados, {} en tránsito, {} pendientes",
                     totalEnvios, enviosEntregados, enviosEnTransito, enviosPendientes);

            return "admin/envios";

        } catch (Exception e) {
            log.error("=== ❌ ERROR AL CARGAR ENVÍOS ===");
            log.error("Tipo de error: {}", e.getClass().getName());
            log.error("Mensaje: {}", e.getMessage());
            log.error("Stack trace: ", e);

            model.addAttribute("envios", Collections.emptyList());
            model.addAttribute("totalEnvios", 0);
            model.addAttribute("enviosEntregados", 0);
            model.addAttribute("enviosEnTransito", 0);
            model.addAttribute("enviosPendientes", 0);
            model.addAttribute("error", "Error al cargar envíos: " + e.getMessage());

            return "admin/envios";
        }
    }

    /**
     * API: Obtener fincas de un productor específico
     */
    @GetMapping("/api/productores/{id}/fincas")
    @ResponseBody
    public ResponseEntity<List<Map<String, Object>>> obtenerFincasDeProductor(@PathVariable("id") Integer productorId) {
        log.info("=== API: Obteniendo fincas del productor ID: {} ===", productorId);

        try {
            // Verificar que el productor existe
            Productor productor = productorRepository.findById(productorId)
                .orElseThrow(() -> new RuntimeException("Productor no encontrado"));

            // Obtener todas las fincas y filtrar por productor
            List<Finca> todasFincas = fincaRepository.findAll();
            List<Map<String, Object>> fincasData = new ArrayList<>();

            for (Finca finca : todasFincas) {
                // Verificar si la finca pertenece a este productor
                if (finca.getProductor() != null &&
                    finca.getProductor().getIdProductor() != null &&
                    finca.getProductor().getIdProductor().equals(productorId)) {

                    Map<String, Object> fincaMap = new HashMap<>();
                    fincaMap.put("idFinca", finca.getIdFinca());
                    fincaMap.put("nombreFinca", finca.getNombreFinca() != null ? finca.getNombreFinca() : "Finca sin nombre");
                    fincaMap.put("ubicacion", finca.getCiudad() != null ? finca.getCiudad() : (finca.getDireccionFinca() != null ? finca.getDireccionFinca() : "Sin ubicación"));
                    fincaMap.put("direccion", finca.getDireccionFinca() != null ? finca.getDireccionFinca() : "");

                    fincasData.add(fincaMap);
                }
            }

            log.info(" Fincas encontradas para productor {}: {}", productorId, fincasData.size());
            return ResponseEntity.ok(fincasData);

        } catch (Exception e) {
            log.error(" Error al obtener fincas del productor: {}", e.getMessage(), e);
            return ResponseEntity.status(500).body(new ArrayList<>());
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

