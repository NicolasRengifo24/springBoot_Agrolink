-- =====================================================
-- SCRIPT SQL COMPLETO - AGROLINK
-- Incluye inserts con coordenadas GPS de fincas
-- =====================================================
-- INSTRUCCIONES:
-- 1. Eliminar la base de datos: DROP DATABASE IF EXISTS springbagrolink_db;
-- 2. Ejecutar Spring Boot (creará las tablas automáticamente)
-- 3. Ejecutar este script para insertar los datos
-- =====================================================

USE springbagrolink_db;

-- =====================================================
-- 1. USUARIOS
-- =====================================================
INSERT INTO tb_usuarios (nombre, nombre_usuario, contrasena_usuario, apellido, correo, ciudad, departamento, direccion, cedula, telefono, latitud, longitud) VALUES
('Carlos','carlosagricola','claveSegura123','Gómez','c.gomez@email.com','Medellín','Antioquia','Carrera 45 #12-34','123456789012','3101111111', 6.2476, -75.5658),
('María','mariacampesina','password1234','Rodríguez','m.rodriguez@email.com','Bogotá','Cundinamarca','Calle 23 #45-67','234567890123','3202222222', 4.7110, -74.0721),
('Juan','juanproductor','juanito2023','Pérez','j.perez@email.com','Cali','Valle del Cauca','Avenida 6N #23-45','345678901234','3003333333', 3.4516, -76.5320),
('Ana','anitacliente','anaclave456','López','a.lopez@email.com','Barranquilla','Atlántico','Carrera 54 #78-90','456789012345','3014444444', 10.9685, -74.7813),
('Pedro','pedrotransport','transporte2023','Martínez','p.martinez@email.com','Pereira','Risaralda','Calle 30 #12-34','567890123456','3025555555', 4.8133, -75.6961),
('Luisa','luisaasesora','luisapass789','García','l.garcia@email.com','Manizales','Caldas','Carrera 22 #33-44','678901234567','3036666666', 5.0689, -75.5174),
('Jorge','jorgeadmin','adminjorge12','Fernández','j.fernandez@email.com','Armenia','Quindío','Avenida Bolívar #15-25','789012345678','3047777777', 4.5339, -75.6811),
('Sofía','sofiadmin','sofiaadmin456','Hernández','s.hernandez@email.com','Ibagué','Tolima','Calle 18 #20-30','890123456789','3058888888', 4.4389, -75.2322),
('Diego','diegoproductor','diego2023pass','Díaz','d.diaz@email.com','Neiva','Huila','Carrera 5 #10-15','901234567890','3069999999', 2.9273, -75.2819),
('Camila','camilacliente','camipass789','Vargas','c.vargas@email.com','Villavicencio','Meta','Avenida 40 #50-60','012345678901','3070000000', 4.1420, -73.6266),
('Nicolas','clienteNicolas','nicopass789','Mendez','mendez@email.com','Villavicencio','Meta','Avenida 67 #23-5','1007846932','3111111111', 4.1420, -73.6266),
('Pepe','MaquinistaPepe','pepepass769','Mujica','pepe@email.com','Madrid','Cundinamarca','cra 77 #23-5','1887846932','3122222222', 4.7332, -74.2650),
('Laura','lauracafe','laurapass2024','Ramírez','l.ramirez@email.com','Popayán','Cauca','Cra 12 #8-23','345999888777','3133333333', 2.4419, -76.6063),
('Mateo','mateotransport','mateo2025','Suárez','m.suarez@email.com','Tunja','Boyacá','Cl 10 #22-14','234888777666','3144444444', 5.5353, -73.3678),
('Valentina','valeclient','valepass123','Castro','v.castro@email.com','Santa Marta','Magdalena','Av 1 #20-45','123777666555','3155555555', 11.2408, -74.1990);

-- =====================================================
-- 2. CALIFICACIONES
-- =====================================================
INSERT INTO tb_calificacion (puntaje, promedio) VALUES
(4.5,4.3),(4.8,4.6),(3.9,4.0),(4.2,4.1),(4.7,4.5),
(4.0,4.2),(4.9,4.7),(3.8,4.0),(4.3,4.2),(4.6,4.4),
(2.2,2.4),(5.0,5.0);

-- =====================================================
-- 3. CATEGORÍAS DE PRODUCTOS
-- =====================================================
INSERT INTO tb_categorias_productos (nombre_categoria) VALUES
('Lácteos'),('Cárnicos'),('Frutas'),('Verduras'),('Granos'),('Café'),
('Tubérculos'),('Aceites'),('Semillas'),('Hierbas');

