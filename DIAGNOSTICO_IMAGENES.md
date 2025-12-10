# 🔍 DIAGNÓSTICO: Problemas con Imágenes de Productos

## ✅ CORRECCIONES REALIZADAS:

### 1. **Error Crítico Corregido: Sintaxis Thymeleaf**
**Antes (INCORRECTO):**
```html
th:src="@{${producto.imagenesProducto != null and !#lists.isEmpty(...)}}"
```

**Ahora (CORRECTO):**
```html
th:src="${producto.imagenesProducto != null and !producto.imagenesProducto.isEmpty() ? producto.imagenesProducto[0].urlImagen : '/imag/placeholder.jpg'}"
```

**Por qué fallaba:** 
- `@{${...}}` es sintaxis INVÁLIDA en Thymeleaf
- `@{...}` solo acepta URLs estáticas o variables simples
- Para expresiones condicionales, usa solo `${...}`

### 2. **CSS Mejorado para Evitar Distorsión**
```css
table img.thumb {
  width: 58px;
  height: 58px;
  border-radius: 10px;
  object-fit: cover;        /* Recorta proporcionalmente */
  object-position: center;   /* Centra el recorte */
  border: 1px solid #eee;
  display: block;            /* Evita espacios extra */
}
```

### 3. **Manejo de Errores de Carga Mejorado**
```html
onerror="this.onerror=null; this.src='/imag/placeholder.jpg';"
```
- `this.onerror=null` evita loops infinitos si el placeholder también falla

---

## 🔍 PASOS PARA VERIFICAR SI LAS IMÁGENES CARGAN:

### **Paso 1: Verifica las URLs en la Base de Datos**
Ejecuta esta consulta SQL:

```sql
SELECT 
    p.id_producto,
    p.nombre_producto,
    ip.url_imagen,
    ip.es_principal
FROM tb_productos p
LEFT JOIN tb_imagenes_productos ip ON p.id_producto = ip.id_producto
ORDER BY p.id_producto;
```

**Deberías ver algo como:**
```
id_producto | nombre_producto | url_imagen                        | es_principal
------------|-----------------|-----------------------------------|-------------
1           | Tomate          | /images/products/abc123.jpg       | 1
2           | Papa            | /images/products/def456.jpg       | 1
3           | Cebolla         | NULL                              | NULL
```

### **Paso 2: Verifica que los Archivos Existan**

Las imágenes deben estar en **AMBAS** ubicaciones:

1. **Desarrollo:**
   ```
   src/main/resources/static/images/products/
   ```

2. **Producción (target):**
   ```
   target/classes/static/images/products/
   ```

**Comando para verificar (PowerShell/CMD):**
```powershell
# Verifica archivos en desarrollo
dir "src\main\resources\static\images\products\"

# Verifica archivos en target
dir "target\classes\static\images\products\"
```

### **Paso 3: Verifica URLs en el Navegador**

Cuando la app esté corriendo en `http://localhost:8080`, prueba acceder directamente:

```
http://localhost:8080/images/products/nombre-archivo.jpg
```

Si ves 404, el archivo NO está en la carpeta correcta.

---

## 🚨 PROBLEMAS COMUNES Y SOLUCIONES:

### **Problema 1: URL con doble barra `/images/products/`**
**Síntoma:** La imagen tiene URL como `//images/products/abc.jpg`
**Solución:** 
```sql
-- Corregir URLs con doble barra
UPDATE tb_imagenes_productos 
SET url_imagen = REPLACE(url_imagen, '//', '/') 
WHERE url_imagen LIKE '//%';
```

### **Problema 2: URL sin barra inicial**
**Síntoma:** URL guardada como `images/products/abc.jpg` (sin `/` inicial)
**Solución:**
```sql
-- Añadir barra inicial si falta
UPDATE tb_imagenes_productos 
SET url_imagen = CONCAT('/', url_imagen) 
WHERE url_imagen NOT LIKE '/%' AND url_imagen IS NOT NULL;
```

### **Problema 3: Archivos no existen**
**Síntoma:** URLs correctas pero imágenes no cargan
**Solución:**
- Copia las imágenes a `src/main/resources/static/images/products/`
- Reinicia la aplicación (Spring Boot copiará a `target/...`)
- O copia manualmente a ambas carpetas

### **Problema 4: Placeholder no existe**
**Síntoma:** Ni la imagen del producto ni el placeholder cargan
**Solución:**
```bash
# Verifica que exista el placeholder
# Debe estar en: src/main/resources/static/imag/placeholder.jpg
```

Si no existe, crea uno o usa otra ruta:
```html
th:src="${... : '/images/placeholder.jpg'}"
```

---

## 🔧 SCRIPTS DE CORRECCIÓN RÁPIDA:

### **Script 1: Normalizar URLs en BD**
```sql
-- Limpia y normaliza todas las URLs de imágenes
UPDATE tb_imagenes_productos
SET url_imagen = CONCAT('/', TRIM(BOTH '/' FROM url_imagen))
WHERE url_imagen IS NOT NULL;
```

### **Script 2: Verificar productos sin imágenes**
```sql
-- Encuentra productos sin ninguna imagen
SELECT 
    p.id_producto,
    p.nombre_producto,
    COUNT(ip.id_imagen) as total_imagenes
FROM tb_productos p
LEFT JOIN tb_imagenes_productos ip ON p.id_producto = ip.id_producto
GROUP BY p.id_producto
HAVING total_imagenes = 0;
```

### **Script 3: Eliminar referencias a imágenes inexistentes**
```sql
-- Si quieres limpiar referencias a archivos que ya no existen
-- (CUIDADO: Esto BORRA registros de la BD)
DELETE FROM tb_imagenes_productos
WHERE url_imagen IS NULL OR url_imagen = '';
```

---

## ✅ CHECKLIST FINAL:

- [ ] Ejecutar consulta SQL para ver URLs guardadas
- [ ] Verificar que archivos existan en `src/main/resources/static/images/products/`
- [ ] Verificar que archivos existan en `target/classes/static/images/products/`
- [ ] Probar acceso directo a imagen en navegador: `http://localhost:8080/images/products/...`
- [ ] Verificar que placeholder existe en `/imag/placeholder.jpg`
- [ ] Reiniciar aplicación Spring Boot
- [ ] Refrescar navegador (Ctrl+F5 para limpiar caché)
- [ ] Verificar consola del navegador (F12) para errores 404

---

## 📊 RESULTADO ESPERADO:

Después de las correcciones, deberías ver:
1. ✅ Imágenes de productos cargando correctamente (58x58px, redondeadas)
2. ✅ Placeholder mostrándose para productos sin imagen
3. ✅ Sin distorsión (object-fit: cover mantiene proporción)
4. ✅ Sin errores en consola del navegador

---

## 🆘 SI AÚN NO FUNCIONA:

Envíame:
1. Resultado de la consulta SQL (primeros 3 registros)
2. Captura de pantalla del error en navegador (F12 > Console)
3. Output de `dir src\main\resources\static\images\products\`

