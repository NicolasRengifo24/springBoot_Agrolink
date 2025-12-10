# 🚚 Sistema de Cálculo de Costos de Envío - Agrolink

## 📋 Resumen de Implementación

He implementado un sistema completo de cálculo de costos de envío basado en:
1. **Distancia real** (usando Google Maps API)
2. **Peso de la carga**

## ✅ Cambios Realizados

### 1️⃣ **Modelos Actualizados**

#### `Envio.java`
Nuevos campos agregados:
- `direccionOrigen` y `direccionDestino`: Direcciones completas
- `latitudOrigen`, `longitudOrigen`, `latitudDestino`, `longitudDestino`: Coordenadas GPS
- `distanciaKm`: Distancia calculada en kilómetros
- `pesoTotalKg`: Peso total de la carga
- `costoBase`: Costo por distancia ($2,500 COP/km)
- `costoPeso`: Costo adicional por peso ($50 COP/kg)
- `costoTotal`: Suma de ambos costos
- `tarifaPorKm` y `tarifaPorKg`: Tarifas configurables

Nuevos estados agregados:
- `Buscando_Transporte`
- `Asignado`
- `En_Transito` ⬅️ NUEVO
- `Finalizado`
- `Cancelado` ⬅️ NUEVO

#### `Usuario.java`
Nuevos campos agregados:
- `latitud` y `longitud`: Para geolocalización de usuarios

### 2️⃣ **Nuevo Servicio: `CostoEnvioService`**

Ubicación: `src/main/java/.../services/CostoEnvioService.java`

**Métodos principales:**

```java
// Calcular distancia usando Google Maps
Double calcularDistanciaGoogleMaps(lat1, lng1, lat2, lng2)

// Calcular distancia usando Haversine (alternativa sin API)
Double calcularDistanciaHaversine(lat1, lng1, lat2, lng2)

// Calcular costos completos
Map<String, BigDecimal> calcularCostoEnvio(distanciaKm, pesoKg)

// Obtener coordenadas de una dirección
Map<String, Double> obtenerCoordenadas(direccion)
```

**Tarifas Aplicadas (COP - Pesos Colombianos):**
- 💵 **$2,500 por km** - Competitivo para transporte en Colombia
- 📦 **$50 por kg** - Costo adicional por peso de carga
- 🔽 **$20,000 mínimo** - Costo mínimo de envío
- 🔼 **$500,000 máximo** - Costo máximo de envío

**Ejemplo de Cálculo:**
```
Distancia: 420 km (Bogotá → Medellín)
Peso: 850 kg

Costo Base = 420 km × $2,500 = $1,050,000
Costo Peso = 850 kg × $50 = $42,500
COSTO TOTAL = $1,092,500 COP
```

### 3️⃣ **Script de Migración SQL**

Ubicación: `migration_costos_envio.sql`

**Ejecutar este script para:**
1. Agregar nuevos campos a la BD
2. Configurar coordenadas de ciudades colombianas
3. Crear índices para mejor rendimiento

## 🔑 Cómo Obtener Google Maps API Key (GRATIS)

### Paso 1: Crear Proyecto en Google Cloud
1. Ir a: https://console.cloud.google.com/
2. Crear un nuevo proyecto (ej: "Agrolink-Envios")

### Paso 2: Habilitar APIs
1. Ir a: **APIs & Services** → **Library**
2. Buscar y habilitar:
   - ✅ **Distance Matrix API** (para calcular distancias)
   - ✅ **Geocoding API** (para obtener coordenadas de direcciones)

### Paso 3: Crear API Key
1. Ir a: **APIs & Services** → **Credentials**
2. Click en: **Create Credentials** → **API Key**
3. Copiar la API Key generada

### Paso 4: Configurar en Agrolink
Editar: `src/main/resources/application.properties`
```properties
google.maps.api.key=TU_API_KEY_AQUI
```

### 📊 Límites Gratuitos de Google Maps
- ✅ **40,000 solicitudes/mes GRATIS**
- ✅ Suficiente para ~1,300 cálculos diarios
- ✅ Perfecto para testing y producción pequeña

**IMPORTANTE:** Si no configuras la API Key, el sistema funcionará usando el método alternativo (Haversine) que calcula distancia en línea recta × 1.3 (aproximación de carretera).

## 🚀 Cómo Usar el Sistema

### Ejemplo de Uso en Controlador:

```java
@Autowired
private CostoEnvioService costoEnvioService;

// 1. Calcular distancia
Double distancia = costoEnvioService.calcularDistanciaGoogleMaps(
    4.7110, -74.0721,  // Bogotá
    6.2476, -75.5658   // Medellín
);
// Resultado: ~420 km

// 2. Calcular costos
Map<String, BigDecimal> costos = costoEnvioService.calcularCostoEnvio(
    420.0,  // km
    850.0   // kg
);

// costos.get("costoBase")  → $1,050,000
// costos.get("costoPeso")  → $42,500
// costos.get("costoTotal") → $1,092,500

// 3. Guardar en el envío
envio.setDistanciaKm(distancia);
envio.setPesoTotalKg(850.0);
envio.setCostoBase(costos.get("costoBase"));
envio.setCostoPeso(costos.get("costoPeso"));
envio.setCostoTotal(costos.get("costoTotal"));
```

## 📱 Próximos Pasos - Vista de Detalles con Mapa

Para implementar la vista de detalles del envío con mapa interactivo:

1. **Crear vista HTML** con Leaflet.js (como en seguimiento.html)
2. **Mostrar:**
   - 📍 Punto de origen (Finca del productor)
   - 📍 Punto de destino (Dirección del cliente)
   - 🛣️ Ruta entre ambos puntos
   - 📊 Desglose de costos
   - 📦 Información de la carga

3. **Endpoint necesario:**
```java
@GetMapping("/transportista/envio/detalle/{id}")
public String verDetalleEnvio(@PathVariable Integer id, Model model) {
    Envio envio = envioService.obtenerPorId(id);
    model.addAttribute("envio", envio);
    return "transportista/envio-detalle";
}
```

## 🎯 Beneficios del Sistema

✅ **Transparencia:** Cliente y transportista ven costo justo
✅ **Automatización:** No hay que calcular manualmente
✅ **Precisión:** Usa distancias reales por carretera
✅ **Escalable:** Fácil ajustar tarifas según economía
✅ **Sin API también funciona:** Método alternativo incluido

## 🔧 Mantenimiento

### Ajustar Tarifas
Editar en `CostoEnvioService.java`:
```java
private static final BigDecimal TARIFA_BASE_POR_KM = new BigDecimal("2500");
private static final BigDecimal TARIFA_POR_KG = new BigDecimal("50");
```

### Actualizar Coordenadas de Ciudades
Ejecutar SQL:
```sql
UPDATE tb_usuarios 
SET latitud = 4.7110, longitud = -74.0721 
WHERE ciudad = 'Bogotá';
```

## 📞 Soporte

Si tienes dudas sobre:
- Obtener API Key de Google Maps
- Configurar el sistema
- Ajustar tarifas
- Implementar la vista de detalles

¡Pregúntame y te ayudo!

---

**Desarrollado para Agrolink 🌾**
*Conectando el campo colombiano con tecnología*

