package com.example.springbootagrolink.controllers;

import com.example.springbootagrolink.model.*;
import com.example.springbootagrolink.services.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.TextStyle;
import java.util.*;

@Controller
@RequestMapping("/productos")
public class ProductoController {

    private static final Logger log = LoggerFactory.getLogger(ProductoController.class);

    @Autowired
    private ProductoService productoService;

    @Autowired
    private ProductorService productorService;

    @Autowired
    private CategoriaProductoService categoriaProductoService;

    @Autowired
    private ImagenProductoService imagenProductoService;

    @Autowired
    private CompraService compraService;

    @Autowired
    private FincaService fincaService;

    @Autowired
    private ProductoFincaService productoFincaService;

    // Directorio para guardar las imágenes (usar ruta absoluta que funcione en desarrollo y producción)
    private static final String UPLOAD_DIR = "target/classes/static/images/products/";

    /**
     * Mostrar dashboard de productor con lista de productos
     */
    @GetMapping
    public String listarProductos(Model model) {
        List<Producto> productos = productoService.obtenerTodos();
        List<CategoriaProducto> categorias = categoriaProductoService.obtenerTodos();
        List<Productor> productores = productorService.obtenerTodos();
        List<Finca> fincas = fincaService.obtenerTodos();

        model.addAttribute("productos", productos);
        model.addAttribute("categorias", categorias);
        model.addAttribute("productores", productores);
        model.addAttribute("fincas", fincas);
        model.addAttribute("ubicacion", "");
        model.addAttribute("categoriaId", 0);

        return "productos/dashboard";
    }

    /**
     * Alias explícito para el dashboard (soporta /productos/dashboard)
     */
    @GetMapping("/dashboard")
    public String listarProductosDashboard(Model model) {
        // Reusar la lógica existente
        return listarProductos(model);
    }

    /**
     * Buscar productos por ubicación y categoría
     */
    @GetMapping("/buscar")
    public String buscarProductos(@RequestParam(value = "ubicacion", defaultValue = "") String ubicacion,
                                 @RequestParam(value = "categoriaId", defaultValue = "0") Integer categoriaId,
                                 Model model) {
        List<Producto> productos;

        if (ubicacion.isEmpty() && categoriaId == 0) {
            productos = productoService.obtenerTodos();
        } else {
            productos = productoService.buscarPorUbicacionYCategoria(ubicacion, categoriaId);
        }

        List<CategoriaProducto> categorias = categoriaProductoService.obtenerTodos();
        List<Productor> productores = productorService.obtenerTodos();

        model.addAttribute("productos", productos);
        model.addAttribute("categorias", categorias);
        model.addAttribute("productores", productores);
        model.addAttribute("ubicacion", ubicacion);
        model.addAttribute("categoriaId", categoriaId);

        return "productos/dashboard";
    }

    /**
     * Obtener datos de ventas de los últimos 6 meses para la gráfica
     */
    @GetMapping("/ventas-mensuales")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> obtenerVentasMensuales() {
        try {
            List<Compra> todasLasCompras = compraService.obtenerTodas();
            LocalDateTime ahora = LocalDateTime.now();

            // Crear mapa para últimos 6 meses
            Map<String, BigDecimal> ventasPorMes = new LinkedHashMap<>();
            List<String> meses = new ArrayList<>();

            Locale localeES = Locale.of("es", "ES");

            for (int i = 5; i >= 0; i--) {
                LocalDateTime mesActual = ahora.minusMonths(i);
                String nombreMes = mesActual.getMonth().getDisplayName(TextStyle.SHORT, localeES);
                meses.add(nombreMes.substring(0, 1).toUpperCase() + nombreMes.substring(1, 3));
                ventasPorMes.put(nombreMes, BigDecimal.ZERO);
            }

            // Agrupar ventas por mes
            for (Compra compra : todasLasCompras) {
                // Validar que fechaHoraCompra no sea NULL
                if (compra.getFechaHoraCompra() != null &&
                    compra.getFechaHoraCompra().isAfter(ahora.minusMonths(6))) {
                    String mes = compra.getFechaHoraCompra().getMonth()
                            .getDisplayName(TextStyle.SHORT, localeES);
                    ventasPorMes.merge(mes, compra.getTotal(), BigDecimal::add);
                }
            }

            // Convertir a lista de valores
            List<BigDecimal> ventas = new ArrayList<>(ventasPorMes.values());

            Map<String, Object> resultado = new HashMap<>();
            resultado.put("meses", meses);
            resultado.put("ventas", ventas);

            return ResponseEntity.ok(resultado);
        } catch (Exception e) {
            log.error("Error al obtener ventas mensuales: {}", e.getMessage(), e);
            // Retornar datos por defecto
            Map<String, Object> resultado = new HashMap<>();
            resultado.put("meses", Arrays.asList("Jun", "Jul", "Ago", "Sep", "Oct", "Nov"));
            resultado.put("ventas", Arrays.asList(12000, 22000, 18000, 26000, 30000, 42000));
            return ResponseEntity.ok(resultado);
        }
    }

