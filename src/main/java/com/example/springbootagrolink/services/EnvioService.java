package com.example.springbootagrolink.services;

import com.example.springbootagrolink.model.Envio;
import com.example.springbootagrolink.model.DetalleCompra;
import com.example.springbootagrolink.model.ProductoFinca;
import com.example.springbootagrolink.model.Producto;
import com.example.springbootagrolink.model.Finca;
import com.example.springbootagrolink.repository.EnvioRepository;
import com.example.springbootagrolink.repository.DetalleCompraRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.math.BigDecimal;
import java.util.Map;

@Service
public class EnvioService {

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
        // Intentar completar coordenadas de origen a partir de la compra si no vienen
        try {
            if ((envio.getLatitudOrigen() == null || envio.getLongitudOrigen() == null)
                    && envio.getCompra() != null && envio.getCompra().getIdCompra() != null) {
                List<DetalleCompra> detalles = detalleCompraRepository.findByCompra_IdCompra(envio.getCompra().getIdCompra());
                if (detalles != null && !detalles.isEmpty()) {
                    // Tomar la primera asociación ProductoFinca disponible como origen
                    DetalleCompra detalle = detalles.get(0);
                    Producto producto = detalle.getProducto();
                    if (producto != null && producto.getProductoFincas() != null && !producto.getProductoFincas().isEmpty()) {
                        ProductoFinca pf = producto.getProductoFincas().get(0);
                        if (pf != null) {
                            Finca finca = pf.getFinca();
                            if (finca != null) {
                                envio.setLatitudOrigen(finca.getLatitud());
                                envio.setLongitudOrigen(finca.getLongitud());
                                if (envio.getDireccionOrigen() == null || envio.getDireccionOrigen().isEmpty()) {
                                    envio.setDireccionOrigen(finca.getDireccionFinca());
                                }
                            }
                        }
                    }
                }
            }

            // Si no hay coordenadas de destino pero sí hay dirección, intentar geocoding
            if ((envio.getLatitudDestino() == null || envio.getLongitudDestino() == null)
                    && envio.getDireccionDestino() != null && !envio.getDireccionDestino().isEmpty()) {
                Map<String, Double> coords = costoEnvioService.obtenerCoordenadas(envio.getDireccionDestino());
                if (coords != null) {
                    envio.setLatitudDestino(coords.get("latitud"));
                    envio.setLongitudDestino(coords.get("longitud"));
                }
            }

            // Calcular peso total si no viene
            if (envio.getPesoTotalKg() == null || envio.getPesoTotalKg() <= 0) {
                Double pesoTotal = 0.0;
                if (envio.getCompra() != null && envio.getCompra().getIdCompra() != null) {
                    List<DetalleCompra> detalles = detalleCompraRepository.findByCompra_IdCompra(envio.getCompra().getIdCompra());
                    if (detalles != null && !detalles.isEmpty()) {
                        for (DetalleCompra d : detalles) {
                            if (d.getProducto() != null && d.getProducto().getPesoKg() != null) {
                                BigDecimal pesoProducto = d.getProducto().getPesoKg();
                                int cantidad = d.getCantidad() != null ? d.getCantidad() : 1;
                                pesoTotal += pesoProducto.doubleValue() * cantidad;
                            }
                        }
                    }
                }
                if (pesoTotal <= 0) pesoTotal = 1.0; // valor mínimo
                envio.setPesoTotalKg(pesoTotal);
            }

            // Calcular distancia si hay coordenadas
            if (envio.getLatitudOrigen() != null && envio.getLongitudOrigen() != null
                    && envio.getLatitudDestino() != null && envio.getLongitudDestino() != null) {
                Double distancia = costoEnvioService.calcularDistanciaHaversine(envio.getLatitudOrigen(), envio.getLongitudOrigen(),
                        envio.getLatitudDestino(), envio.getLongitudDestino());
                envio.setDistanciaKm(distancia);

                // Calcular costos
                Map<String, BigDecimal> desglose = costoEnvioService.calcularCostoEnvio(distancia, envio.getPesoTotalKg());
                if (desglose != null) {
                    envio.setCostoBase(desglose.getOrDefault("costoBase", BigDecimal.ZERO));
                    envio.setCostoPeso(desglose.getOrDefault("costoPeso", BigDecimal.ZERO));
                    envio.setCostoTotal(desglose.getOrDefault("costoTotal", BigDecimal.ZERO));
                    // Si la entidad tiene campos para tarifas, intentar setearlos
                    if (desglose.get("tarifaPorKm") != null) envio.setTarifaPorKm(desglose.get("tarifaPorKm"));
                    if (desglose.get("tarifaPorKg") != null) envio.setTarifaPorKg(desglose.get("tarifaPorKg"));
                }
            }

        } catch (Exception e) {
            // En caso de error, se procede a guardar sin cálculo para evitar bloquear el flujo
            System.err.println("Warning: fallo al completar cálculo de envío: " + e.getMessage());
        }

        return envioRepository.save(envio);
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
