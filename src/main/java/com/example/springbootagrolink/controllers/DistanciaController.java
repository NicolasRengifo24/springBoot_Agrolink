package com.example.springbootagrolink.controllers;

import com.example.springbootagrolink.services.CostoEnvioService;
import com.example.springbootagrolink.services.EnvioService;
import com.example.springbootagrolink.model.Envio;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api")
public class DistanciaController {

    @Autowired
    private CostoEnvioService costoEnvioService;

    @Autowired
    private EnvioService envioService;

    /**
     * Endpoint de prueba para calcular distancia (Haversine) y desglose de costos.
     * Ejemplo: /api/distancia?lat1=4.7110&lon1=-74.0721&lat2=2.4456&lon2=-76.6142&peso=500
     */
    @GetMapping("/distancia")
    public ResponseEntity<Map<String, Object>> calcularDistanciaYCosto(
            @RequestParam("lat1") Double lat1,
            @RequestParam("lon1") Double lon1,
            @RequestParam("lat2") Double lat2,
            @RequestParam("lon2") Double lon2,
            @RequestParam(value = "peso", required = false, defaultValue = "1.0") Double peso
    ) {
        Map<String, Object> resultado = new HashMap<>();

        if (lat1 == null || lon1 == null || lat2 == null || lon2 == null) {
            resultado.put("error", "Parametros lat1, lon1, lat2, lon2 son requeridos");
            return ResponseEntity.badRequest().body(resultado);
        }

        Double distanciaKm = costoEnvioService.calcularDistanciaHaversine(lat1, lon1, lat2, lon2);
        Map<String, BigDecimal> desglose = costoEnvioService.calcularCostoEnvio(distanciaKm, peso);

        resultado.put("distanciaKm", distanciaKm);
        resultado.putAll(desglose);

        return ResponseEntity.ok(resultado);
    }

    /**
     * Recalcula distancia y costos para un Envío existente y lo guarda.
     * Ejemplo: GET /api/envios/1/recalcular
     */
    @GetMapping("/envios/{id}/recalcular")
    public ResponseEntity<Map<String, Object>> recalcularEnvio(@PathVariable Integer id) {
        Optional<Envio> opt = envioService.obtenerPorId(id);
        if (!opt.isPresent()) {
            return ResponseEntity.notFound().build();
        }

        Envio envio = opt.get();
        // Llamamos a crear(envio) porque la lógica de cálculo está implementada allí y hace save
        Envio guardado = envioService.crear(envio);

        Map<String, Object> resultado = new HashMap<>();
        resultado.put("idEnvio", guardado.getIdEnvio());
        resultado.put("distanciaKm", guardado.getDistanciaKm());
        resultado.put("costoBase", guardado.getCostoBase());
        resultado.put("costoPeso", guardado.getCostoPeso());
        resultado.put("costoTotal", guardado.getCostoTotal());

        return ResponseEntity.ok(resultado);
    }

    /**
     * Crea un Envío de prueba con coordenadas y peso dados, calcula y guarda los costos.
     * Ejemplo: GET /api/envios/testcrear?latO=4.7110&lonO=-74.0721&latD=2.4456&lonD=-76.6142&peso=500
     */
    @GetMapping("/envios/testcrear")
    public ResponseEntity<Map<String, Object>> crearEnvioTest(
            @RequestParam("latO") Double latO,
            @RequestParam("lonO") Double lonO,
            @RequestParam("latD") Double latD,
            @RequestParam("lonD") Double lonD,
            @RequestParam(value = "peso", required = false, defaultValue = "1.0") Double peso
    ) {
        Envio envio = new Envio();
        envio.setLatitudOrigen(latO);
        envio.setLongitudOrigen(lonO);
        envio.setLatitudDestino(latD);
        envio.setLongitudDestino(lonD);
        envio.setPesoTotalKg(peso);

        Envio guardado = envioService.crear(envio);

        Map<String, Object> resultado = new HashMap<>();
        resultado.put("idEnvio", guardado.getIdEnvio());
        resultado.put("distanciaKm", guardado.getDistanciaKm());
        resultado.put("costoBase", guardado.getCostoBase());
        resultado.put("costoPeso", guardado.getCostoPeso());
        resultado.put("costoTotal", guardado.getCostoTotal());

        return ResponseEntity.ok(resultado);
    }
}