    /**
     * Guardar un nuevo producto
     */
    @PostMapping("/guardar")
    public String guardarProducto(@ModelAttribute Producto producto,
                                  @RequestParam("productorId") Integer productorId,
                                  @RequestParam("categoriaId") Integer categoriaId,
                                  @RequestParam(value = "fincaIds", required = false) List<Integer> fincaIds,
                                  @RequestParam(value = "imagenFile", required = false) MultipartFile imagenFile,
                                  @RequestParam(value = "esPrincipal", defaultValue = "false") boolean esPrincipal,
                                  RedirectAttributes redirectAttributes) {
         try {
            // Validar y asignar el productor
            Optional<Productor> productorOpt = productorService.obtenerPorId(productorId);
            if (productorOpt.isEmpty()) {
                redirectAttributes.addFlashAttribute("error", "Productor no encontrado");
                return "redirect:/productos/crear";
            }
            producto.setProductor(productorOpt.get());

            // Validar y asignar la categoría
            Optional<CategoriaProducto> categoriaOpt = categoriaProductoService.obtenerPorId(categoriaId);
            if (categoriaOpt.isEmpty()) {
                redirectAttributes.addFlashAttribute("error", "Categoría no encontrada");
                return "redirect:/productos/crear";
            }
            producto.setCategoria(categoriaOpt.get());

            // Establecer valores por defecto si no se proporcionan
            if (producto.getStock() == null) {
                producto.setStock(0);
            }
            if (producto.getPesoKg() == null) {
                producto.setPesoKg(BigDecimal.valueOf(1.00));
            }

            // Guardar el producto
            Producto productoGuardado = productoService.guardar(producto);

            // Asociar producto con finca(s) si se proporcionaron
            if (fincaIds != null && !fincaIds.isEmpty()) {
                for (Integer fincaId : fincaIds) {
                    Optional<Finca> fincaOpt = fincaService.obtenerPorId(fincaId);
                    if (fincaOpt.isPresent()) {
                        // Crear la asociación ProductoFinca
                        ProductoFinca productoFinca = new ProductoFinca();
                        productoFinca.setProducto(productoGuardado);
                        productoFinca.setFinca(fincaOpt.get());
                        // Valores por defecto para campos opcionales
                        productoFinca.setCantidadProduccion(BigDecimal.ZERO);
                        productoFinca.setFechaCosecha(null);

                        productoFincaService.guardar(productoFinca);
                        log.info("Producto {} asociado a finca {}", productoGuardado.getIdProducto(), fincaId);
                    }
                }
            }

            // Procesar imagen si se proporciona
            if (imagenFile != null && !imagenFile.isEmpty()) {
                try {
                    log.info("Procesando imagen: {} - Tamaño: {} bytes",
                        imagenFile.getOriginalFilename(), imagenFile.getSize());
                    guardarImagenProducto(imagenFile, productoGuardado, esPrincipal);
                    log.info("Imagen guardada exitosamente para producto {}", productoGuardado.getIdProducto());
                } catch (Exception imgEx) {
                    log.error("Error al guardar imagen, pero producto creado: {}", imgEx.getMessage(), imgEx);
                    redirectAttributes.addFlashAttribute("warning",
                        "Producto creado pero hubo un error al guardar la imagen: " + imgEx.getMessage());
                    return "redirect:/productos";
                }
            }

            log.info("Producto creado exitosamente con ID: {}", productoGuardado.getIdProducto());
            redirectAttributes.addFlashAttribute("success", "created");
            return "redirect:/productos";

        } catch (Exception e) {
            log.error("Error al crear producto: {}", e.getMessage(), e);
            redirectAttributes.addFlashAttribute("error", "Error al crear el producto: " + e.getMessage());
            return "redirect:/productos/crear";
        }
    }

