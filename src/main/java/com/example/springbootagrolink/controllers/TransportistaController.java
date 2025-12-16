package com.example.springbootagrolink.controllers;

import com.example.springbootagrolink.model.*;
import com.example.springbootagrolink.repository.*;
import com.example.springbootagrolink.services.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Controlador para las vistas del Transportista con datos reales de la BD
 */
@Controller
@RequestMapping("/transportista")
public class TransportistaController {

    private static final Logger log = LoggerFactory.getLogger(TransportistaController.class);

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private TransportistaRepository transportistaRepository;

    @Autowired
    private EnvioRepository envioRepository;

    @Autowired
    private VehiculoRepository vehiculoRepository;

    @Autowired
    private TransportistaService transportistaService;

    @Autowired
    private jakarta.persistence.EntityManager entityManager;

    @Autowired
    private com.example.springbootagrolink.services.CategoriaProductoService categoriaProductoService;

    @Autowired
    private com.example.springbootagrolink.services.ServicioService servicioService;

    /**
     * API para obtener envíos disponibles en JSON (usando SQL nativa para obtener cliente)
     */
    @GetMapping("/envios-api")
    @ResponseBody
    public ResponseEntity<List<Map<String, Object>>> obtenerEnviosDisponiblesApi() {
        try {
            log.info("▶ Obteniendo envíos disponibles para API (JSON)");

            List<Map<String, Object>> respuesta = new ArrayList<>();

            // Obtener envíos disponibles con SQL nativa que trae el nombre del cliente
            List<Object[]> enviosData = envioRepository.findEnviosDisponiblesConCliente("Buscando_Transporte");

            if (enviosData != null && !enviosData.isEmpty()) {
                log.info("  → Envíos obtenidos: {}", enviosData.size());

                for (Object[] row : enviosData) {
                    Map<String, Object> envioMap = new HashMap<>();

                    // Los valores vienen en orden: id_envio, direccion_origen, direccion_destino,
                    // distancia_km, peso_total_kg, costo_total, estado_envio, id_compra, nombre_cliente
                    envioMap.put("idEnvio", row[0]);
                    envioMap.put("direccionOrigen", row[1] != null ? row[1] : "N/A");
                    envioMap.put("direccionDestino", row[2] != null ? row[2] : "N/A");
                    envioMap.put("distanciaKm", row[3] != null ? row[3] : 0);
                    envioMap.put("pesoTotalKg", row[4] != null ? row[4] : 0);
                    envioMap.put("costoTotal", row[5] != null ? row[5] : 0);
                    envioMap.put("estadoEnvio", row[6] != null ? row[6] : "Buscando_Transporte");

                    // El nombre del cliente viene directamente de la SQL (índice 8)
                    String cliente = row[8] != null ? row[8].toString() : "Por asignar";
                    log.debug("    Envío ID {} → Cliente: {}", row[0], cliente);

                    envioMap.put("cliente", cliente);
                    respuesta.add(envioMap);
                }
            } else {
                log.info("  → No hay envíos disponibles");
            }

            log.info("✓ Enviando {} envíos disponibles en JSON", respuesta.size());
            return ResponseEntity.ok(respuesta);

        } catch (Exception e) {
            log.error("✗ Error obteniendo envíos disponibles: {}", e.getMessage());
            log.error("Stack trace: ", e);
            return ResponseEntity.ok(new ArrayList<>());
        }
    }

    /**
     * Obtener el transportista autenticado
     */
    private Transportista obtenerTransportistaAutenticado() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();

        Usuario usuario = usuarioRepository.findByNombreUsuario(username)
            .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        Transportista transportista = transportistaRepository.findById(usuario.getIdUsuario())
            .orElse(null);

        if (transportista == null) {
            log.warn("El usuario {} tiene rol TRANSPORTISTA pero no existe registro en tb_transportistas", username);
            // Usar el servicio para crear el transportista (esto tiene @Transactional correcto)
            transportista = transportistaService.crearTransportistaAutomatico(usuario);
        }

