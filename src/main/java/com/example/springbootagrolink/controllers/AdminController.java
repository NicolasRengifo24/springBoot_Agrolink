package com.example.springbootagrolink.controllers;

import com.example.springbootagrolink.model.*;
import com.example.springbootagrolink.repository.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
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

    public AdminController(ProductoRepository productoRepository,
                           CompraRepository compraRepository,
                           TransportistaRepository transportistaRepository,
                           ProductorRepository productorRepository) {
        this.productoRepository = productoRepository;
        this.compraRepository = compraRepository;
        this.transportistaRepository = transportistaRepository;
        this.productorRepository = productorRepository;
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

            log.info("Dashboard admin cargado - Pedidos: {}, Inventario: {} kg", totalPedidos, inventarioTotal);

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