    /**
     * Ver detalles de un producto específico
     */
    @GetMapping("/{id:\\d+}")
    public String verDetalleProducto(@PathVariable Integer id, Model model) {
        Optional<Producto> productoOpt = productoService.obtenerPorId(id);
        if (productoOpt.isEmpty()) {
            return "redirect:/productos";
        }

        model.addAttribute("producto", productoOpt.get());
        return "productos/detalle";
    }




    /**
     * Actualizar un producto existente
     */
    @PostMapping("/actualizar/{id:\\d+}")
    public String actualizarProducto(@PathVariable Integer id,
                                   @ModelAttribute Producto producto,
                                   @RequestParam(value = "productorId", required = false) Integer productorId,
                                   @RequestParam(value = "categoriaId", required = false) Integer categoriaId,
                                   @RequestParam(value = "fincaIds", required = false) List<Integer> fincaIds,
                                   @RequestParam(value = "imagenFile", required = false) MultipartFile imagenFile,
                                   @RequestParam(value = "esPrincipal", defaultValue = "false") boolean esPrincipal,
                                   RedirectAttributes redirectAttributes) {
        try {
            // Verificar que el producto existe
            Optional<Producto> productoExistenteOpt = productoService.obtenerPorId(id);
            if (productoExistenteOpt.isEmpty()) {
                redirectAttributes.addFlashAttribute("error", "Producto no encontrado");
                return "redirect:/productos";
            }

            Producto productoExistente = productoExistenteOpt.get();

            // Actualizar campos básicos
            productoExistente.setNombreProducto(producto.getNombreProducto());
            productoExistente.setDescripcionProducto(producto.getDescripcionProducto());
            productoExistente.setPrecio(producto.getPrecio());
            productoExistente.setStock(producto.getStock());
            productoExistente.setPesoKg(producto.getPesoKg());

            // Asignar productor si se proporciona
            if (productorId != null) {
                productorService.obtenerPorId(productorId)
                        .ifPresent(productoExistente::setProductor);
            }

            // Asignar categoría si se proporciona
            if (categoriaId != null) {
                categoriaProductoService.obtenerPorId(categoriaId)
                        .ifPresent(productoExistente::setCategoria);
            }

            // Guardar el producto actualizado directamente
            Producto productoActualizado = productoService.guardar(productoExistente);

            // Actualizar asociaciones con fincas
            if (fincaIds != null) {
                // Eliminar asociaciones existentes
                List<ProductoFinca> asociacionesExistentes = productoFincaService.obtenerTodos().stream()
                    .filter(pf -> pf.getProducto() != null &&
                                 pf.getProducto().getIdProducto().equals(id))
                    .toList();

                for (ProductoFinca pf : asociacionesExistentes) {
                    productoFincaService.eliminar(pf.getIdProductoFinca());
                }

                // Crear nuevas asociaciones
                for (Integer fincaId : fincaIds) {
                    Optional<Finca> fincaOpt = fincaService.obtenerPorId(fincaId);
                    if (fincaOpt.isPresent()) {
                        ProductoFinca productoFinca = new ProductoFinca();
                        productoFinca.setProducto(productoActualizado);
                        productoFinca.setFinca(fincaOpt.get());
                        productoFinca.setCantidadProduccion(BigDecimal.ZERO);
                        productoFinca.setFechaCosecha(null);

                        productoFincaService.guardar(productoFinca);
                        log.info("Producto {} re-asociado a finca {}", id, fincaId);
                    }
                }
            }

            // Procesar nueva imagen si se proporciona
            if (imagenFile != null && !imagenFile.isEmpty()) {
                guardarImagenProducto(imagenFile, productoActualizado, esPrincipal);
            }

            redirectAttributes.addFlashAttribute("success", "updated");
            return "redirect:/productos";

        } catch (Exception e) {
            log.error("Error al actualizar producto {}: {}", id, e.getMessage(), e);
            redirectAttributes.addFlashAttribute("error", "update_failed");
            return "redirect:/productos";
        }
    }

