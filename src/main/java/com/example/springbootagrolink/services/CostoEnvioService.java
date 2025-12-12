package com.example.springbootagrolink.services;

import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.beans.factory.annotation.Value;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.Map;

/**
 * Servicio para calcular costos de envío basados en distancia y peso
 * Integrado con Google Maps Distance Matrix API
 */
@Service
public class CostoEnvioService {

    @Value("${google.maps.api.key:}")
    private String googleMapsApiKey;

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    // Tarifas en COP (Pesos Colombianos) — ahora configurables via properties
    @Value("${costo.tarifaBasePorKm:2500}")
    private BigDecimal tarifaBasePorKm;

    @Value("${costo.tarifaPorKg:50}")
    private BigDecimal tarifaPorKg;

    @Value("${costo.minimo:20000}")
    private BigDecimal costoMinimo;

    /**
     * Calcula la distancia entre dos puntos usando Google Maps Distance Matrix API
     *
     * @param origenLat Latitud del origen
     * @param origenLng Longitud del origen
     * @param destinoLat Latitud del destino
     * @param destinoLng Longitud del destino
     * @return Distancia en kilómetros
     */
    public Double calcularDistanciaGoogleMaps(Double origenLat, Double origenLng,
                                               Double destinoLat, Double destinoLng) {
        try {
            if (googleMapsApiKey == null || googleMapsApiKey.isEmpty()) {
                // Si no hay API key, usar cálculo de distancia euclidiana aproximada
                return calcularDistanciaHaversine(origenLat, origenLng, destinoLat, destinoLng);
            }

            String url = String.format(
                "https://maps.googleapis.com/maps/api/distancematrix/json?origins=%s,%s&destinations=%s,%s&key=%s",
                origenLat, origenLng, destinoLat, destinoLng, googleMapsApiKey
            );

            String response = restTemplate.getForObject(url, String.class);
            JsonNode root = objectMapper.readTree(response);

            if ("OK".equals(root.get("status").asText())) {
                JsonNode element = root.get("rows").get(0).get("elements").get(0);

                if ("OK".equals(element.get("status").asText())) {
                    // La distancia viene en metros, convertir a kilómetros
                    int distanciaMetros = element.get("distance").get("value").asInt();
                    return distanciaMetros / 1000.0;
                }
            }

            // Si falla la API, usar método alternativo
            return calcularDistanciaHaversine(origenLat, origenLng, destinoLat, destinoLng);

        } catch (Exception e) {
            System.err.println("Error al calcular distancia con Google Maps: " + e.getMessage());
            // Usar método alternativo en caso de error
            return calcularDistanciaHaversine(origenLat, origenLng, destinoLat, destinoLng);
        }
    }

    /**
     * Calcula la distancia entre dos puntos usando la fórmula de Haversine
     * (Distancia en línea recta sobre la esfera terrestre)
     *
     * @param lat1 Latitud punto 1
     * @param lon1 Longitud punto 1
     * @param lat2 Latitud punto 2
     * @param lon2 Longitud punto 2
     * @return Distancia en kilómetros
     */
    public Double calcularDistanciaHaversine(Double lat1, Double lon1, Double lat2, Double lon2) {
        final int RADIO_TIERRA_KM = 6371;

        double latDistance = Math.toRadians(lat2 - lat1);
        double lonDistance = Math.toRadians(lon2 - lon1);

        double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);

        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        double distancia = RADIO_TIERRA_KM * c;

        // Ajuste: Multiplicar por 1.3 para aproximar distancia por carretera
        // (las carreteras no son líneas rectas)
        return distancia * 1.3;
    }

    /**
     * Calcula el costo total del envío basado en distancia y peso
     *
     * @param distanciaKm Distancia en kilómetros
     * @param pesoKg Peso en kilogramos
     * @return Map con el desglose de costos
     */
    public Map<String, BigDecimal> calcularCostoEnvio(Double distanciaKm, Double pesoKg) {
        Map<String, BigDecimal> costos = new HashMap<>();

        // Validaciones
        if (distanciaKm == null || distanciaKm <= 0) {
            distanciaKm = 1.0; // Mínimo 1 km
        }
        if (pesoKg == null || pesoKg <= 0) {
            pesoKg = 1.0; // Mínimo 1 kg
        }

        // Calcular costo base (por distancia)
        BigDecimal costoBase = tarifaBasePorKm.multiply(BigDecimal.valueOf(distanciaKm))
                .setScale(2, RoundingMode.HALF_UP);

        // Calcular costo por peso
        BigDecimal costoPeso = tarifaPorKg.multiply(BigDecimal.valueOf(pesoKg))
                .setScale(2, RoundingMode.HALF_UP);

        // Calcular total
        BigDecimal costoTotal = costoBase.add(costoPeso);

        // Aplicar costo mínimo (siempre aplicará el mínimo si corresponde)
        if (costoTotal.compareTo(costoMinimo) < 0) {
            costoTotal = costoMinimo;
        }

        costos.put("costoBase", costoBase);
        costos.put("costoPeso", costoPeso);
        costos.put("costoTotal", costoTotal);
        costos.put("tarifaPorKm", tarifaBasePorKm);
        costos.put("tarifaPorKg", tarifaPorKg);

        return costos;
    }

    /**
     * Obtiene las coordenadas GPS de una dirección usando Google Geocoding API
     *
     * @param direccion Dirección completa
     * @return Map con latitud y longitud
     */
    public Map<String, Double> obtenerCoordenadas(String direccion) {
        Map<String, Double> coordenadas = new HashMap<>();

        try {
            if (googleMapsApiKey == null || googleMapsApiKey.isEmpty()) {
                // Retornar coordenadas por defecto (Bogotá, Colombia)
                coordenadas.put("latitud", 4.7110);
                coordenadas.put("longitud", -74.0721);
                return coordenadas;
            }

            String url = String.format(
                "https://maps.googleapis.com/maps/api/geocode/json?address=%s&key=%s",
                direccion.replace(" ", "+"), googleMapsApiKey
            );

            String response = restTemplate.getForObject(url, String.class);
            JsonNode root = objectMapper.readTree(response);

            if ("OK".equals(root.get("status").asText())) {
                JsonNode location = root.get("results").get(0).get("geometry").get("location");
                coordenadas.put("latitud", location.get("lat").asDouble());
                coordenadas.put("longitud", location.get("lng").asDouble());
            } else {
                // Coordenadas por defecto si falla
                coordenadas.put("latitud", 4.7110);
                coordenadas.put("longitud", -74.0721);
            }

        } catch (Exception e) {
            System.err.println("Error al obtener coordenadas: " + e.getMessage());
            // Coordenadas por defecto
            coordenadas.put("latitud", 4.7110);
            coordenadas.put("longitud", -74.0721);
        }

        return coordenadas;
    }

    /**
     * Formatea un valor monetario a COP
     *
     * @param valor Valor a formatear
     * @return String formateado
     */
    public String formatearCOP(BigDecimal valor) {
        return String.format("$%,.0f COP", valor);
    }
}
