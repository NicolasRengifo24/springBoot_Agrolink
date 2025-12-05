package com.example.springbootagrolink.controllers;

import com.example.springbootagrolink.model.*;
import com.example.springbootagrolink.services.*;
import org.springframework.beans.factory.annotation.Autowired;
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
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Controller
@RequestMapping("/productos")
public class ProductoController {

    @Autowired
    private ProductoService productoService;

    @Autowired
    private ProductorService productorService;

    @Autowired
    private CategoriaProductoService categoriaProductoService;

    @Autowired
    private ImagenProductoService imagenProductoService;

    // Directorio para guardar las imágenes (usar ruta absoluta que funcione en desarrollo y producción)
    private static final String UPLOAD_DIR = "target/classes/static/images/products/";

    /**
     * Mostrar lista de todos los productos
     */
    @GetMapping
    public String listarProductos(Model model) {
        List<Producto> productos = productoService.obtenerTodos();
        List<CategoriaProducto> categorias = categoriaProductoService.obtenerTodos();

        model.addAttribute("productos", productos);
        model.addAttribute("categorias", categorias);
        model.addAttribute("ubicacion", "");
        model.addAttribute("categoriaId", 0);

        return "productos/lista";
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

        model.addAttribute("productos", productos);
        model.addAttribute("categorias", categorias);
        model.addAttribute("ubicacion", ubicacion);
        model.addAttribute("categoriaId", categoriaId);

        return "productos/lista";
    }

    /**
     * Mostrar formulario para crear un nuevo producto
     */
    @GetMapping("/nuevo")
    public String mostrarFormularioCrear(Model model) {
        model.addAttribute("producto", new Producto());
        model.addAttribute("productores", productorService.obtenerTodos());
        model.addAttribute("categorias", categoriaProductoService.obtenerTodos());
        return "productos/crear";
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
                return "redirect:/productos/nuevo";
            }
            producto.setProductor(productorOpt.get());

            // Validar y asignar la categoría
            Optional<CategoriaProducto> categoriaOpt = categoriaProductoService.obtenerPorId(categoriaId);
            if (categoriaOpt.isEmpty()) {
                redirectAttributes.addFlashAttribute("error", "Categoría no encontrada");
                return "redirect:/productos/nuevo";
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

            redirectAttributes.addFlashAttribute("success", "Producto creado exitosamente");
            return "redirect:/productos";

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error al crear el producto: " + e.getMessage());
            return "redirect:/productos/nuevo";
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
     * Mostrar formulario para editar un producto
     */
    @GetMapping("/editar/{id}")
    public String mostrarFormularioEditar(@PathVariable Integer id, Model model) {
        Optional<Producto> productoOpt = productoService.obtenerPorId(id);
        if (productoOpt.isEmpty()) {
            return "redirect:/productos";
        }

        Producto producto = productoOpt.get();
        model.addAttribute("producto", producto);
        model.addAttribute("productores", productorService.obtenerTodos());
        model.addAttribute("categorias", categoriaProductoService.obtenerTodos());

        // Cargar las imágenes existentes del producto
        if (producto.getImagenesProducto() != null) {
            model.addAttribute("imagenes", producto.getImagenesProducto());
        }

        return "productos/editar";
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
                Optional<Productor> productorOpt = productorService.obtenerPorId(productorId);
                if (productorOpt.isPresent()) {
                    productoExistente.setProductor(productorOpt.get());
                }
            }

            // Asignar categoría si se proporciona
            if (categoriaId != null) {
                Optional<CategoriaProducto> categoriaOpt = categoriaProductoService.obtenerPorId(categoriaId);
                if (categoriaOpt.isPresent()) {
                    productoExistente.setCategoria(categoriaOpt.get());
                }
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
            System.out.println("Error al actualizar producto: " + e.getMessage());
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("error", "update_failed");
            return "redirect:/productos/editar/" + id;
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
        String prodPath = UPLOAD_DIR;

        // Crear directorios si no existen
        Path devUploadPath = Paths.get(devPath);
        Path prodUploadPath = Paths.get(prodPath);

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
        imagenProducto.setUrlImagen("images/products/" + nuevoNombre);
        imagenProducto.setEsPrincipal(esPrincipal);
        imagenProducto.setProducto(producto);

        imagenProductoService.guardar(imagenProducto, producto.getIdProducto());
    }
}