    /**
     * Eliminar un producto
     */
    @PostMapping("/eliminar/{id:\\d+}")
    public String eliminarProducto(@PathVariable Integer id, RedirectAttributes redirectAttributes) {
        try {
            boolean eliminado = productoService.eliminar(id);
            if (eliminado) {
                redirectAttributes.addFlashAttribute("success", "deleted");
            } else {
                redirectAttributes.addFlashAttribute("error", "delete_failed");
            }
        } catch (Exception e) {
            // Si hay error por restricción de integridad (tiene compras asociadas)
            if (e.getMessage().contains("constraint") || e.getMessage().contains("foreign key")) {
                redirectAttributes.addFlashAttribute("error", "has_orders");
            } else {
                redirectAttributes.addFlashAttribute("error", "delete_failed");
            }
        }
        return "redirect:/productos";
    }

    /**
     * Método auxiliar para guardar imagen del producto
     */
    private void guardarImagenProducto(MultipartFile imagenFile, Producto producto, boolean esPrincipal) throws IOException {
        if (imagenFile.isEmpty()) {
            return;
        }

        // Definir las rutas donde guardar (desarrollo y producción)
        String devPath = "src/main/resources/static/images/products/";

        // Crear directorios si no existen
        Path devUploadPath = Paths.get(devPath);
        Path prodUploadPath = Paths.get(UPLOAD_DIR);

        if (!Files.exists(devUploadPath)) {
            Files.createDirectories(devUploadPath);
        }
        if (!Files.exists(prodUploadPath)) {
            Files.createDirectories(prodUploadPath);
        }

        // Generar nombre único para el archivo
        String originalFilename = imagenFile.getOriginalFilename();
        String extension = (originalFilename != null && originalFilename.contains("."))
            ? originalFilename.substring(originalFilename.lastIndexOf("."))
            : ".jpg";
        String nuevoNombre = UUID.randomUUID() + extension;

        // Guardar archivo físico (primero obtener los bytes para poder escribir en múltiples ubicaciones)
        byte[] bytes = imagenFile.getBytes();

        // Intentar guardar en la carpeta de desarrollo primero
        try {
            Path devFilePath = devUploadPath.resolve(nuevoNombre);
            Files.write(devFilePath, bytes);
        } catch (IOException e) {
            System.out.println("No se pudo guardar en la carpeta de desarrollo: " + e.getMessage());
        }

        // Guardar en la carpeta de producción/target
        Path prodFilePath = prodUploadPath.resolve(nuevoNombre);
        Files.write(prodFilePath, bytes);

        // Crear registro en base de datos
        ImagenProducto imagenProducto = new ImagenProducto();
        // Asegurar que la URL se guarde con barra inicial para rutas absolutas
        String urlRelativa = "images/products/" + nuevoNombre;
        imagenProducto.setUrlImagen(urlRelativa.startsWith("/") ? urlRelativa : "/" + urlRelativa);
        imagenProducto.setEsPrincipal(esPrincipal);
        imagenProducto.setProducto(producto);

        imagenProductoService.guardar(imagenProducto, producto.getIdProducto());
    }

    /**
     * Mostrar formulario de creación de producto
     */
    @GetMapping("/crear")
    public String mostrarFormularioCrear(Model model) {
        List<CategoriaProducto> categorias = categoriaProductoService.obtenerTodos();
        List<Productor> productores = productorService.obtenerTodos();
        List<Finca> fincas = fincaService.obtenerTodos();

        model.addAttribute("categorias", categorias);
        model.addAttribute("productores", productores);
        model.addAttribute("fincas", fincas);
        model.addAttribute("producto", new Producto());

        return "productos/crear";
    }

