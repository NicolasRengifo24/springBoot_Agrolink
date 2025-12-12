-- Script para verificar compras en la base de datos
-- Ejecutar en MySQL Workbench o línea de comandos

USE springbagrolink_db;

-- 1. Verificar cuántas compras hay
SELECT COUNT(*) as total_compras FROM tb_compras;

-- 2. Ver todas las compras (sin fecha_hora_compra)
SELECT
    id_compra,
    id_cliente,
    subtotal,
    impuestos,
    valor_envio,
    total,
    metodo_pago,
    direccion_entrega
FROM tb_compras
ORDER BY id_compra DESC
LIMIT 20;

-- 3. Ver clientes relacionados
SELECT
    c.id_compra,
    c.total,
    cl.id_usuario as cliente_id,
    u.nombre,
    u.apellido,
    u.correo
FROM tb_compras c
LEFT JOIN tb_clientes cl ON c.id_cliente = cl.id_usuario
LEFT JOIN tb_usuarios u ON cl.id_usuario = u.id_usuario
ORDER BY c.id_compra DESC
LIMIT 20;

-- 4. Si NO hay compras, insertar compras de prueba
-- Usando los IDs de clientes que SÍ existen: 4, 10, 11, 15

-- ¡EJECUTA ESTAS LÍNEAS! (Ya no están comentadas)

-- Insertar compra de prueba 1 (Cliente 4)
INSERT INTO tb_compras (id_cliente, subtotal, impuestos, valor_envio, total, metodo_pago, direccion_entrega)
VALUES (4, 45000.00, 3600.00, 5000.00, 53600.00, 'Efectivo', 'Calle 123 #45-67');

-- Insertar compra de prueba 2 (Cliente 10)
INSERT INTO tb_compras (id_cliente, subtotal, impuestos, valor_envio, total, metodo_pago, direccion_entrega)
VALUES (10, 30000.00, 2400.00, 3000.00, 35400.00, 'Tarjeta', 'Carrera 50 #20-30');

-- Insertar compra de prueba 3 (Cliente 11)
INSERT INTO tb_compras (id_cliente, subtotal, impuestos, valor_envio, total, metodo_pago, direccion_entrega)
VALUES (11, 25000.00, 2000.00, 0.00, 27000.00, 'Efectivo', 'Avenida 80 #100-200');

-- Insertar compra de prueba 4 (Cliente 15)
INSERT INTO tb_compras (id_cliente, subtotal, impuestos, valor_envio, total, metodo_pago, direccion_entrega)
VALUES (15, 60000.00, 4800.00, 8000.00, 72800.00, 'PSE', 'Transversal 45 #88-99');

-- Verificar inserciones con nombres de clientes
SELECT
    c.id_compra,
    c.id_cliente,
    u.nombre,
    u.apellido,
    u.correo,
    c.total,
    c.metodo_pago
FROM tb_compras c
LEFT JOIN tb_clientes cl ON c.id_cliente = cl.id_usuario
LEFT JOIN tb_usuarios u ON cl.id_usuario = u.id_usuario
ORDER BY c.id_compra DESC
LIMIT 10;

