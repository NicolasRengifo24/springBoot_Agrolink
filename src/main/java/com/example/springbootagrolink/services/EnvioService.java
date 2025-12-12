package com.example.springbootagrolink.services;

import com.example.springbootagrolink.model.Envio;
import com.example.springbootagrolink.model.DetalleCompra;
import com.example.springbootagrolink.model.ProductoFinca;
import com.example.springbootagrolink.model.Producto;
import com.example.springbootagrolink.model.Finca;
import com.example.springbootagrolink.repository.EnvioRepository;
import com.example.springbootagrolink.repository.DetalleCompraRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.math.BigDecimal;
import java.util.Map;

@Service
public class EnvioService {

    private static final Logger log = LoggerFactory.getLogger(EnvioService.class);

    @Autowired
    private EnvioRepository envioRepository;

    @Autowired
    private DetalleCompraRepository detalleCompraRepository;

    @Autowired
    private CostoEnvioService costoEnvioService;

    // Obtener todos los envíos
    public List<Envio> obtenerTodos() {
        return envioRepository.findAll();
    }

    // Obtener un envío por ID
    public Optional<Envio> obtenerPorId(Integer id) {
        return envioRepository.findById(id);
    }

    // Crear nuevo envío
    @Transactional
    public Envio crear(Envio envio) {
        try {
            log.info("▶ INICIANDO CREACIÓN DE ENVÍO");
            log.info("  ID Compra: {}", (envio.getCompra() != null ? envio.getCompra().getIdCompra() : "NULL"));

            // 1. OBTENER Y VALIDAR DIRECCIÓN DE DESTINO
            if ((envio.getDireccionDestino() == null || envio.getDireccionDestino().isEmpty())
                    && envio.getCompra() != null && envio.getCompra().getDireccionEntrega() != null) {
                envio.setDireccionDestino(envio.getCompra().getDireccionEntrega());
                log.info("  ✓ Dirección destino capturada desde compra: {}", envio.getDireccionDestino());
            }

            // 2. OBTENER COORDENADAS DE DESTINO
            if ((envio.getLatitudDestino() == null || envio.getLongitudDestino() == null)
                    && envio.getDireccionDestino() != null && !envio.getDireccionDestino().isEmpty()) {
                log.info("  → Obteniendo coordenadas de destino...");
                Map<String, Double> coordsDestino = costoEnvioService.obtenerCoordenadas(envio.getDireccionDestino());
                if (coordsDestino != null) {
                    envio.setLatitudDestino(coordsDestino.get("latitud"));
                    envio.setLongitudDestino(coordsDestino.get("longitud"));
                    log.info("    ✓ Coordenadas destino obtenidas: lat={}, lon={}",
                            envio.getLatitudDestino(), envio.getLongitudDestino());
                }
            }

            // 3. CALCULAR PESO TOTAL desde los detalles de la compra
            if ((envio.getPesoTotalKg() == null || envio.getPesoTotalKg() <= 0)
                    && envio.getCompra() != null && envio.getCompra().getIdCompra() != null) {
                log.info("  → Calculando peso total desde detalles de compra...");
                Double pesoTotal = 0.0;

                List<DetalleCompra> detalles = detalleCompraRepository.findByCompra_IdCompra(envio.getCompra().getIdCompra());
                if (detalles != null && !detalles.isEmpty()) {
                    for (DetalleCompra d : detalles) {
                        if (d.getProducto() != null && d.getProducto().getPesoKg() != null) {
                            BigDecimal pesoProducto = d.getProducto().getPesoKg();
                            int cantidad = d.getCantidad() != null ? d.getCantidad() : 1;
                            Double pesoParcial = pesoProducto.doubleValue() * cantidad;
                            pesoTotal += pesoParcial;
                            log.debug("    Producto: {}, Peso: {} kg, Cantidad: {}, Subtotal: {} kg",
                                    d.getProducto().getNombreProducto(), pesoProducto, cantidad, pesoParcial);
                        }
                    }
                }

                if (pesoTotal <= 0) {
                    pesoTotal = 1.0; // Peso mínimo
                    log.warn("    ⚠ Peso total <= 0, asignando mínimo: 1.0 kg");
                } else {
                    log.info("    ✓ Peso total calculado: {} kg", pesoTotal);
                }
                envio.setPesoTotalKg(pesoTotal);
            } else if (envio.getPesoTotalKg() != null) {
                log.info("  ✓ Peso total ya definido: {} kg", envio.getPesoTotalKg());
            }

            // 4. OBTENER COORDENADAS Y DIRECCIÓN DE ORIGEN desde la finca del primer producto
            if ((envio.getLatitudOrigen() == null || envio.getLongitudOrigen() == null)
                    && envio.getCompra() != null && envio.getCompra().getIdCompra() != null) {
                log.info("  → Obteniendo coordenadas de origen desde finca del primer producto...");

                List<DetalleCompra> detalles = detalleCompraRepository.findByCompra_IdCompra(envio.getCompra().getIdCompra());
                if (detalles != null && !detalles.isEmpty()) {
                    DetalleCompra primerDetalle = detalles.get(0);
                    Producto producto = primerDetalle.getProducto();

                    if (producto != null && producto.getProductoFincas() != null && !producto.getProductoFincas().isEmpty()) {
                        ProductoFinca pf = producto.getProductoFincas().get(0);
                        if (pf != null && pf.getFinca() != null) {
                            Finca finca = pf.getFinca();
                            envio.setLatitudOrigen(finca.getLatitud());
                            envio.setLongitudOrigen(finca.getLongitud());

                            if (envio.getDireccionOrigen() == null || envio.getDireccionOrigen().isEmpty()) {
                                envio.setDireccionOrigen(finca.getDireccionFinca());
                            }

                            log.info("    ✓ Coordenadas origen obtenidas desde finca: lat={}, lon={}",
                                    envio.getLatitudOrigen(), envio.getLongitudOrigen());
                            log.info("    ✓ Dirección origen: {}", envio.getDireccionOrigen());
                        }
                    }
                }
            }

            // 5. CALCULAR DISTANCIA si hay coordenadas de origen y destino
            if (envio.getLatitudOrigen() != null && envio.getLongitudOrigen() != null
                    && envio.getLatitudDestino() != null && envio.getLongitudDestino() != null) {
                log.info("  → Calculando distancia...");
                Double distancia = costoEnvioService.calcularDistanciaHaversine(
                        envio.getLatitudOrigen(), envio.getLongitudOrigen(),
                        envio.getLatitudDestino(), envio.getLongitudDestino());
                envio.setDistanciaKm(distancia);
                log.info("    ✓ Distancia calculada: {} km", distancia);

                // 6. CALCULAR COSTOS basados en distancia y peso
                log.info("  → Calculando costos...");
                Map<String, BigDecimal> desglose = costoEnvioService.calcularCostoEnvio(
                        envio.getDistanciaKm(), envio.getPesoTotalKg());

                if (desglose != null) {
                    envio.setCostoBase(desglose.getOrDefault("costoBase", BigDecimal.ZERO));
                    envio.setCostoPeso(desglose.getOrDefault("costoPeso", BigDecimal.ZERO));
                    envio.setCostoTotal(desglose.getOrDefault("costoTotal", BigDecimal.ZERO));

                    if (desglose.get("tarifaPorKm") != null) {
                        envio.setTarifaPorKm(desglose.get("tarifaPorKm"));
                    }
                    if (desglose.get("tarifaPorKg") != null) {
                        envio.setTarifaPorKg(desglose.get("tarifaPorKg"));
                    }

                    log.info("    ✓ Costos calculados:");
                    log.info("      - Costo Base (distancia): ${}", envio.getCostoBase());
                    log.info("      - Costo Peso: ${}", envio.getCostoPeso());
                    log.info("      - Costo Total: ${}", envio.getCostoTotal());
                }
            } else {
                log.warn("  ⚠ No hay coordenadas suficientes para calcular distancia y costos");
                log.warn("    Origen: lat={}, lon={}", envio.getLatitudOrigen(), envio.getLongitudOrigen());
                log.warn("    Destino: lat={}, lon={}", envio.getLatitudDestino(), envio.getLongitudDestino());
            }

            // 7. GUARDAR EL ENVÍO
            log.info("  → Guardando envío en BD...");
            Envio envioGuardado = envioRepository.save(envio);
            log.info("✓ ENVÍO CREADO EXITOSAMENTE:");
            log.info("  ID: {}", envioGuardado.getIdEnvio());
            log.info("  Compra: {}", (envioGuardado.getCompra() != null ? envioGuardado.getCompra().getIdCompra() : "NULL"));
            log.info("  Origen: {}", envioGuardado.getDireccionOrigen());
            log.info("  Destino: {}", envioGuardado.getDireccionDestino());
            log.info("  Distancia: {} km", envioGuardado.getDistanciaKm());
            log.info("  Peso: {} kg", envioGuardado.getPesoTotalKg());
            log.info("  Costo Total: ${}", envioGuardado.getCostoTotal());

            return envioGuardado;

        } catch (Exception e) {
            // En caso de error, se procede a guardar sin cálculo para evitar bloquear el flujo
            log.error("✗ WARNING: Fallo al completar cálculo de envío: {}", e.getMessage());
            log.error("  Guardando envío con datos incompletos...");
            e.printStackTrace();
            return envioRepository.save(envio);
        }
    }

    // Actualizar envío existente
    public Envio actualizar(Integer id, Envio envioActualizado) {
        Optional<Envio> envioExistente = envioRepository.findById(id);

        if (envioExistente.isPresent()) {
            Envio envio = envioExistente.get();

            envio.setCompra(envioActualizado.getCompra());
            envio.setVehiculo(envioActualizado.getVehiculo());
            envio.setTransportista(envioActualizado.getTransportista());
            envio.setEstadoEnvio(envioActualizado.getEstadoEnvio());
            envio.setFechaSalida(envioActualizado.getFechaSalida());
            envio.setFechaEntrega(envioActualizado.getFechaEntrega());
            envio.setNumeroSeguimiento(envioActualizado.getNumeroSeguimiento());

            return envioRepository.save(envio);
        }

        return null;
    }

    // Eliminar un envío
    public boolean eliminar(Integer id) {
        if (envioRepository.existsById(id)) {
            envioRepository.deleteById(id);
            return true;
        }
        return false;
    }
}
