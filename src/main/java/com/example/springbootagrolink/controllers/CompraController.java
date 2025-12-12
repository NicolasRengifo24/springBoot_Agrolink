package com.example.springbootagrolink.controllers;

import com.example.springbootagrolink.model.*;
import com.example.springbootagrolink.services.CompraService;
import com.example.springbootagrolink.services.ProductoService;
import com.example.springbootagrolink.repository.UsuarioRepository;
import com.example.springbootagrolink.repository.ClienteRepository;
import com.example.springbootagrolink.repository.EnvioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.servlet.http.HttpSession;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.math.BigDecimal;
import java.util.*;

@Controller
@RequestMapping("/compras")
public class CompraController {

    private static final Logger log = LoggerFactory.getLogger(CompraController.class);

    private final CompraService compraService;

    @Autowired
    private ProductoService productoService;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private ClienteRepository clienteRepository;

    @Autowired
    private EnvioRepository envioRepository;

    public CompraController(CompraService compraService) {
        this.compraService = compraService;
    }

    /**
     * Método que agrega el contador del carrito a todas las vistas automáticamente
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
            Usuario usuario = usuarioRepository.findByNombreUsuario(username).orElse(null);
            if (usuario != null) {
                return usuario.getNombre();
            }
        }
        return null;
    }

    // -----------------------------------------------------------
    // GET - Listar compras
    // -----------------------------------------------------------
    @GetMapping
    public String listarCompras(
            Model model,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        Page<Compra> compras = compraService.buscarCompras(null, PageRequest.of(page, size));
        model.addAttribute("compras", compras);
        return "compras/lista";
    }

    // -----------------------------------------------------------
    // GET - Mostrar detalle de una compra
    // -----------------------------------------------------------
    @GetMapping("/{id}")
    public String detalleCompra(@PathVariable Integer id, Model model) {
        Compra compra = compraService.buscarCompras(
                (Specification<Compra>) (root, query, cb) ->
                        cb.equal(root.get("idCompra"), id),
                PageRequest.of(0, 1)
        ).stream().findFirst().orElse(null);

        model.addAttribute("compra", compra);
        return "compras/detalle";
    }

    // -----------------------------------------------------------
    // GET - Formulario para crear compra desde el carrito
    // -----------------------------------------------------------
    @GetMapping("/crear")
    public String mostrarFormularioCrear(HttpSession session, Model model, RedirectAttributes redirectAttributes) {
        try {
            System.out.println("=== INICIO mostrarFormularioCrear ===");

            // Obtener el carrito desde la sesión
            @SuppressWarnings("unchecked")
            Map<Integer, Integer> carrito = (Map<Integer, Integer>) session.getAttribute("carrito");
            System.out.println("Carrito obtenido: " + (carrito != null ? carrito.size() + " items" : "null"));

            if (carrito == null || carrito.isEmpty()) {
                System.out.println("ERROR: Carrito vacío");
                redirectAttributes.addFlashAttribute("error", "El carrito está vacío");
                return "redirect:/cliente/carrito";
            }

            // Obtener usuario autenticado
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            System.out.println("Authentication: " + (authentication != null ? authentication.getName() : "null"));

            if (authentication == null || !authentication.isAuthenticated() ||
                "anonymousUser".equals(authentication.getPrincipal())) {
                System.out.println("ERROR: Usuario no autenticado");
                redirectAttributes.addFlashAttribute("error", "Debe iniciar sesión para realizar una compra");
                return "redirect:/login";
            }

            String username = authentication.getName();
            Usuario usuario = usuarioRepository.findByNombreUsuario(username).orElse(null);
            System.out.println("Usuario encontrado: " + (usuario != null ? usuario.getNombre() : "null"));

            if (usuario == null) {
                System.out.println("ERROR: Usuario no encontrado en BD");
                redirectAttributes.addFlashAttribute("error", "Usuario no encontrado");
                return "redirect:/login";
            }

            // Obtener el cliente asociado al usuario
            Cliente cliente = clienteRepository.findById(usuario.getIdUsuario()).orElse(null);
            System.out.println("Cliente encontrado: " + (cliente != null ? "SI" : "NO"));

            // Si no existe el cliente, crearlo usando SQL nativo para evitar problemas con @MapsId
            if (cliente == null) {
                System.out.println("Cliente no existe, creándolo manualmente...");
                try {
                    // Crear el cliente manualmente usando el constructor
                    cliente = new Cliente();
                    cliente.setIdUsuario(usuario.getIdUsuario());
                    cliente.setUsuario(usuario);
                    cliente.setPreferencias("Sin Preferencias");
                    cliente.setCalificacion(null);

                    // Guardar usando flush para forzar la persistencia inmediata
                    cliente = clienteRepository.saveAndFlush(cliente);
                    System.out.println("Cliente creado exitosamente con ID: " + cliente.getIdUsuario());
                } catch (Exception ex) {
                    System.err.println("Error al crear cliente: " + ex.getMessage());
                    // Si falla la creación, continuar de todos modos usando el ID del usuario
                    System.out.println("Continuando sin registro de cliente en tb_clientes");
                }
            }

            // Convertir el carrito a una lista de items con detalles del producto
            System.out.println("Procesando items del carrito...");
            List<Map<String, Object>> cartItems = new ArrayList<>();
            double subtotal = 0.0;
            double pesoTotal = 0.0;

            for (Map.Entry<Integer, Integer> entry : carrito.entrySet()) {
                Integer idProducto = entry.getKey();
                Integer cantidad = entry.getValue();
                System.out.println("Procesando producto ID: " + idProducto + ", cantidad: " + cantidad);

                Producto producto = productoService.obtenerPorId(idProducto).orElse(null);
                if (producto != null) {
                    String imagenUrl = "/imag/placeholder.jpg";
                    if (producto.getImagenesProducto() != null && !producto.getImagenesProducto().isEmpty()) {
                        imagenUrl = "/" + producto.getImagenesProducto().get(0).getUrlImagen();
                    }

                    double precioProducto = producto.getPrecio().doubleValue();
                    double subtotalItem = precioProducto * cantidad;
                    subtotal += subtotalItem;

                    // Calcular peso total
                    double pesoProducto = producto.getPesoKg() != null ? producto.getPesoKg().doubleValue() : 1.0;
                    pesoTotal += pesoProducto * cantidad;

                    Map<String, Object> item = new HashMap<>();
                    item.put("id", producto.getIdProducto());
                    item.put("nombre", producto.getNombreProducto());
                    item.put("precio", precioProducto);
                    item.put("cantidad", cantidad);
                    item.put("subtotal", subtotalItem);
                    item.put("imagenUrl", imagenUrl);
                    item.put("peso", pesoProducto);

                    cartItems.add(item);
                    System.out.println("Producto agregado: " + producto.getNombreProducto());
                } else {
                    System.out.println("WARNING: Producto no encontrado ID: " + idProducto);
                }
            }

            // Calcular costos
            double costoEnvio = pesoTotal * 800; // $800 por kg
            double impuestos = subtotal * 0.08; // 8% de impuestos
            double total = subtotal + costoEnvio + impuestos;

            System.out.println("Cálculos - Subtotal: " + subtotal + ", Envío: " + costoEnvio + ", Total: " + total);

            // Agregar atributos al modelo
            model.addAttribute("usuario", usuario);
            model.addAttribute("cliente", cliente);
            model.addAttribute("cartItems", cartItems);
            model.addAttribute("subtotal", subtotal);
            model.addAttribute("pesoTotal", pesoTotal);
            model.addAttribute("costoEnvio", costoEnvio);
            model.addAttribute("impuestos", impuestos);
            model.addAttribute("total", total);

            System.out.println("=== FIN mostrarFormularioCrear - Retornando vista compras/crear ===");
            return "compras/crear";

        } catch (Exception e) {
            System.err.println("=== ERROR EN mostrarFormularioCrear ===");
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("error", "Error al cargar la página de compra: " + e.getMessage());
            return "redirect:/cliente/carrito";
        }
    }

    // -----------------------------------------------------------
    // POST - Procesar compra desde el carrito
    // -----------------------------------------------------------
    @PostMapping("/procesar")
    public String procesarCompra(
            @RequestParam String direccionEntrega,
            @RequestParam String metodoPago,
            @RequestParam(required = false) String notasAdicionales,
            HttpSession session,
            RedirectAttributes redirectAttributes
    ) {
        try {
            System.out.println("=== INICIO procesarCompra ===");
            System.out.println("Dirección: " + direccionEntrega);
            System.out.println("Método de pago: " + metodoPago);

            // Obtener el carrito desde la sesión
            @SuppressWarnings("unchecked")
            Map<Integer, Integer> carrito = (Map<Integer, Integer>) session.getAttribute("carrito");
            System.out.println("Carrito: " + (carrito != null ? carrito.size() + " items" : "null"));

            if (carrito == null || carrito.isEmpty()) {
                System.out.println("ERROR: Carrito vacío");
                redirectAttributes.addFlashAttribute("error", "El carrito está vacío");
                return "redirect:/cliente/carrito";
            }

            // Obtener usuario autenticado
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String username = authentication.getName();
            Usuario usuario = usuarioRepository.findByNombreUsuario(username).orElse(null);
            System.out.println("Usuario encontrado: " + (usuario != null ? usuario.getNombre() : "null"));

            if (usuario == null) {
                System.out.println("ERROR: Usuario no encontrado");
                redirectAttributes.addFlashAttribute("error", "Usuario no encontrado");
                return "redirect:/login";
            }

            Cliente cliente = clienteRepository.findById(usuario.getIdUsuario()).orElse(null);
            System.out.println("Cliente encontrado: " + (cliente != null ? "SI" : "NO"));

            // Si no existe el cliente, crearlo automáticamente
            if (cliente == null) {
                System.out.println("Cliente no existe, creándolo...");
                try {
                    cliente = new Cliente();
                    cliente.setIdUsuario(usuario.getIdUsuario());
                    cliente.setUsuario(usuario);
                    cliente.setPreferencias("Sin Preferencias");
                    cliente.setCalificacion(null);
                    cliente = clienteRepository.saveAndFlush(cliente);
                    System.out.println("Cliente creado exitosamente con ID: " + cliente.getIdUsuario());
                } catch (Exception ex) {
                    System.err.println("Error al crear cliente: " + ex.getMessage());
                    ex.printStackTrace();
                    redirectAttributes.addFlashAttribute("error", "Error al procesar la compra. Por favor, intenta nuevamente.");
                    return "redirect:/compras/crear";
                }
            }

            // Crear la compra
            System.out.println("Creando compra con ID cliente: " + cliente.getIdUsuario());
            Compra nuevaCompra = compraService.crearCompra(cliente.getIdUsuario(), metodoPago, direccionEntrega);
            System.out.println("Compra creada con ID: " + nuevaCompra.getIdCompra());

            // Agregar cada producto del carrito a la compra
            System.out.println("Agregando productos a la compra...");
            for (Map.Entry<Integer, Integer> entry : carrito.entrySet()) {
                Integer idProducto = entry.getKey();
                Integer cantidad = entry.getValue();
                System.out.println("Agregando producto ID: " + idProducto + ", cantidad: " + cantidad);
                compraService.agregarProducto(nuevaCompra.getIdCompra(), idProducto, cantidad);
            }

            // Limpiar el carrito después de procesar la compra
            session.removeAttribute("carrito");
            System.out.println("Carrito limpiado de la sesión");

            // Guardar el ID de la compra en sesión para usarlo en la pasarela de pago
            session.setAttribute("idCompraActual", nuevaCompra.getIdCompra());
            System.out.println("ID de compra guardado en sesión: " + nuevaCompra.getIdCompra());

            // Redirigir a la pasarela de pago
            String redirectUrl = "redirect:/pago/index?idCompra=" + nuevaCompra.getIdCompra();
            System.out.println("=== FIN procesarCompra - Redirigiendo a: " + redirectUrl + " ===");
            return redirectUrl;

        } catch (Exception e) {
            System.err.println("=== ERROR EN procesarCompra ===");
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("error", "Error al procesar la compra: " + e.getMessage());
            return "redirect:/compras/crear";
        }
    }

    // -----------------------------------------------------------
    // POST - Agregar producto a una compra
    // -----------------------------------------------------------
    @PostMapping("/{idCompra}/agregar-producto")
    public String agregarProducto(
            @PathVariable Integer idCompra,
            @RequestParam Integer idProducto,
            @RequestParam Integer cantidad
    ) {
        DetalleCompra d = compraService.agregarProducto(idCompra, idProducto, cantidad);
        return "redirect:/compras/" + idCompra;
    }

    // -----------------------------------------------------------
    // GET - Cancelar compra
    // -----------------------------------------------------------
    @GetMapping("/cancelar/{idCompra}")
    public String cancelarCompra(@PathVariable Integer idCompra) {
        compraService.cancelarCompra(idCompra);
        return "redirect:/compras";
    }

    // -----------------------------------------------------------
    // POST - Eliminar compra y sus envíos asociados
    // -----------------------------------------------------------
    @PostMapping("/{idCompra}/eliminar")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> eliminarCompra(@PathVariable Integer idCompra) {
        try {
            log.info("▶ INICIANDO ELIMINACIÓN DE COMPRA");
            log.info("  ID Compra: {}", idCompra);

            // Validar que el ID sea válido
            if (idCompra == null || idCompra <= 0) {
                log.error("✗ ID de compra inválido: {}", idCompra);
                return ResponseEntity.badRequest()
                    .body(Map.of("success", false, "message", "ID de compra inválido"));
            }

            // Verificar que la compra exista
            Optional<Compra> compraOpt = compraService.obtenerPorId(idCompra);
            if (compraOpt.isEmpty()) {
                log.error("✗ Compra no encontrada: {}", idCompra);
                return ResponseEntity.badRequest()
                    .body(Map.of("success", false, "message", "Compra no encontrada"));
            }

            Compra compra = compraOpt.get();
            log.info("  ✓ Compra encontrada: ID={}, Cliente={}", compra.getIdCompra(),
                    (compra.getCliente() != null ? compra.getCliente().getIdUsuario() : "NULL"));

            // Eliminar todos los envíos asociados a esta compra (SIN IMPORTAR EL ESTADO)
            log.info("  → Buscando envíos asociados...");
            try {
                // Obtener TODOS los envíos (sin filtrar por estado)
                List<Envio> todosLosEnvios = envioRepository.findAll();
                List<Envio> enviosAsociados = todosLosEnvios.stream()
                    .filter(e -> {
                        try {
                            return e.getCompra() != null &&
                                   e.getCompra().getIdCompra() != null &&
                                   e.getCompra().getIdCompra().equals(idCompra);
                        } catch (Exception ex) {
                            log.warn("Error al filtrar envío: {}", ex.getMessage());
                            return false;
                        }
                    })
                    .toList();

                if (!enviosAsociados.isEmpty()) {
                    log.info("    Encontrados {} envío(s) asociado(s)", enviosAsociados.size());
                    for (Envio envio : enviosAsociados) {
                        try {
                            log.info("    Eliminando envío: ID={}, Estado={}", envio.getIdEnvio(), envio.getEstadoEnvio());
                            envioRepository.deleteById(envio.getIdEnvio());
                            log.info("    ✓ Envío eliminado: ID={}", envio.getIdEnvio());
                        } catch (Exception ex) {
                            log.warn("    ⚠ Error al eliminar envío {}: {}", envio.getIdEnvio(), ex.getMessage());
                            // Continuar con los siguientes envíos
                        }
                    }
                } else {
                    log.info("    No hay envíos asociados");
                }
            } catch (Exception ex) {
                log.warn("  ⚠ Error buscando envíos: {}", ex.getMessage());
                // Continuar incluso si hay error en envíos
            }

            // Eliminar la compra
            log.info("  → Eliminando compra...");
            try {
                compraService.cancelarCompra(idCompra);
                log.info("    ✓ Compra eliminada");
            } catch (Exception ex) {
                log.error("    ✗ Error al cancelar compra: {}", ex.getMessage());
                throw ex; // Re-lanzar el error porque es crítico
            }

            log.info("✓ COMPRA ELIMINADA EXITOSAMENTE");
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Compra eliminada correctamente"
            ));

        } catch (Exception e) {
            log.error("✗ ERROR AL ELIMINAR COMPRA: {}", e.getMessage());
            log.error("  Tipo: {}", e.getClass().getSimpleName());

            // Log del stack trace completo para debugging
            StringWriter sw = new StringWriter();
            e.printStackTrace(new PrintWriter(sw));
            log.error("  Stack trace:\n{}", sw.toString());

            return ResponseEntity.status(500)
                .body(Map.of(
                    "success", false,
                    "message", "Error al eliminar la compra: " + e.getMessage()
                ));
        }
    }
}