    /**
     * Mostrar formulario de edición en una página separada
     */
    @GetMapping("/editar/{id:\\d+}")
    public String mostrarFormularioEditar(@PathVariable Integer id, Model model) {
        Optional<Producto> productoOpt = productoService.obtenerPorId(id);
        if (productoOpt.isEmpty()) {
            return "redirect:/productos";
        }

        List<CategoriaProducto> categorias = categoriaProductoService.obtenerTodos();
        List<Productor> productores = productorService.obtenerTodos();
        List<Finca> fincas = fincaService.obtenerTodos();

        model.addAttribute("producto", productoOpt.get());
        model.addAttribute("categorias", categorias);
        model.addAttribute("productores", productores);
        model.addAttribute("fincas", fincas);

        return "productos/editar";
    }

    /**
     * Mostrar lista de pedidos/compras dirigidas al productor
     */
    @GetMapping("/pedidos")
    public String listarPedidos(Model model) {
        List<Compra> compras = compraService.obtenerTodas();
        model.addAttribute("compras", compras);
        model.addAttribute("page", "pedidos");
        return "productos/pedidos";
    }

    /**
     * Endpoint JSON para cargar pedidos vía AJAX (usado en el dashboard interactivo)
     * Maneja fechas NULL y referencias circulares
     */
    @GetMapping("/pedidos/json")
    @ResponseBody
    public ResponseEntity<?> obtenerPedidosJson() {
        try {
            List<Compra> compras = compraService.obtenerTodas();

            // Si no hay compras, retornar lista vacía
            if (compras == null || compras.isEmpty()) {
                return ResponseEntity.ok(new ArrayList<>());
            }

            // Filtrar compras con fechas válidas para evitar errores
            List<Map<String, Object>> comprasSimplificadas = new ArrayList<>();
            for (Compra c : compras) {
                try {
                    Map<String, Object> compraMap = new HashMap<>();
                    compraMap.put("idCompra", c.getIdCompra());
                    compraMap.put("fechaHoraCompra", c.getFechaHoraCompra());
                    compraMap.put("total", c.getTotal());
                    compraMap.put("direccionEntrega", c.getDireccionEntrega());
                    compraMap.put("metodoPago", c.getMetodoPago());

                    // Datos del cliente (evitar referencia circular)
                    if (c.getCliente() != null) {
                        Map<String, Object> clienteMap = new HashMap<>();
                        clienteMap.put("nombre", c.getCliente().getUsuario() != null ?
                            c.getCliente().getUsuario().getNombre() : "Sin nombre");
                        clienteMap.put("correo", c.getCliente().getUsuario() != null ?
                            c.getCliente().getUsuario().getCorreo() : "");
                        compraMap.put("cliente", clienteMap);
                    }

                    comprasSimplificadas.add(compraMap);
                } catch (Exception ex) {
                    log.warn("Error al procesar compra {}: {}", c.getIdCompra(), ex.getMessage());
                    // Continuar con las demás compras
                }
            }

            return ResponseEntity.ok(comprasSimplificadas);
        } catch (Exception e) {
            log.error("Error al obtener pedidos: {}", e.getMessage(), e);
            Map<String, String> error = new HashMap<>();
            error.put("error", "Error al cargar pedidos: " + e.getMessage());
            return ResponseEntity.status(500).body(error);
        }
    }

