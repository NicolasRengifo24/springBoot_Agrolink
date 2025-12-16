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
    private final DetalleCompraRepository detalleCompraRepository;

    public AdminController(ProductoRepository productoRepository,
                           CompraRepository compraRepository,
                           TransportistaRepository transportistaRepository,
                           ProductorRepository productorRepository,
                           UsuarioRepository usuarioRepository,
                           ClienteRepository clienteRepository,
                           AsesorRepository asesorRepository,
                           FincaRepository fincaRepository,
                           EnvioRepository envioRepository,
                           DetalleCompraRepository detalleCompraRepository) {
        this.productoRepository = productoRepository;
        this.compraRepository = compraRepository;
        this.transportistaRepository = transportistaRepository;
        this.productorRepository = productorRepository;
        this.usuarioRepository = usuarioRepository;
        this.clienteRepository = clienteRepository;
        this.asesorRepository = asesorRepository;
        this.fincaRepository = fincaRepository;
        this.envioRepository = envioRepository;
        this.detalleCompraRepository = detalleCompraRepository;
    }

    /**
     * Ruta raíz de admin - Redirige directamente a gestión de usuarios
     * Ruta: /admin
     */
    @GetMapping
    public String dashboardRedirect() {
        log.info("Redirigiendo /admin -> /admin/usuarios (vista admin.html eliminada)");
        return "redirect:/admin/usuarios";
    }

    /**
     * Vista de gestión de usuarios con tabs para cada rol
     */
    @GetMapping("/usuarios")
    public String gestionUsuarios(Model model) {
        log.info("=== Accediendo a /admin/usuarios ===");
        try {
            List<Usuario> todosUsuarios = usuarioRepository.findAll();
            List<Cliente> clientes = clienteRepository.findAll();
            List<Productor> productores = productorRepository.findAll();
            List<Transportista> transportistas = transportistaRepository.findAll();
            List<Asesor> asesores = asesorRepository.findAll();

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
            return "admin/usuarios";

        } catch (Exception e) {
            log.error("Error al cargar usuarios: {}", e.getMessage(), e);
            model.addAttribute("error", "Error al cargar usuarios");
            return "admin/usuarios";
        }
    }

    /**
     * API: Obtener detalles de un usuario
     */
    @GetMapping("/api/usuarios/{id}")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> obtenerDetallesUsuario(@PathVariable Integer id) {
        try {
            Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

            Map<String, Object> usuarioData = new HashMap<>();
            usuarioData.put("idUsuario", usuario.getIdUsuario());
            usuarioData.put("nombre", usuario.getNombre());
            usuarioData.put("apellido", usuario.getApellido());
            usuarioData.put("nombreUsuario", usuario.getNombreUsuario());
            usuarioData.put("correo", usuario.getCorreo());
            usuarioData.put("telefono", usuario.getTelefono());
            usuarioData.put("cedula", usuario.getCedula());
            usuarioData.put("direccion", usuario.getDireccion());
            usuarioData.put("ciudad", usuario.getCiudad());
            usuarioData.put("departamento", usuario.getDepartamento());
            usuarioData.put("rol", usuario.getRol() != null ? usuario.getRol().name() : null);

            return ResponseEntity.ok(usuarioData);

        } catch (Exception e) {
            log.error("Error al obtener usuario: {}", e.getMessage(), e);
            return ResponseEntity.status(500).body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Actualizar usuario - Método tradicional con formulario
     * Más simple: el formulario se envía directamente, sin JavaScript complicado
     */
    @PostMapping("/usuarios/actualizar/{id}")
    public String actualizarUsuario(@PathVariable Integer id,
                                     @RequestParam("nombre") String nombre,
                                     @RequestParam("apellido") String apellido,
                                     @RequestParam("correo") String correo,
                                     @RequestParam("telefono") String telefono,
                                     @RequestParam(value = "cedula", required = false) String cedula,
                                     @RequestParam(value = "direccion", required = false) String direccion,
                                     @RequestParam(value = "ciudad", required = false) String ciudad,
                                     @RequestParam(value = "departamento", required = false) String departamento) {
        try {
            log.info("=== Actualizando usuario ID: {} ===", id);
            log.info("Datos recibidos: nombre={}, apellido={}, correo={}, telefono={}", nombre, apellido, correo, telefono);

            // 1. Buscar el usuario en la base de datos
            Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

            log.info("Usuario encontrado: {}", usuario.getNombreUsuario());

            // 2. Actualizar los campos del usuario con los nuevos datos
            usuario.setNombre(nombre);
            usuario.setApellido(apellido);
            usuario.setCorreo(correo);
            usuario.setTelefono(telefono);
            usuario.setCedula(cedula);
            usuario.setDireccion(direccion);
            usuario.setCiudad(ciudad);
            usuario.setDepartamento(departamento);

            // 3. Guardar los cambios en la base de datos
            usuarioRepository.save(usuario);
            log.info("✅ Usuario actualizado exitosamente: {}", usuario.getNombreUsuario());

            // 4. Redirigir de vuelta a la lista de usuarios con mensaje de éxito
            return "redirect:/admin/usuarios?success=actualizado";

        } catch (Exception e) {
            log.error("❌ Error al actualizar usuario {}: {}", id, e.getMessage(), e);
            // En caso de error, redirigir con mensaje de error
            return "redirect:/admin/usuarios?error=actualizar";
        }
    }

    /**
     * Vista de gestión de productos
     */
    @GetMapping("/productos")
    public String gestionProductos(Model model) {
        try {
            List<Producto> productos = productoRepository.findAll();

            Set<CategoriaProducto> categoriasUnicas = new HashSet<>();
            for (Producto p : productos) {
                if (p.getCategoria() != null) {
                    categoriasUnicas.add(p.getCategoria());
                }
            }

            model.addAttribute("productos", productos);
            model.addAttribute("categorias", new ArrayList<>(categoriasUnicas));
            model.addAttribute("totalProductos", productos.size());

            return "admin/productos";

        } catch (Exception e) {
            log.error("Error al cargar productos: {}", e.getMessage(), e);
            model.addAttribute("error", "Error al cargar productos");
            return "admin/productos";
        }
    }

    /**
     * Editar producto
     */
    @GetMapping("/productos/editar/{id}")
    public String editarProducto(@PathVariable("id") Integer id, Model model) {
        try {
            Producto producto = productoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Producto no encontrado"));

            Set<CategoriaProducto> categoriasUnicas = new HashSet<>();
            for (Producto p : productoRepository.findAll()) {
                if (p.getCategoria() != null) {
                    categoriasUnicas.add(p.getCategoria());
                }
            }

            model.addAttribute("producto", producto);
            model.addAttribute("categorias", new ArrayList<>(categoriasUnicas));
            model.addAttribute("productores", productorRepository.findAll());

            return "admin/editar-producto";

        } catch (Exception e) {
            log.error("Error al cargar producto: {}", e.getMessage(), e);
            return "redirect:/admin/productos";
        }
    }

    /**
     * Actualizar producto
     */
    @PostMapping("/productos/actualizar/{id}")
    public String actualizarProducto(@PathVariable("id") Integer id,
                                      @RequestParam("nombreProducto") String nombreProducto,
                                      @RequestParam(value = "descripcionProducto", required = false) String descripcionProducto,
                                      @RequestParam("stock") Integer stock,
                                      @RequestParam("precio") BigDecimal precio,
                                      @RequestParam(value = "pesoKg", required = false) BigDecimal pesoKg,
                                      @RequestParam("categoria.idCategoria") Integer categoriaId,
                                      @RequestParam("productor.idProductor") Integer productorId) {
        try {
            Producto producto = productoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Producto no encontrado"));

            producto.setNombreProducto(nombreProducto);
            producto.setDescripcionProducto(descripcionProducto);
            producto.setStock(stock);
            producto.setPrecio(precio);
            producto.setPesoKg(pesoKg);

            if (categoriaId != null) {
                CategoriaProducto categoria = new CategoriaProducto();
                categoria.setIdCategoria(categoriaId);
                producto.setCategoria(categoria);
            }

            if (productorId != null) {
                Productor productor = productorRepository.findById(productorId).orElse(null);
                if (productor != null) {
                    producto.setProductor(productor);
                }
            }

            productoRepository.save(producto);
            return "redirect:/admin/productos?success=actualizado";

        } catch (Exception e) {
            log.error("Error al actualizar producto: {}", e.getMessage(), e);
            return "redirect:/admin/productos?error=actualizar";
        }
    }

    /**
     * Eliminar producto
     */
    @DeleteMapping("/productos/eliminar/{id}")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> eliminarProducto(@PathVariable("id") Integer id) {
        Map<String, Object> response = new HashMap<>();
        try {
            Producto producto = productoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Producto no encontrado"));

            productoRepository.delete(producto);
            response.put("success", true);
            response.put("message", "Producto eliminado exitosamente");
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Error al eliminar producto: {}", e.getMessage(), e);
            response.put("success", false);
            response.put("message", "Error al eliminar producto");
            return ResponseEntity.status(500).body(response);
        }
    }

    /**
     * Vista de pedidos
     */
    @GetMapping("/pedidos")
    public String gestionPedidos(Model model) {
        try {
            List<Compra> compras = compraRepository.findAll();

            List<Compra> comprasOrdenadas = compras.stream()
                .sorted((c1, c2) -> c2.getIdCompra().compareTo(c1.getIdCompra()))
                .toList();

            BigDecimal totalVentas = compras.stream()
                .map(Compra::getTotal)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

            // Obtener detalles de compra para cada pedido
            Map<Integer, List<DetalleCompra>> detallesPorCompra = new HashMap<>();
            for (Compra compra : comprasOrdenadas) {
                List<DetalleCompra> detalles = detalleCompraRepository.findByCompra_IdCompra(compra.getIdCompra());
                detallesPorCompra.put(compra.getIdCompra(), detalles);
            }

            model.addAttribute("compras", comprasOrdenadas);
            model.addAttribute("detallesPorCompra", detallesPorCompra);
            model.addAttribute("totalPedidos", compras.size());
            model.addAttribute("totalVentas", totalVentas);

            return "admin/pedidos";

        } catch (Exception e) {
            log.error("Error al cargar pedidos: {}", e.getMessage(), e);
            model.addAttribute("error", "Error al cargar pedidos");
            return "admin/pedidos";
        }
    }

    /**
     * Vista de envíos
     */
    @GetMapping("/envios")
    public String gestionEnvios(Model model) {
        try {
            List<Envio> envios = envioRepository.findAll();

            List<Envio> enviosOrdenados = envios.stream()
                .sorted((e1, e2) -> e2.getIdEnvio().compareTo(e1.getIdEnvio()))
                .toList();

            model.addAttribute("envios", enviosOrdenados);
            model.addAttribute("totalEnvios", envios.size());

            return "admin/envios";

        } catch (Exception e) {
            log.error("Error al cargar envíos: {}", e.getMessage(), e);
            model.addAttribute("error", "Error al cargar envíos");
            return "admin/envios";
        }
    }

    /**
     * API: Obtener fincas de un productor
     */
    @GetMapping("/api/productores/{id}/fincas")
    @ResponseBody
    public ResponseEntity<List<Map<String, Object>>> obtenerFincasDeProductor(@PathVariable("id") Integer productorId) {
        try {
            List<Finca> todasFincas = fincaRepository.findAll();
            List<Map<String, Object>> fincasData = new ArrayList<>();

            for (Finca finca : todasFincas) {
                if (finca.getProductor() != null &&
                    finca.getProductor().getIdProductor() != null &&
                    finca.getProductor().getIdProductor().equals(productorId)) {

                    Map<String, Object> fincaMap = new HashMap<>();
                    fincaMap.put("idFinca", finca.getIdFinca());
                    fincaMap.put("nombreFinca", finca.getNombreFinca() != null ? finca.getNombreFinca() : "Finca sin nombre");
                    fincaMap.put("ubicacion", finca.getCiudad() != null ? finca.getCiudad() : "Sin ubicación");
                    fincaMap.put("direccion", finca.getDireccionFinca() != null ? finca.getDireccionFinca() : "");

                    fincasData.add(fincaMap);
                }
            }

            return ResponseEntity.ok(fincasData);

        } catch (Exception e) {
            log.error("Error al obtener fincas: {}", e.getMessage(), e);
            return ResponseEntity.status(500).body(new ArrayList<>());
        }
    }
}