-- =====================================================
-- 4. PRODUCTORES
-- =====================================================
INSERT INTO tb_productores (id_usuario,id_calificacion,tipo_cultivo) VALUES
(1,1,'Monocultivo'),   -- Carlos (Usuario 1 - Medellín)
(3,3,'Policultivo'),   -- Juan (Usuario 3 - Cali)
(9,9,'Huerta'),        -- Diego (Usuario 9 - Neiva)
(13,2,'Cultivo_Urbano'); -- Laura (Usuario 13 - Popayán)

-- =====================================================
-- 5. CLIENTES
-- =====================================================
INSERT INTO tb_clientes (id_usuario,id_calificacion,preferencias) VALUES
(4,4,'Prefiere productos orgánicos'),
(10,10,'Sin preferencias'),
(11,11,'Productos locales'),
(15,6,'Compra frutas y verduras frescas');

-- =====================================================
-- 6. TRANSPORTISTAS
-- =====================================================
INSERT INTO tb_transportistas (id_usuario,id_calificacion,zonas_entrega) VALUES
(5,5,'Antioquia, Risaralda, Caldas'),
(14,2,'Boyacá, Santander, Meta');

-- =====================================================
-- 7. ADMINISTRADORES
-- =====================================================
INSERT INTO tb_administradores (id_usuario,privilegios_admin) VALUES
(7,'Super administrador con acceso total'),
(8,'Administrador de usuarios y productos');

-- =====================================================
-- 8. ASESORES
-- =====================================================
INSERT INTO tb_asesores (id_usuario,id_calificacion,tipo_asesoria) VALUES
(6,6,'Asesor_Agricola'),
(2,2,'Veterinario'),
(12,12,'Maquinista');

-- =====================================================
-- 9. FINCAS CON COORDENADAS GPS REALES
-- =====================================================
-- IMPORTANTE: Ahora las fincas tienen coordenadas GPS precisas
-- El transportista usará estas coordenadas para calcular distancia y costo

INSERT INTO tb_fincas (id_usuario, nombre_finca, direccion_finca, ciudad, departamento, latitud, longitud) VALUES
-- Fincas del Productor 1 (Carlos - Antioquia)
(1, 'Finca Tierra Verde', 'Vereda El Roble, Antioquia', 'Medellín', 'Antioquia', 6.1754, -75.5852),
(1, 'Finca Las Lomas', 'Vereda La Ceiba, Antioquia', 'Rionegro', 'Antioquia', 6.1442, -75.3736),

-- Fincas del Productor 3 (Juan - Valle del Cauca)
(3, 'Finca El Mirador', 'Vereda El Trigal, Valle del Cauca', 'Palmira', 'Valle del Cauca', 3.5394, -76.3036),

-- Fincas del Productor 9 (Diego - Huila)
(9, 'Finca La Colina', 'Vereda La Pradera, Huila', 'Neiva', 'Huila', 2.9587, -75.2906),
(9, 'Finca Agua Clara', 'Vereda San Isidro, Huila', 'Campoalegre', 'Huila', 2.6856, -75.3262),

-- Fincas del Productor 13 (Laura - Cauca)
(13, 'Finca AgroVida', 'Vereda El Cedro, Cauca', 'Popayán', 'Cauca', 2.4583, -76.5578),
(13, 'Finca Brisa Fresca', 'Vereda Montebello, Cauca', 'Timbío', 'Cauca', 2.3511, -76.6844);

-- =====================================================
-- 10. PRODUCTOS
-- =====================================================
INSERT INTO tb_productos (id_usuario,id_categoria,id_calificacion,precio,nombre_producto,descripcion_producto,stock,peso_kg) VALUES
-- Productos del Productor 1 (Carlos)
(1,6,1,12000,'Café especial','Café de alta calidad cultivado en Antioquia',500,1.0),
(1,4,1,1800,'Tomate chonto','Tomate cultivado sin pesticidas en Antioquia',600,0.3),

-- Productos del Productor 3 (Juan)
(3,3,3,2500,'Plátano hartón','Plátano cultivado en el Valle del Cauca',800,1.2),
(3,5,3,4000,'Frijol rojo','Frijol de excelente calidad del Valle',400,0.2),

-- Productos del Productor 9 (Diego)
(9,1,9,3500,'Queso campesino','Queso fresco producido en Huila',300,0.5),
(9,7,9,1200,'Papa criolla','Papa de producción local en Huila',900,0.4),

-- Productos del Productor 13 (Laura)
(13,2,2,15000,'Carne de res','Carne fresca certificada del Cauca',250,1.5);

-- =====================================================
-- 11. IMÁGENES DE PRODUCTOS
-- =====================================================
INSERT INTO tb_imagenes_productos (id_producto, url_imagen, es_principal) VALUES
(1, 'images/products/cafe_especial_1.webp', TRUE),
(1, 'images/products/cafe_especial_2.jpg', FALSE),
(5, 'images/products/queso_campesino.jpg', TRUE),
(2, 'images/products/tomate_chonto.jpg', TRUE),
(6, 'images/products/papa_criolla.jpg', TRUE);