    /**
     * Endpoint JSON para listar todas las fincas del productor
     * Evita referencias circulares simplificando los datos
     */
    @GetMapping("/fincas/listar")
    @ResponseBody
    public ResponseEntity<?> listarFincasJson() {
        try {
            List<Finca> fincas = fincaService.obtenerTodos();

            if (fincas == null || fincas.isEmpty()) {
                return ResponseEntity.ok(new ArrayList<>());
            }

            // Simplificar datos para evitar referencias circulares
            List<Map<String, Object>> fincasSimplificadas = new ArrayList<>();
            for (Finca f : fincas) {
                try {
                    Map<String, Object> fincaMap = new HashMap<>();
                    fincaMap.put("idFinca", f.getIdFinca());
                    fincaMap.put("nombreFinca", f.getNombreFinca() != null ? f.getNombreFinca() : "Sin nombre");
                    fincaMap.put("departamento", f.getDepartamento() != null ? f.getDepartamento() : "");
                    fincaMap.put("ciudad", f.getCiudad() != null ? f.getCiudad() : "");
                    fincaMap.put("direccionFinca", f.getDireccionFinca() != null ? f.getDireccionFinca() : "");
                    fincaMap.put("latitud", f.getLatitud());
                    fincaMap.put("longitud", f.getLongitud());
                    fincaMap.put("certificadoBPA", f.getCertificadoBPA() != null ? f.getCertificadoBPA() : "Sin Certificado");
                    fincaMap.put("registroICA", f.getRegistroICA() != null ? f.getRegistroICA() : "Sin Certificado");

                    // Contar productos asociados de manera segura
                    int cantidadProductos = 0;
                    try {
                        cantidadProductos = f.getProductoFincas() != null ? f.getProductoFincas().size() : 0;
                    } catch (Exception e) {
                        log.debug("No se pudo cargar productos de finca {}", f.getIdFinca());
                    }
                    fincaMap.put("cantidadProductos", cantidadProductos);

                    fincasSimplificadas.add(fincaMap);
                } catch (Exception ex) {
                    log.warn("Error al procesar finca {}: {}", f.getIdFinca(), ex.getMessage());
                }
            }

            return ResponseEntity.ok(fincasSimplificadas);
        } catch (Exception e) {
            log.error("Error al listar fincas: {}", e.getMessage(), e);
            Map<String, String> error = new HashMap<>();
            error.put("error", "Error al cargar fincas: " + e.getMessage());
            return ResponseEntity.status(500).body(error);
        }
    }

