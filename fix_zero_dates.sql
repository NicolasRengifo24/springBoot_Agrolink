-- =========================================
-- Script para corregir fechas cero en MySQL
-- Base de datos: springbagrolink_db
-- =========================================

USE springbagrolink_db;

-- 1. Verificar cuántos registros tienen fechas cero
SELECT COUNT(*) AS registros_con_fecha_cero
FROM tb_compras
WHERE fecha_hora_compra = '0000-00-00 00:00:00'
   OR fecha_hora_compra IS NULL;

-- 2. Mostrar los registros afectados
SELECT id_compra, id_cliente, fecha_hora_compra, total
FROM tb_compras
WHERE fecha_hora_compra = '0000-00-00 00:00:00'
   OR fecha_hora_compra IS NULL
LIMIT 10;

-- 3. OPCIÓN A: Establecer fecha actual para registros con fecha cero
-- (Descomenta para ejecutar)
/*
UPDATE tb_compras
SET fecha_hora_compra = NOW()
WHERE fecha_hora_compra = '0000-00-00 00:00:00'
   OR fecha_hora_compra IS NULL;
*/

-- 4. OPCIÓN B: Establecer NULL para registros con fecha cero
-- (Descomenta para ejecutar - requiere que la columna acepte NULL)
/*
UPDATE tb_compras
SET fecha_hora_compra = NULL
WHERE fecha_hora_compra = '0000-00-00 00:00:00';
*/

-- 5. OPCIÓN C: Establecer una fecha específica (ejemplo: inicio del año)
-- (Descomenta para ejecutar)
/*
UPDATE tb_compras
SET fecha_hora_compra = '2024-01-01 00:00:00'
WHERE fecha_hora_compra = '0000-00-00 00:00:00'
   OR fecha_hora_compra IS NULL;
*/

-- 6. Verificar que se corrigieron
SELECT COUNT(*) AS registros_corregidos
FROM tb_compras
WHERE fecha_hora_compra IS NOT NULL
  AND fecha_hora_compra != '0000-00-00 00:00:00';

-- 7. Modificar la columna para no permitir valores NULL en el futuro (OPCIONAL)
-- (Descomenta para ejecutar - solo después de limpiar los datos)
/*
ALTER TABLE tb_compras
MODIFY COLUMN fecha_hora_compra DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP;
*/

-- =========================================
-- RECOMENDACIÓN
-- =========================================
-- Ejecuta OPCIÓN A (establecer NOW()) si quieres que las compras
-- sin fecha tengan la fecha actual como referencia.
--
-- Esto permitirá que la gráfica de ventas funcione correctamente.
-- =========================================