-- =====================================================
-- 12. PRODUCTOS_FINCAS
-- Cada producto está correctamente asignado a las fincas de su productor
-- =====================================================
INSERT INTO tb_productos_fincas (id_finca,id_producto,cantidad_produccion,fecha_cosecha) VALUES
-- Finca 1 (Tierra Verde - Carlos) produce: Café y Tomate
(1, 1, 300.00, '2025-06-01'),  -- Café especial
(1, 2, 150.00, '2025-06-10'),  -- Tomate chonto

-- Finca 2 (Las Lomas - Carlos) produce: Café y Tomate
(2, 1, 200.00, '2025-06-15'),  -- Café especial
(2, 2, 100.00, '2025-06-20'),  -- Tomate chonto

-- Finca 3 (El Mirador - Juan) produce: Plátano y Frijol
(3, 3, 400.00, '2025-05-15'),  -- Plátano hartón
(3, 4, 200.00, '2025-07-05'),  -- Frijol rojo

-- Finca 4 (La Colina - Diego) produce: Queso y Papa
(4, 5, 150.00, '2025-06-05'),  -- Queso campesino
(4, 6, 450.00, '2025-05-20'),  -- Papa criolla

-- Finca 5 (Agua Clara - Diego) produce: Queso y Papa
(5, 5, 150.00, '2025-06-12'),  -- Queso campesino
(5, 6, 450.00, '2025-05-25'),  -- Papa criolla

-- Finca 6 (AgroVida - Laura) produce: Carne de res
(6, 7, 125.00, '2025-07-01'),  -- Carne de res

-- Finca 7 (Brisa Fresca - Laura) produce: Carne de res
(7, 7, 125.00, '2025-07-10');  -- Carne de res

-- =====================================================
-- 13. COMPRAS CON COORDENADAS DE DESTINO
-- =====================================================
INSERT INTO tb_compras (id_cliente,subtotal,impuestos,total,metodo_pago,direccion_entrega) VALUES
(4,24000,2000,26000,'Tarjeta débito','Calle 23 #45-67, Bogotá'),
(10,7000,500,7500,'Efectivo','Avenida 40 #50-60, Villavicencio'),
(11,15000,1200,16200,'Nequi','Avenida 67 #23-5, Villavicencio'),
(15,18000,1440,19440,'Tarjeta crédito','Av 1 #20-45, Santa Marta');

-- =====================================================
-- 14. DETALLES DE COMPRAS
-- =====================================================
INSERT INTO tb_detalles_compra (id_compra,id_producto,cantidad,precio_unitario,subtotal) VALUES
(1,1,2,12000,24000),
(2,5,2,3500,7000),
(3,2,5,1800,9000),
(4,6,15,1200,18000);

-- =====================================================
-- 15. VEHÍCULOS
-- =====================================================
INSERT INTO tb_vehiculos (id_transportista,tipo_vehiculo,capacidad_carga,documento_propiedad,placa_vehiculo) VALUES
(5,'Camión',5000,'Documento propiedad 123','ABC123'),
(5,'Camioneta',2000,'Documento propiedad 456','DEF456'),
(14,'Camión',4000,'Documento propiedad 789','GHI789');

-- =====================================================
-- 16. ENVÍOS CON DATOS DE COSTO
-- =====================================================
-- Los envíos ahora incluyen datos calculados de distancia y costo
-- La distancia se calcula desde la FINCA del producto hasta el CLIENTE

-- ENVÍO 1: Finca 1 (Medellín, Antioquia) → Cliente 4 (Bogotá)
-- Producto: Café (2 kg)
-- Distancia aproximada: 420 km
INSERT INTO tb_envios (
    id_compra,
    estado_envio,
    direccion_origen,
    direccion_destino,
    latitud_origen,
    longitud_origen,
    latitud_destino,
    longitud_destino,
    distancia_km,
    peso_total_kg,
    tarifa_por_km,
    tarifa_por_kg,
    costo_base,
    costo_peso,
    costo_total
) VALUES (
    1,
    'Buscando_Transporte',
    'Finca Tierra Verde, Vereda El Roble, Medellín, Antioquia',
    'Calle 23 #45-67, Bogotá',
    6.1754,
    -75.5852,
    4.7110,
    -74.0721,
    420.0,
    2.0,
    2500.00,
    50.00,
    1050000.00,
    100.00,
    1050100.00
);