    /**
     * Endpoint de DEBUG para ver todas las fincas en el sistema
     */
    @GetMapping("/fincas/debug")
    @ResponseBody
    public ResponseEntity<?> debugTodasLasFincas() {
        try {
            log.info("========================================");
            log.info("DEBUG: Obteniendo TODAS las fincas del sistema");

            List<Finca> todasLasFincas = fincaService.obtenerTodos();
            log.info("DEBUG: Total de fincas en el sistema: {}", todasLasFincas.size());

            List<Map<String, Object>> fincasDebug = new ArrayList<>();

            for (Finca f : todasLasFincas) {
                Map<String, Object> fincaMap = new HashMap<>();
                fincaMap.put("idFinca", f.getIdFinca());
                fincaMap.put("nombreFinca", f.getNombreFinca());
                fincaMap.put("ciudad", f.getCiudad());
                fincaMap.put("departamento", f.getDepartamento());
                fincaMap.put("productorId", f.getProductor() != null ? f.getProductor().getIdProductor() : null);

                fincasDebug.add(fincaMap);
                log.info("DEBUG: Finca ID {} - {} (Productor: {})",
                    f.getIdFinca(),
                    f.getNombreFinca(),
                    f.getProductor() != null ? f.getProductor().getIdProductor() : "NULL");
            }

            log.info("========================================");

            return ResponseEntity.ok(Map.of(
                "total", todasLasFincas.size(),
                "fincas", fincasDebug
            ));

        } catch (Exception e) {
            log.error("DEBUG ERROR: {}", e.getMessage(), e);
            return ResponseEntity.status(500).body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Endpoint JSON para obtener fincas de un productor específico
     * Se usa en el formulario de creación de productos para seleccionar la finca
     */
    @GetMapping("/fincas/por-productor/{productorId}")
    @ResponseBody
    public ResponseEntity<?> obtenerFincasPorProductor(@PathVariable Integer productorId) {
        try {
            log.info("========================================");
            log.info("Solicitando fincas para productor ID: {}", productorId);

            // Verificar que el productor existe
            Optional<Productor> productorOpt = productorService.obtenerPorId(productorId);
            if (productorOpt.isEmpty()) {
                log.warn("Productor no encontrado con ID: {}", productorId);
                return ResponseEntity.badRequest()
                    .body(Map.of("error", "Productor no encontrado"));
            }

            log.info("Productor encontrado: {}", productorOpt.get().getIdProductor());

            // Obtener fincas del productor usando el método optimizado del servicio
            List<Finca> fincasDelProductor = fincaService.obtenerPorProductor(productorId);
            log.info("Total de fincas encontradas para productor {}: {}", productorId, fincasDelProductor.size());

            if (fincasDelProductor.isEmpty()) {
                log.warn("El productor {} no tiene fincas registradas", productorId);
            }

            List<Map<String, Object>> fincasSimplificadas = new ArrayList<>();

            for (Finca f : fincasDelProductor) {
                log.info("Procesando finca ID: {}, Nombre: {}", f.getIdFinca(), f.getNombreFinca());

                Map<String, Object> fincaMap = new HashMap<>();
                fincaMap.put("idFinca", f.getIdFinca());
                fincaMap.put("nombreFinca", f.getNombreFinca() != null ? f.getNombreFinca() : "Sin nombre");
                fincaMap.put("ciudad", f.getCiudad() != null ? f.getCiudad() : "");
                fincaMap.put("departamento", f.getDepartamento() != null ? f.getDepartamento() : "");
                fincaMap.put("direccionFinca", f.getDireccionFinca() != null ? f.getDireccionFinca() : "");

                fincasSimplificadas.add(fincaMap);
            }

            log.info("Fincas simplificadas a enviar: {}", fincasSimplificadas.size());
            log.info("========================================");

            return ResponseEntity.ok(fincasSimplificadas);

        } catch (Exception e) {
            log.error("Error al obtener fincas del productor {}: {}", productorId, e.getMessage(), e);
            return ResponseEntity.status(500)
                .body(Map.of("error", "Error al cargar las fincas: " + e.getMessage()));
        }
    }

    /**
     * Endpoint JSON para crear una nueva finca (sin productos asociados inicialmente)
     * Los productos se asociarán cuando se creen/editen
     */
    @PostMapping("/fincas/crear")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> crearFincaJson(@RequestBody Map<String, Object> fincaData) {
        try {
            // Extraer datos del JSON
            String nombreFinca = (String) fincaData.get("nombreFinca");
            String departamento = (String) fincaData.get("departamento");
            String ciudad = (String) fincaData.get("ciudad");
            String direccionFinca = (String) fincaData.get("direccionFinca");
            String certificadoBPA = (String) fincaData.getOrDefault("certificadoBPA", "Sin Certificado");
            String registroICA = (String) fincaData.getOrDefault("registroICA", "Sin Certificado");

            Double latitud = fincaData.get("latitud") != null ?
                Double.parseDouble(fincaData.get("latitud").toString()) : null;
            Double longitud = fincaData.get("longitud") != null ?
                Double.parseDouble(fincaData.get("longitud").toString()) : null;

            // Validar datos obligatorios
            if (nombreFinca == null || nombreFinca.trim().isEmpty()) {
                return ResponseEntity.badRequest()
                    .body(Map.of("error", "El nombre de la finca es obligatorio"));
            }

            // TODO: Obtener el productor actual de la sesión
            // Por ahora usaremos el primer productor disponible (TEMPORAL)
            List<Productor> productores = productorService.obtenerTodos();
            if (productores.isEmpty()) {
                return ResponseEntity.badRequest()
                    .body(Map.of("error", "No hay productores registrados"));
            }
            Productor productor = productores.get(0); // TEMPORAL: en producción debe venir de la sesión

            // Crear la finca
            Finca finca = new Finca();
            finca.setNombreFinca(nombreFinca);
            finca.setDepartamento(departamento);
            finca.setCiudad(ciudad);
            finca.setDireccionFinca(direccionFinca);
            finca.setLatitud(latitud);
            finca.setLongitud(longitud);
            finca.setCertificadoBPA(certificadoBPA);
            finca.setRegistroICA(registroICA);
            finca.setProductor(productor);

            // Guardar la finca
            Finca fincaGuardada = fincaService.guardar(finca);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("finca", fincaGuardada);
            response.put("message", "Finca creada exitosamente. Ahora puedes asociar productos al crearlos o editarlos.");

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Error al crear finca: {}", e.getMessage(), e);
            return ResponseEntity.status(500)
                .body(Map.of("error", "Error al crear la finca: " + e.getMessage()));
        }
    }
}
