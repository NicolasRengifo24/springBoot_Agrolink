-- =====================================================
-- DIAGNÓSTICO Y CORRECCIÓN COMPLETA - FINCAS
-- =====================================================
-- Base de datos: springbagrolink_db
-- =====================================================

USE springbagrolink_db;

-- 1. VERIFICAR SI EXISTEN FINCAS
SELECT 'VERIFICACIÓN: Fincas en la base de datos' AS mensaje;
SELECT COUNT(*) as total_fincas FROM tb_fincas;

-- 2. VER TODAS LAS FINCAS EXISTENTES
SELECT
    f.id_finca,
    f.id_usuario AS productor_id,
    f.nombre_finca,
    f.ciudad,
    f.departamento,
    f.direccion_finca
FROM tb_fincas f
ORDER BY f.id_finca;

-- 3. VERIFICAR PRODUCTORES
SELECT 'VERIFICACIÓN: Productores existentes' AS mensaje;
SELECT
    p.usuario_id,
    u.nombre_usuario
FROM tb_productores p
INNER JOIN tb_usuarios u ON p.usuario_id = u.id_usuario;

-- =====================================================
-- SI NO HAY FINCAS, INSERTAR DATOS DE PRUEBA
-- =====================================================

-- Limpiar tabla de fincas (OPCIONAL - solo si hay problemas)
-- DELETE FROM tb_productos_fincas;
-- DELETE FROM tb_fincas;
-- ALTER TABLE tb_fincas AUTO_INCREMENT = 1;

-- Insertar fincas de prueba
INSERT INTO tb_fincas (id_usuario, nombre_finca, direccion_finca, ciudad, departamento, latitud, longitud) VALUES
-- Productor ID 1
(1, 'Finca Tierra Verde', 'Vereda El Roble, Antioquia', 'Medellín', 'Antioquia', 6.1754, -75.5852),
(1, 'Finca Las Lomas', 'Vereda La Ceiba, Antioquia', 'Rionegro', 'Antioquia', 6.1442, -75.3736),

-- Productor ID 3
(3, 'Finca El Mirador', 'Vereda El Trigal, Valle del Cauca', 'Palmira', 'Valle del Cauca', 3.5394, -76.3036),

-- Productor ID 9
(9, 'Finca La Colina', 'Vereda La Pradera, Huila', 'Neiva', 'Huila', 2.9587, -75.2906),
(9, 'Finca Agua Clara', 'Vereda San Isidro, Huila', 'Campoalegre', 'Huila', 2.6856, -75.3262),

-- Productor ID 13
(13, 'Finca AgroVida', 'Vereda El Cedro, Cauca', 'Popayán', 'Cauca', 2.4583, -76.5578),
(13, 'Finca Brisa Fresca', 'Vereda Montebello, Cauca', 'Timbío', 'Cauca', 2.3511, -76.6844);

-- 4. VERIFICAR QUE SE INSERTARON
SELECT 'RESULTADO: Fincas después de insertar' AS mensaje;
SELECT
    f.id_finca,
    f.id_usuario AS productor_id,
    f.nombre_finca,
    f.ciudad,
    f.departamento
FROM tb_fincas f
ORDER BY f.id_finca;

-- 5. VERIFICAR RELACIÓN PRODUCTOR-FINCAS
SELECT 'RESULTADO: Fincas por Productor' AS mensaje;
SELECT
    p.usuario_id,
    u.nombre_usuario,
    COUNT(f.id_finca) as total_fincas,
    GROUP_CONCAT(f.nombre_finca SEPARATOR ', ') as nombres_fincas
FROM tb_productores p
INNER JOIN tb_usuarios u ON p.usuario_id = u.id_usuario
LEFT JOIN tb_fincas f ON p.usuario_id = f.id_usuario
GROUP BY p.usuario_id, u.nombre_usuario;

