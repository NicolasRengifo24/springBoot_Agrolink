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

            // Obtener envíos disponibles directamente
            log.info("  → Consultando BD para envíos con estado Buscando_Transporte...");
            List<Envio> enviosDisponibles = envioRepository.findByEstadoEnvio(Envio.EstadoEnvio.Buscando_Transporte);
            
            if (enviosDisponibles == null) {
                log.warn("  ⚠ Query retornó NULL, usando lista vacía");
                enviosDisponibles = new ArrayList<>();
            }

            log.info("  ✓ Total de envíos encontrados en BD: {}", enviosDisponibles.size());

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
                log.info("      Compra: {}", envio.getCompra() != null ? envio.getCompra().getIdCompra() : "NULL");
            }

            // NO filtrar, mostrar TODOS los envíos encontrados
            // Los campos como fecha_entrega, fecha_salida, transportista, vehiculo se llenarán cuando se acepte
            log.info("  ✓ Se mostrarán {} envíos sin filtrado", enviosDisponibles.size());

            // Agregar datos al modelo
            model.addAttribute("envios", enviosDisponibles);
            try {
                Transportista transportistaModel = obtenerTransportistaAutenticado();
                Usuario usuarioModel = transportistaModel.getUsuario();
                // Forzar carga de datos del usuario para evitar lazy issues
                if (usuarioModel != null) {
                    usuarioModel.getNombre();
                    usuarioModel.getNombreUsuario();
                }
                model.addAttribute("usuario", usuarioModel);
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

            model.addAttribute("envios", new ArrayList<>());
            model.addAttribute("error", "Error al cargar envíos: " + e.getMessage());
            return "transportista/envios";
        }
    }

    /**
     * Aceptar un envío disponible
     */
    @PostMapping("/envios/aceptar/{id}")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> aceptarEnvio(@PathVariable Integer id) {
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

            envio.setTransportista(transportista);
            envio.setEstadoEnvio(Envio.EstadoEnvio.Asignado);
            envioRepository.save(envio);

            log.info("Envío {} aceptado por transportista {}", id, transportista.getIdUsuario());
            return ResponseEntity.ok(Map.of("success", true, "message", "Envío aceptado exitosamente"));

        } catch (Exception e) {
            log.error("Error al aceptar envío: {}", e.getMessage(), e);
            return ResponseEntity.status(500).body(Map.of("success", false, "message", "Error al aceptar envío"));
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

            envio.setEstadoEnvio(Envio.EstadoEnvio.valueOf(estado));

            // Si se marca como finalizado, establecer fecha de entrega
            if (estado.equals("Finalizado")) {
                envio.setFechaEntrega(java.time.LocalDate.now());
            }

            envioRepository.save(envio);

            log.info("Estado del envío {} actualizado a {}", id, estado);
            return ResponseEntity.ok(Map.of("success", true, "message", "Estado actualizado exitosamente"));

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
            vehiculo.setTipoVehiculo(tipoVehiculo);
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
}
