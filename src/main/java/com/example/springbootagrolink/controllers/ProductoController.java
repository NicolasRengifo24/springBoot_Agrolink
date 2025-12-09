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

        model.addAttribute("productos", productos);
        model.addAttribute("categorias", categorias);
        model.addAttribute("productores", productores);
        model.addAttribute("ubicacion", "");
        model.addAttribute("categoriaId", 0);

        return "productos/dashboard";
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
                if (compra.getFechaHoraCompra().isAfter(ahora.minusMonths(6))) {
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

            // Procesar imagen si se proporciona
            if (imagenFile != null && !imagenFile.isEmpty()) {
                guardarImagenProducto(imagenFile, productoGuardado, esPrincipal);
            }

            redirectAttributes.addFlashAttribute("success", "created");
            return "redirect:/productos";

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error al crear el producto: " + e.getMessage());
            return "redirect:/productos";
        }
    }

    /**
     * Ver detalles de un producto específico
     */
    @GetMapping("/{id}")
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
    @PostMapping("/actualizar/{id}")
    public String actualizarProducto(@PathVariable Integer id,
                                   @ModelAttribute Producto producto,
                                   @RequestParam(value = "productorId", required = false) Integer productorId,
                                   @RequestParam(value = "categoriaId", required = false) Integer categoriaId,
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
    @PostMapping("/eliminar/{id}")
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

        model.addAttribute("categorias", categorias);
        model.addAttribute("productores", productores);
        model.addAttribute("producto", new Producto());

        return "productos/crear";
    }

    /**
     * Mostrar formulario de edición en una página separada
     */
    @GetMapping("/editar/{id}")
    public String mostrarFormularioEditar(@PathVariable Integer id, Model model) {
        Optional<Producto> productoOpt = productoService.obtenerPorId(id);
        if (productoOpt.isEmpty()) {
            return "redirect:/productos";
        }

        List<CategoriaProducto> categorias = categoriaProductoService.obtenerTodos();
        List<Productor> productores = productorService.obtenerTodos();

        model.addAttribute("producto", productoOpt.get());
        model.addAttribute("categorias", categorias);
        model.addAttribute("productores", productores);

        return "productos/editar";
    }
}