        return transportista;
    }

    /**
     * Obtener nombre del cliente usando SQL nativa (evita lazy loading de fecha_hora_compra)
     */
    private String obtenerNombreClientePorCompra(Integer idCompra) {
        try {
            String sql = "SELECT CONCAT(u.nombre, ' ', u.apellido) " +
                        "FROM tb_compras c " +
                        "JOIN tb_clientes cl ON c.id_cliente = cl.id_usuario " +
                        "JOIN tb_usuarios u ON cl.id_usuario = u.id_usuario " +
                        "WHERE c.id_compra = ?1";

            Object result = entityManager.createNativeQuery(sql)
                .setParameter(1, idCompra)
                .getSingleResult();

            return result != null ? result.toString() : "Sin asignar";
        } catch (Exception e) {
            log.warn("Error al obtener nombre de cliente para compra {}: {}", idCompra, e.getMessage());
            return "Sin asignar";
        }
    }

    // =============================================================================
    // NOTE: Se eliminó la vista 'dashboard' y los endpoints relacionados a sus gráficos
    // - Se removieron: método calcularIngresosPorMes, método GET /dashboard,
    //   y método GET /api/datos-dashboard
    // - Esto evita servir la plantilla view y las APIs asociadas a las gráficas
    // =============================================================================


    /**
     * Ver envíos disponibles para aceptar (sin transportista asignado)
     */
    @GetMapping("/envios")
    public String enviosDisponibles(Model model) {
        try {
            log.info("▶ ▶ ▶ INICIANDO: Obtener envíos disponibles para transportista");

            // Obtener envíos disponibles SIN fechas asignadas (filtrado en BD)
            log.info("  → Consultando BD para envíos sin fechas asignadas...");
            List<Envio> enviosDisponibles = envioRepository.findEnviosDisponiblesSinFechas(Envio.EstadoEnvio.Buscando_Transporte);

            if (enviosDisponibles == null) {
                log.warn("  ⚠ Query retornó NULL, usando lista vacía");
                enviosDisponibles = new ArrayList<>();
            }

            log.info("  ✓ Total de envíos disponibles (sin fechas asignadas): {}", enviosDisponibles.size());

            // Mostrar detalles de CADA envío encontrado
            for (int i = 0; i < enviosDisponibles.size(); i++) {
                Envio envio = enviosDisponibles.get(i);
                log.info("    [Envío {}]", (i + 1));
                log.info("      ID: {}", envio.getIdEnvio());
                log.info("      Origen: {}", envio.getDireccionOrigen());
                log.info("      Destino: {}", envio.getDireccionDestino());
                log.info("      Distancia: {} km", envio.getDistanciaKm());
                log.info("      Peso: {} kg", envio.getPesoTotalKg());
                log.info("      Costo: ${}", envio.getCostoTotal());
                log.info("      Estado: {}", envio.getEstadoEnvio());
                // NO acceder a fechaHoraCompra para evitar error "Zero date value prohibited"
                log.info("      Tiene Compra: {}", envio.getCompra() != null ? "SÍ (ID: " + envio.getCompra().getIdCompra() + ")" : "NO");
            }

            // Mostrar solo envíos realmente disponibles (sin fechas asignadas)
            // Los campos como fecha_entrega, fecha_salida, transportista, vehiculo se llenarán cuando se acepte
            log.info("  ✓ Se mostrarán {} envíos disponibles (filtrados sin fechas)", enviosDisponibles.size());

            // Crear mapa de nombres de clientes usando SQL nativa (evita lazy loading problemático)
            Map<Integer, String> nombresClientes = new HashMap<>();
            for (Envio envio : enviosDisponibles) {
                if (envio.getCompra() != null) {
                    try {
                        // Obtener nombre del cliente usando SQL nativa para evitar fecha_hora_compra
                        Integer idCompra = envio.getCompra().getIdCompra();
                        String nombreCliente = obtenerNombreClientePorCompra(idCompra);
                        nombresClientes.put(envio.getIdEnvio(), nombreCliente != null ? nombreCliente : "Sin asignar");
                    } catch (Exception e) {
                        log.warn("No se pudo obtener cliente para envío {}", envio.getIdEnvio());
                        nombresClientes.put(envio.getIdEnvio(), "Sin asignar");
                    }
                } else {
                    nombresClientes.put(envio.getIdEnvio(), "Sin asignar");
                }
            }

            // Agregar datos al modelo
            model.addAttribute("envios", enviosDisponibles);
            model.addAttribute("nombresClientes", nombresClientes);

            try {
                Transportista transportistaModel = obtenerTransportistaAutenticado();
                Usuario usuarioModel = transportistaModel.getUsuario();
                // Forzar carga de datos del usuario para evitar lazy issues
                if (usuarioModel != null) {
                    usuarioModel.getNombre();
                    usuarioModel.getNombreUsuario();
                }
                model.addAttribute("usuario", usuarioModel);

                // --- Exponer los vehículos del transportista para el modal de aceptación ---
                List<Vehiculo> vehiculos = vehiculoRepository.findByTransportista_IdUsuario(transportistaModel.getIdUsuario());
                if (vehiculos == null) vehiculos = new ArrayList<>();
                model.addAttribute("vehiculos", vehiculos);

            } catch (Exception ex) {
                model.addAttribute("usuario", null);
            }

            log.info("✓ ✓ ✓ ÉXITO: {} envíos disponibles agregados al modelo", enviosDisponibles.size());
            return "transportista/envios";

        } catch (Exception e) {
            log.error("✗ ✗ ✗ ERROR CRÍTICO al cargar envíos disponibles");
            log.error("  Tipo de excepción: {}", e.getClass().getName());
            log.error("  Mensaje: {}", e.getMessage());

            // Imprimir stack trace
            for (StackTraceElement ste : e.getStackTrace()) {
                log.error("    at {}", ste);
            }

            return "transportista/envios";
        }
    }

    /**
     * Aceptar un envío disponible
     */
    @PostMapping("/envios/aceptar/{id}")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> aceptarEnvio(
            @PathVariable Integer id,
            @RequestParam(required = false) Integer vehiculoId,
            @RequestParam(required = false) String fechaSalida,
            @RequestParam(required = false) String fechaEntrega) {
        try {
            Transportista transportista = obtenerTransportistaAutenticado();

            Optional<Envio> envioOpt = envioRepository.findById(id);
            if (envioOpt.isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("success", false, "message", "Envío no encontrado"));
            }

            Envio envio = envioOpt.get();

            if (envio.getEstadoEnvio() != Envio.EstadoEnvio.Buscando_Transporte) {
                return ResponseEntity.badRequest().body(Map.of("success", false, "message", "El envío ya no está disponible"));
            }

            // Asignar transportista
            envio.setTransportista(transportista);

            // Si se envió un vehiculoId, validar y asignar
            if (vehiculoId != null) {
                Optional<Vehiculo> vehiculoOpt = vehiculoRepository.findById(vehiculoId);
                if (vehiculoOpt.isEmpty()) {
                    return ResponseEntity.badRequest().body(Map.of("success", false, "message", "Vehículo no encontrado"));
                }
                Vehiculo vehiculo = vehiculoOpt.get();
                // Verificar que el vehículo pertenece al transportista
                if (vehiculo.getTransportista() == null || !vehiculo.getTransportista().getIdUsuario().equals(transportista.getIdUsuario())) {
                    return ResponseEntity.badRequest().body(Map.of("success", false, "message", "Vehículo no autorizado"));
                }
                envio.setVehiculo(vehiculo);
            }

            // Asignar fecha de salida si se proporciona
            if (fechaSalida != null && !fechaSalida.isBlank()) {
                try {
                    envio.setFechaSalida(java.time.LocalDate.parse(fechaSalida));
                } catch (Exception ex) {
                    return ResponseEntity.badRequest().body(Map.of("success", false, "message", "Fecha de salida inválida (formato esperado: yyyy-MM-dd)"));
                }
            } else {
                envio.setFechaSalida(null);
            }

            // Asignar fecha de entrega si se proporciona
            if (fechaEntrega != null && !fechaEntrega.isBlank()) {
                try {
                    envio.setFechaEntrega(java.time.LocalDate.parse(fechaEntrega));
                } catch (Exception ex) {
                    return ResponseEntity.badRequest().body(Map.of("success", false, "message", "Fecha de entrega inválida (formato esperado: yyyy-MM-dd)"));
                }
            } else {
                envio.setFechaEntrega(null);
            }

            // Cambiar estado del envío a ASIGNADO
            envio.setEstadoEnvio(Envio.EstadoEnvio.Asignado);
            envioRepository.save(envio);

            return ResponseEntity.ok(Map.of("success", true, "message", "Envío aceptado y asignado correctamente"));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("success", false, "message", "Error al aceptar envío: " + e.getMessage()));
        }
    }
    /**
     * Ver envíos aceptados por el transportista
     */
    @GetMapping("/mis-envios")
    @Transactional(readOnly = true)
    public String misEnvios(Model model) {
        try {
            Transportista transportista = obtenerTransportistaAutenticado();
            Usuario usuario = transportista.getUsuario();

            // Forzar carga de datos del usuario
            usuario.getNombre();
            usuario.getNombreUsuario();

            List<Envio> misEnvios = envioRepository.findByTransportista_IdUsuario(transportista.getIdUsuario());
            if (misEnvios == null) {
                misEnvios = new ArrayList<>();
            }

            // Forzar la inicialización de todas las relaciones LAZY para cada envío
            misEnvios.forEach(envio -> {
                envio.getIdEnvio();
                envio.getDireccionOrigen();
                envio.getDireccionDestino();
                envio.getDistanciaKm();
                envio.getPesoTotalKg();
                envio.getCostoTotal();
                envio.getEstadoEnvio();

                if (envio.getCompra() != null) {
                    Compra compra = envio.getCompra();
                    compra.getIdCompra();
                    compra.getDireccionEntrega();
                    compra.getSubtotal();
                    compra.getTotal();

                    if (compra.getCliente() != null) {
                        Cliente cliente = compra.getCliente();
                        cliente.getIdUsuario();

                        if (cliente.getUsuario() != null) {
                            Usuario clienteUsuario = cliente.getUsuario();
                            clienteUsuario.getNombre();
                            clienteUsuario.getTelefono();
                        }
                    }
                }
            });

            model.addAttribute("usuario", usuario);
            model.addAttribute("envios", misEnvios);

            log.info("Mis envíos cargados para {}: {} envíos",
                usuario.getNombreUsuario(), misEnvios.size());
            return "transportista/mis-envios";

        } catch (Exception e) {
            log.error("Error al cargar mis envíos: {}", e.getMessage(), e);
            try {
                Transportista transportista = obtenerTransportistaAutenticado();
                Usuario usuario = transportista.getUsuario();
                usuario.getNombre();
                usuario.getNombreUsuario();
                model.addAttribute("usuario", usuario);
            } catch (Exception ex) {
                log.error("No se pudo obtener usuario: {}", ex.getMessage());
            }
            model.addAttribute("envios", new ArrayList<>());
            model.addAttribute("error", "Error al cargar mis envíos: " + e.getMessage());
            return "transportista/mis-envios";
        }
    }

    /**
     * Actualizar estado de un envío
     */
    @PostMapping("/envios/actualizar-estado/{id}")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> actualizarEstadoEnvio(
            @PathVariable Integer id,
            @RequestParam String estado) {
        try {
            Transportista transportista = obtenerTransportistaAutenticado();

            Optional<Envio> envioOpt = envioRepository.findById(id);
            if (envioOpt.isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("success", false, "message", "Envío no encontrado"));
            }

            Envio envio = envioOpt.get();

            // Verificar que el envío pertenece al transportista
            if (envio.getTransportista() == null || !envio.getTransportista().getIdUsuario().equals(transportista.getIdUsuario())) {
                return ResponseEntity.badRequest().body(Map.of("success", false, "message", "No autorizado"));
            }

            // Estado previo (antes del cambio)
            Envio.EstadoEnvio prevEstadoEnum = envio.getEstadoEnvio();
            String prevEstado = prevEstadoEnum != null ? prevEstadoEnum.name() : null;

             // Si el transportista solicita devolver el envío a búsqueda, limpiar asignaciones
             if ("Buscando_Transporte".equals(estado)) {
                 envio.setTransportista(null);
                 envio.setVehiculo(null);
                 envio.setFechaSalida(null);
                 envio.setFechaEntrega(null);
                 envio.setEstadoEnvio(Envio.EstadoEnvio.Buscando_Transporte);

                 envioRepository.save(envio);
                 log.info("El envío {} fue cancelado por el transportista y devuelto a Buscando_Transporte", id);
                 return ResponseEntity.ok(Map.of("success", true, "message", "Envío cancelado y devuelto a búsqueda de transportista", "prevEstado", prevEstado, "nuevoEstado", "Buscando_Transporte"));
             }

             // Si se marca como finalizado, establecer fecha de entrega
             if ("Finalizado".equals(estado)) {
                 envio.setEstadoEnvio(Envio.EstadoEnvio.Finalizado);
                 envio.setFechaEntrega(java.time.LocalDate.now());
                 envioRepository.save(envio);
                 log.info("Estado del envío {} actualizado a Finalizado", id);
                 return ResponseEntity.ok(Map.of("success", true, "message", "Estado actualizado a Finalizado", "prevEstado", prevEstado, "nuevoEstado", "Finalizado"));
             }

             // Caso general (En_Transito, Asignado, etc.)
            envio.setEstadoEnvio(Envio.EstadoEnvio.valueOf(estado));
            envioRepository.save(envio);

            log.info("Estado del envío {} actualizado a {}", id, estado);
            return ResponseEntity.ok(Map.of("success", true, "message", "Estado actualizado exitosamente", "prevEstado", prevEstado, "nuevoEstado", estado));

        } catch (Exception e) {
            log.error("Error al actualizar estado del envío: {}", e.getMessage(), e);
            return ResponseEntity.status(500).body(Map.of("success", false, "message", "Error al actualizar estado"));
        }
    }

    /**
     * Gestión de vehículos del transportista
     */
    @GetMapping("/vehiculos")
    @Transactional(readOnly = true)
    public String vehiculos(Model model) {
        try {
            Transportista transportista = obtenerTransportistaAutenticado();
            Usuario usuario = transportista.getUsuario();

            // Forzar carga de datos del usuario
            usuario.getNombre();
            usuario.getNombreUsuario();

            List<Vehiculo> vehiculos = vehiculoRepository.findByTransportista_IdUsuario(transportista.getIdUsuario());
            if (vehiculos == null) {
                vehiculos = new ArrayList<>();
            }

            model.addAttribute("usuario", usuario);
            model.addAttribute("transportista", transportista);
            model.addAttribute("vehiculos", vehiculos);
            // Exponer las opciones del enum TipoVehiculo para poblar el select del formulario
            model.addAttribute("tipoVehiculoOptions", com.example.springbootagrolink.model.TipoVehiculo.values());

            log.info("Vehículos cargados para {}: {} vehículos",
                usuario.getNombreUsuario(), vehiculos.size());
            return "transportista/vehiculos";

        } catch (Exception e) {
            log.error("Error al cargar vehículos: {}", e.getMessage(), e);
            try {
                Transportista transportista = obtenerTransportistaAutenticado();
                Usuario usuario = transportista.getUsuario();
                usuario.getNombre();
                usuario.getNombreUsuario();
                model.addAttribute("usuario", usuario);
            } catch (Exception ex) {
                log.error("No se pudo obtener usuario: {}", ex.getMessage());
            }
            model.addAttribute("vehiculos", new ArrayList<>());
            model.addAttribute("error", "Error al cargar vehículos: " + e.getMessage());
            return "transportista/vehiculos";
        }
    }

    /**
     * Seguimiento de envíos con datos reales
     */
    @GetMapping("/seguimiento")
    @Transactional(readOnly = true)
    public String seguimiento(Model model) {
        try {
            Transportista transportista = obtenerTransportistaAutenticado();
            Usuario usuario = transportista.getUsuario();

            // Forzar carga de datos del usuario
            usuario.getNombre();
            usuario.getNombreUsuario();

            // Obtener todos los envíos del transportista
            List<Envio> todosLosEnvios = envioRepository.findByTransportista_IdUsuario(transportista.getIdUsuario());
            if (todosLosEnvios == null) {
                todosLosEnvios = new ArrayList<>();
            }

            // Filtrar envíos activos (En_Transito y Asignado)
            List<Envio> enviosEnTransito = todosLosEnvios.stream()
                .filter(e -> e.getEstadoEnvio() == Envio.EstadoEnvio.En_Transito ||
                            e.getEstadoEnvio() == Envio.EstadoEnvio.Asignado)
                .collect(Collectors.toList());

            // Calcular estadísticas
            long enTransito = todosLosEnvios.stream()
                .filter(e -> e.getEstadoEnvio() == Envio.EstadoEnvio.En_Transito)
                .count();

            long pendientes = todosLosEnvios.stream()
                .filter(e -> e.getEstadoEnvio() == Envio.EstadoEnvio.Asignado)
                .count();

            long finalizados = todosLosEnvios.stream()
                .filter(e -> e.getEstadoEnvio() == Envio.EstadoEnvio.Finalizado)
                .count();

            long total = todosLosEnvios.size();
            int tasaEntrega = total > 0 ? (int)((finalizados * 100) / total) : 0;

            model.addAttribute("usuario", usuario);
            model.addAttribute("envios", enviosEnTransito);
            model.addAttribute("enTransito", enTransito);
            model.addAttribute("pendientes", pendientes);
            model.addAttribute("tasaEntrega", tasaEntrega);

            log.info("Seguimiento cargado para {}: {} envíos en tránsito de {} totales",
                usuario.getNombreUsuario(), enviosEnTransito.size(), total);
            return "transportista/seguimiento";

        } catch (Exception e) {
            log.error("Error al cargar seguimiento: {}", e.getMessage(), e);
            try {
                Transportista transportista = obtenerTransportistaAutenticado();
                Usuario usuario = transportista.getUsuario();
                usuario.getNombre();
                usuario.getNombreUsuario();
                model.addAttribute("usuario", usuario);
            } catch (Exception ex) {
                log.error("No se pudo obtener usuario: {}", ex.getMessage());
            }
            model.addAttribute("envios", new ArrayList<>());
            model.addAttribute("enTransito", 0);
            model.addAttribute("pendientes", 0);
            model.addAttribute("tasaEntrega", 0);
            model.addAttribute("error", "Error al cargar seguimiento: " + e.getMessage());
            return "transportista/seguimiento";
        }
    }

    /**
     * Crear un nuevo vehículo
     */
    @PostMapping("/vehiculos/crear")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> crearVehiculo(
            @RequestParam String tipoVehiculo,
             @RequestParam BigDecimal capacidadCarga,
             @RequestParam String placaVehiculo) {
        try {
            Transportista transportista = obtenerTransportistaAutenticado();

            Vehiculo vehiculo = new Vehiculo();
            vehiculo.setTransportista(transportista);
            // Convertir el string recibido al enum TipoVehiculo (si no coincide, usar AUTOMOVIL por defecto)
            try {
                com.example.springbootagrolink.model.TipoVehiculo tv = com.example.springbootagrolink.model.TipoVehiculo.valueOf(tipoVehiculo);
                vehiculo.setTipoVehiculo(tv);
            } catch (Exception ex) {
                log.warn("Tipo de vehículo inválido recibido: {}. Usando AUTOMOVIL por defecto", tipoVehiculo);
                vehiculo.setTipoVehiculo(com.example.springbootagrolink.model.TipoVehiculo.AUTOMOVIL);
            }
             vehiculo.setCapacidadCarga(capacidadCarga);
             vehiculo.setPlacaVehiculo(placaVehiculo);

             vehiculoRepository.save(vehiculo);

             log.info("Vehículo creado: {}", placaVehiculo);
             return ResponseEntity.ok(Map.of("success", true, "message", "Vehículo creado exitosamente"));

         } catch (Exception e) {
             log.error("Error al crear vehículo: {}", e.getMessage(), e);
             return ResponseEntity.status(500).body(Map.of("success", false, "message", "Error al crear vehículo"));
         }
     }

    /**
     * Eliminar un vehículo
     */
    @DeleteMapping("/vehiculos/eliminar/{id}")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> eliminarVehiculo(@PathVariable Integer id) {
        try {
            Transportista transportista = obtenerTransportistaAutenticado();

            Optional<Vehiculo> vehiculoOpt = vehiculoRepository.findById(id);
            if (vehiculoOpt.isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("success", false, "message", "Vehículo no encontrado"));
            }

            Vehiculo vehiculo = vehiculoOpt.get();

            // Verificar que el vehículo pertenece al transportista
            if (!vehiculo.getTransportista().getIdUsuario().equals(transportista.getIdUsuario())) {
                return ResponseEntity.badRequest().body(Map.of("success", false, "message", "No autorizado"));
            }

            vehiculoRepository.delete(vehiculo);

            log.info("Vehículo eliminado: {}", id);
            return ResponseEntity.ok(Map.of("success", true, "message", "Vehículo eliminado exitosamente"));

        } catch (Exception e) {
            log.error("Error al eliminar vehículo: {}", e.getMessage(), e);
            return ResponseEntity.status(500).body(Map.of("success", false, "message", "Error al eliminar vehículo"));
        }
    }

    // Endpoint para ver el perfil del transportista: reenvía al controlador central de perfiles
    @GetMapping("/perfil")
    public String perfilTransportistaPreparado(Model model) {
        try {
            // Obtener usuario autenticado
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication == null || !authentication.isAuthenticated() || "anonymousUser".equals(authentication.getPrincipal())) {
                return "redirect:/login";
            }

            String username = authentication.getName();
            Usuario usuario = usuarioRepository.findByNombreUsuario(username).orElse(null);
            if (usuario == null) {
                return "redirect:/login";
            }

            // Verificar rol
            if (usuario.getRol() == null || !"ROLE_TRANSPORTISTA".equals(usuario.getRol().name())) {
                // Reusar controlador central de perfiles para otros roles
                return "redirect:/perfil";
            }

            // Obtener o crear transportista asociado
            Transportista transportista = transportistaRepository.findById(usuario.getIdUsuario()).orElse(null);
            if (transportista == null) {
                try {
                    transportista = transportistaService.crearTransportistaAutomatico(usuario);
                } catch (Exception e) {
                    // Si falla la creación automática, seguimos con null y mostramos mensaje
                    transportista = null;
                }
            }

            // Preparar un view-model (Mapa) con las propiedades que la plantilla espera
            Map<String, Object> transportistaVM = new HashMap<>();

            // Usuario dentro del view-model
            Map<String, Object> usuarioVM = new HashMap<>();
            usuarioVM.put("nombre", usuario.getNombre());
            usuarioVM.put("apellido", usuario.getApellido());
            usuarioVM.put("telefono", usuario.getTelefono());
            usuarioVM.put("correo", usuario.getCorreo());
            usuarioVM.put("nombreUsuario", usuario.getNombreUsuario());
            transportistaVM.put("usuario", usuarioVM);

            // Campos simples
            transportistaVM.put("empresa", null);
            transportistaVM.put("documentacionCompleta", false);
            transportistaVM.put("zonasEntrega", transportista != null ? transportista.getZonasEntrega() : null);

            // Vehículos: mapear a la forma que espera la plantilla (tipo, placa, capacidad, disponible)
            List<Map<String, Object>> vehiculosVM = new ArrayList<>();
            if (transportista != null) {
                List<Vehiculo> vehiculos = vehiculoRepository.findByTransportista_IdUsuario(transportista.getIdUsuario());
                if (vehiculos != null) {
                    for (Vehiculo v : vehiculos) {
                        Map<String, Object> vmap = new HashMap<>();
                        vmap.put("tipo", v.getTipoVehiculo() != null ? v.getTipoVehiculo().name() : "N/A");
                        vmap.put("placa", v.getPlacaVehiculo());
                        vmap.put("capacidad", v.getCapacidadCarga());
                        // No existe campo disponible en la entidad; asumir true
                        vmap.put("disponible", true);
                        vehiculosVM.add(vmap);
                    }
                }
            }
            transportistaVM.put("vehiculos", vehiculosVM);

            // Calificación: adaptar la entidad Calificacion (puntaje/promedio) al template (puntuacion, totalEvaluaciones, comentarios)
            Map<String, Object> calVM = new HashMap<>();
            if (transportista != null && transportista.getCalificacion() != null) {
                com.example.springbootagrolink.model.Calificacion c = transportista.getCalificacion();
                // Usar 'promedio' si existe, sino 'puntaje'
                Number puntuacion = null;
                try {
                    java.lang.reflect.Method mProm = c.getClass().getMethod("getPromedio");
                    Object val = mProm.invoke(c);
                    if (val instanceof Number) puntuacion = (Number) val;
                } catch (Exception ignored) {}
                try {
                    if (puntuacion == null) {
                        java.lang.reflect.Method mPun = c.getClass().getMethod("getPuntaje");
                        Object val2 = mPun.invoke(c);
                        if (val2 instanceof Number) puntuacion = (Number) val2;
                    }
                } catch (Exception ignored) {}

                calVM.put("puntuacion", puntuacion != null ? puntuacion.doubleValue() : 0);
                calVM.put("totalEvaluaciones", 0);
                calVM.put("comentarios", new ArrayList<>());
            } else {
                calVM.put("puntuacion", 0);
                calVM.put("totalEvaluaciones", 0);
                calVM.put("comentarios", new ArrayList<>());
            }
            transportistaVM.put("calificacion", calVM);

            // Agregar atributos al modelo
            model.addAttribute("usuario", usuario);
            model.addAttribute("transportista", transportistaVM);
            model.addAttribute("title", "Mi Perfil - Transportista");
            model.addAttribute("datosEspecificos", transportistaVM);
            model.addAttribute("templateEspecifico", "transportista/perfil :: transportista-perfil");
            model.addAttribute("mostrarTransportista", transportistaVM != null);

            // Añadir datos para el navbar/plantilla (categorías y servicios)
            try {
                model.addAttribute("categorias", categoriaProductoService.obtenerTodos());
                // Agrupar servicios por clave similar al controller de perfil
                Map<String, List<com.example.springbootagrolink.model.Servicio>> categoriasServicios = new java.util.HashMap<>();
                List<com.example.springbootagrolink.model.Servicio> servicios = servicioService.obtenerTodosLosServicios();
                if (servicios != null) {
                    for (com.example.springbootagrolink.model.Servicio s : servicios) {
                        String key = "Otros";
                        try {
                            if (s.getAsesor() != null && s.getAsesor().getTipoAsesoria() != null && !s.getAsesor().getTipoAsesoria().trim().isEmpty()) {
                                key = s.getAsesor().getTipoAsesoria();
                            }
                        } catch (Exception ignored) {}
                        categoriasServicios.computeIfAbsent(key, k -> new java.util.ArrayList<>()).add(s);
                    }
                }
                model.addAttribute("categoriasServicios", categoriasServicios);
            } catch (Exception e) {
                model.addAttribute("categorias", new java.util.ArrayList<>());
                model.addAttribute("categoriasServicios", new java.util.HashMap<>());
            }

            // Estadísticas para transportista
            Map<String, Object> estadisticas = new HashMap<>();
            try {
                List<com.example.springbootagrolink.model.Envio> todos = envioRepository.findByTransportista_IdUsuario(usuario.getIdUsuario());
                if (todos == null) todos = new ArrayList<>();

                long finalizados = todos.stream().filter(e -> e.getEstadoEnvio() == com.example.springbootagrolink.model.Envio.EstadoEnvio.Finalizado).count();
                long asignados = todos.stream().filter(e -> e.getEstadoEnvio() == com.example.springbootagrolink.model.Envio.EstadoEnvio.Asignado).count();
                long enTransito = todos.stream().filter(e -> e.getEstadoEnvio() == com.example.springbootagrolink.model.Envio.EstadoEnvio.En_Transito).count();
                int tasaExito = todos.size() > 0 ? (int) ((finalizados * 100) / todos.size()) : 0;

                estadisticas.put("entregasCompletadas", finalizados);
                estadisticas.put("entregasPendientes", asignados + enTransito);
                estadisticas.put("entregasMes", 0);
                estadisticas.put("tasaExito", tasaExito);
                estadisticas.put("tiempoPromedio", 0);
                estadisticas.put("saldoDisponible", 0);
                estadisticas.put("ingresosMes", 0);
                estadisticas.put("pendienteCobro", 0);
            } catch (Exception e) {
                estadisticas.put("entregasCompletadas", 0);
                estadisticas.put("entregasPendientes", 0);
                estadisticas.put("entregasMes", 0);
                estadisticas.put("tasaExito", 0);
            }

            model.addAttribute("estadisticas", estadisticas);

            // Usar layout sin navbar para la vista específica del transportista
            return "layouts/perfil-sin-navbar";
        } catch (Exception e) {
            // En caso de error, redirigir al perfil general
            return "redirect:/perfil";
        }
    }
}