-- ENVÍO 2: Finca 4 (Neiva, Huila) → Cliente 10 (Villavicencio)
-- Producto: Queso (1 kg)
-- Distancia aproximada: 180 km
INSERT INTO tb_envios (
    id_compra,
    estado_envio,
    direccion_origen,
    direccion_destino,
    latitud_origen,
    longitud_origen,
    latitud_destino,
    longitud_destino,
    distancia_km,
    peso_total_kg,
    tarifa_por_km,
    tarifa_por_kg,
    costo_base,
    costo_peso,
    costo_total
) VALUES (
    2,
    'Buscando_Transporte',
    'Finca La Colina, Vereda La Pradera, Neiva, Huila',
    'Avenida 40 #50-60, Villavicencio',
    2.9587,
    -75.2906,
    4.1420,
    -73.6266,
    180.0,
    1.0,
    2500.00,
    50.00,
    450000.00,
    50.00,
    450050.00
);

-- ENVÍO 3: Finca 1 (Medellín, Antioquia) → Cliente 11 (Villavicencio)
-- Producto: Tomate (1.5 kg)
-- Distancia aproximada: 520 km
INSERT INTO tb_envios (
    id_compra,
    estado_envio,
    direccion_origen,
    direccion_destino,
    latitud_origen,
    longitud_origen,
    latitud_destino,
    longitud_destino,
    distancia_km,
    peso_total_kg,
    tarifa_por_km,
    tarifa_por_kg,
    costo_base,
    costo_peso,
    costo_total
) VALUES (
    3,
    'Buscando_Transporte',
    'Finca Tierra Verde, Vereda El Roble, Medellín, Antioquia',
    'Avenida 67 #23-5, Villavicencio',
    6.1754,
    -75.5852,
    4.1420,
    -73.6266,
    520.0,
    1.5,
    2500.00,
    50.00,
    1300000.00,
    75.00,
    1300075.00
);

-- ENVÍO 4: Finca 4 (Neiva, Huila) → Cliente 15 (Santa Marta)
-- Producto: Papa (6 kg)
-- Distancia aproximada: 780 km
INSERT INTO tb_envios (
    id_compra,
    estado_envio,
    direccion_origen,
    direccion_destino,
    latitud_origen,
    longitud_origen,
    latitud_destino,
    longitud_destino,
    distancia_km,
    peso_total_kg,
    tarifa_por_km,
    tarifa_por_kg,
    costo_base,
    costo_peso,
    costo_total
) VALUES (
    4,
    'Buscando_Transporte',
    'Finca La Colina, Vereda La Pradera, Neiva, Huila',
    'Av 1 #20-45, Santa Marta',
    2.9587,
    -75.2906,
    11.2408,
    -74.1990,
    780.0,
    6.0,
    2500.00,
    50.00,
    1950000.00,
    300.00,
    1950300.00
);

-- =====================================================
-- 17. MÁQUINAS
-- =====================================================
INSERT INTO tb_maquinas (id_asesor,tipo_maquina,documento_propiedad,modelo) VALUES
(12,'Tractor','Documento tractor 101','John Deere 5100'),
(12,'Cosechadora','Documento cosechadora 202','New Holland CX8080');

-- =====================================================
-- 18. SERVICIOS
-- =====================================================
INSERT INTO tb_servicios (id_asesor,descripcion,estado) VALUES
(6,'Asesoría en cultivo de café','Activo'),
(6,'Análisis de suelos','Activo'),
(2,'Asesoría en ganadería','Inactivo'),
(12,'Alquiler de maquinaria y servicios de operación','Activo');

-- =====================================================
-- 19. CERTIFICADOS
-- =====================================================
INSERT INTO tb_certificados (id_usuario,tipo_certificado,descripcion_cert,fecha_expedicion) VALUES
(6,'Ingeniero Agrónomo','Título profesional','2018-06-15'),
(6,'Especialista en suelos','Posgrado','2020-11-20'),
(2,'Técnico pecuario','Certificación SENA','2019-03-10'),
(12,'Técnico Operario Agrícola','SENA CBA','2020-03-29');

-- =====================================================
-- RESUMEN DE DATOS INSERTADOS
-- =====================================================
-- ✅ 15 Usuarios con coordenadas GPS
-- ✅ 12 Calificaciones
-- ✅ 10 Categorías de productos
-- ✅ 4 Productores
-- ✅ 4 Clientes
-- ✅ 2 Transportistas
-- ✅ 2 Administradores
-- ✅ 3 Asesores
-- ✅ 7 Fincas CON COORDENADAS GPS REALES
-- ✅ 7 Productos
-- ✅ 5 Imágenes de productos
-- ✅ 12 Relaciones Producto-Finca
-- ✅ 4 Compras
-- ✅ 4 Detalles de compras
-- ✅ 3 Vehículos
-- ✅ 4 Envíos CON CÁLCULO DE COSTOS COMPLETO
-- ✅ 2 Máquinas
-- ✅ 4 Servicios
-- ✅ 4 Certificados
-- =====================================================

COMMIT;

