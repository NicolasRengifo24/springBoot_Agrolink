-- =====================================================
-- SCRIPT COMPLETO BASE DE DATOS AGROLINK
-- Sistema de comercialización de productos agrícolas
-- Autor: Análisis basado en modelos JPA
-- Fecha: 2025-12-11
-- Motor: MySQL 8.0+
-- =====================================================

-- =====================================================
-- ELIMINAR BASE DE DATOS SI EXISTE Y CREAR NUEVA
-- =====================================================
DROP DATABASE IF EXISTS db_agrolink;
CREATE DATABASE db_agrolink CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE db_agrolink;

-- =====================================================
-- TABLA: tb_usuarios (Tabla principal de usuarios)
-- Relación: Es la tabla padre de todas las especializaciones
-- =====================================================
CREATE TABLE tb_usuarios (
    id_usuario INT AUTO_INCREMENT PRIMARY KEY,
    nombre VARCHAR(50) NOT NULL,
    nombre_usuario VARCHAR(100) NOT NULL UNIQUE,
    contrasena_usuario VARCHAR(200) NOT NULL,
    apellido VARCHAR(50) NOT NULL,
    correo VARCHAR(100) NOT NULL UNIQUE,
    ciudad VARCHAR(50) NOT NULL,
    departamento VARCHAR(50) NOT NULL,
    direccion VARCHAR(200) NOT NULL,
    cedula VARCHAR(20) NOT NULL UNIQUE,
    telefono VARCHAR(15) DEFAULT '0000000000',
    rol VARCHAR(50) NOT NULL DEFAULT 'ROLE_CLIENTE',
    latitud DOUBLE DEFAULT NULL,
    longitud DOUBLE DEFAULT NULL,
    INDEX idx_usuario_rol (rol),
    INDEX idx_usuario_correo (correo),
    INDEX idx_usuario_cedula (cedula)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- =====================================================
-- TABLA: tb_calificacion
-- Relación: Es referenciada por múltiples tablas (Cliente, Productor, Transportista, Asesor, Producto)
-- =====================================================
CREATE TABLE tb_calificacion (
    id_calificacion INT AUTO_INCREMENT PRIMARY KEY,
    puntaje DECIMAL(5,2) DEFAULT NULL,
    promedio DECIMAL(5,2) DEFAULT NULL,
    INDEX idx_calificacion_promedio (promedio)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- =====================================================
-- TABLA: tb_clientes
-- Relación: 1:1 con Usuario (FK-PK), N:1 con Calificacion
-- =====================================================
CREATE TABLE tb_clientes (
    id_usuario INT PRIMARY KEY,
    id_calificacion INT DEFAULT NULL,
    preferencias VARCHAR(150) DEFAULT 'Sin Preferencias',
    CONSTRAINT fk_clientes_usuarios
        FOREIGN KEY (id_usuario) REFERENCES tb_usuarios(id_usuario)
        ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT fk_clientes_calificacion
        FOREIGN KEY (id_calificacion) REFERENCES tb_calificacion(id_calificacion)
        ON DELETE SET NULL ON UPDATE CASCADE,
    INDEX idx_cliente_calificacion (id_calificacion)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- =====================================================
-- TABLA: tb_productores
-- Relación: 1:1 con Usuario (FK-PK), N:1 con Calificacion
-- =====================================================
CREATE TABLE tb_productores (
    id_usuario INT PRIMARY KEY,
    id_calificacion INT DEFAULT NULL,
    tipo_cultivo VARCHAR(50) DEFAULT NULL,
    CONSTRAINT fk_productores_usuario
        FOREIGN KEY (id_usuario) REFERENCES tb_usuarios(id_usuario)
        ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT fk_productores_calificacion
        FOREIGN KEY (id_calificacion) REFERENCES tb_calificacion(id_calificacion)
        ON DELETE SET NULL ON UPDATE CASCADE,
    CONSTRAINT uq_productores_usuario UNIQUE (id_usuario),
    INDEX idx_productor_calificacion (id_calificacion),
    INDEX idx_productor_tipo_cultivo (tipo_cultivo)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- =====================================================
-- TABLA: tb_transportistas
-- Relación: 1:1 con Usuario (FK-PK), N:1 con Calificacion
-- =====================================================
CREATE TABLE tb_transportistas (
    id_usuario INT PRIMARY KEY,
    id_calificacion INT DEFAULT NULL,
    zonas_entrega VARCHAR(250) DEFAULT NULL,
    CONSTRAINT fk_transportistas_usuarios
        FOREIGN KEY (id_usuario) REFERENCES tb_usuarios(id_usuario)
        ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT fk_transportistas_calificacion
        FOREIGN KEY (id_calificacion) REFERENCES tb_calificacion(id_calificacion)
        ON DELETE SET NULL ON UPDATE CASCADE,
    INDEX idx_transportista_calificacion (id_calificacion)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- =====================================================
-- TABLA: tb_asesores
-- Relación: 1:1 con Usuario (FK-PK), N:1 con Calificacion
-- =====================================================
CREATE TABLE tb_asesores (
    id_usuario INT PRIMARY KEY,
    id_calificacion INT DEFAULT NULL,
    tipo_asesoria VARCHAR(50) DEFAULT NULL,
    CONSTRAINT fk_asesores_usuarios
        FOREIGN KEY (id_usuario) REFERENCES tb_usuarios(id_usuario)
        ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT fk_asesores_calificacion
        FOREIGN KEY (id_calificacion) REFERENCES tb_calificacion(id_calificacion)
        ON DELETE SET NULL ON UPDATE CASCADE,
    INDEX idx_asesor_calificacion (id_calificacion)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- =====================================================
-- TABLA: tb_administradores
-- Relación: 1:1 con Usuario (FK-PK)
-- =====================================================
CREATE TABLE tb_administradores (
    id_usuario INT PRIMARY KEY,
    privilegios_admin VARCHAR(200) DEFAULT NULL,
    CONSTRAINT fk_administradores_usuarios
        FOREIGN KEY (id_usuario) REFERENCES tb_usuarios(id_usuario)
        ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- =====================================================
-- TABLA: tb_fincas
-- Relación: N:1 con Productor
-- =====================================================
CREATE TABLE tb_fincas (
    id_finca INT AUTO_INCREMENT PRIMARY KEY,
    id_usuario INT NOT NULL,
    nombre_finca VARCHAR(100) DEFAULT NULL,
    direccion_finca VARCHAR(200) DEFAULT NULL,
    certificado_BPA VARCHAR(200) DEFAULT 'Sin Certificado',
    certificado_MIRFE VARCHAR(200) DEFAULT 'Sin Certificado',
    certificado_MIPE VARCHAR(200) DEFAULT 'Sin Certificado',
    registro_ICA VARCHAR(200) DEFAULT 'Sin Certificado',
    latitud DOUBLE DEFAULT NULL,
    longitud DOUBLE DEFAULT NULL,
    ciudad VARCHAR(100) DEFAULT NULL,
    departamento VARCHAR(100) DEFAULT NULL,
    CONSTRAINT fk_fincas_productor
        FOREIGN KEY (id_usuario) REFERENCES tb_productores(id_usuario)
        ON DELETE CASCADE ON UPDATE CASCADE,
    INDEX idx_finca_productor (id_usuario),
    INDEX idx_finca_ubicacion (latitud, longitud)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- =====================================================
-- TABLA: tb_categorias_productos
-- Relación: Es referenciada por Producto
-- =====================================================
CREATE TABLE tb_categorias_productos (
    id_categoria INT AUTO_INCREMENT PRIMARY KEY,
    nombre_categoria VARCHAR(50) NOT NULL,
    INDEX idx_categoria_nombre (nombre_categoria)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- =====================================================
-- TABLA: tb_productos
-- Relación: N:1 con Productor, N:1 con CategoriaProducto, N:1 con Calificacion
-- =====================================================
CREATE TABLE tb_productos (
    id_producto INT AUTO_INCREMENT PRIMARY KEY,
    id_usuario INT NOT NULL,
    id_categoria INT NOT NULL,
    id_calificacion INT DEFAULT NULL,
    precio DECIMAL(12,2) DEFAULT NULL,
    nombre_producto VARCHAR(100) NOT NULL,
    descripcion_producto VARCHAR(255) DEFAULT NULL,
    stock INT DEFAULT NULL,
    peso_kg DECIMAL(10,2) DEFAULT 1.00,
    CONSTRAINT fk_productos_productor
        FOREIGN KEY (id_usuario) REFERENCES tb_productores(id_usuario)
        ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT fk_productos_categoria
        FOREIGN KEY (id_categoria) REFERENCES tb_categorias_productos(id_categoria)
        ON DELETE RESTRICT ON UPDATE CASCADE,
    CONSTRAINT fk_productos_calificacion
        FOREIGN KEY (id_calificacion) REFERENCES tb_calificacion(id_calificacion)
        ON DELETE SET NULL ON UPDATE CASCADE,
    INDEX idx_producto_productor (id_usuario),
    INDEX idx_producto_categoria (id_categoria),
    INDEX idx_producto_calificacion (id_calificacion),
    INDEX idx_producto_nombre (nombre_producto),
    INDEX idx_producto_stock (stock)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- =====================================================
-- TABLA: tb_imagenes_productos
-- Relación: N:1 con Producto
-- =====================================================
CREATE TABLE tb_imagenes_productos (
    id_imagen INT AUTO_INCREMENT PRIMARY KEY,
    id_producto INT NOT NULL,
    url_imagen VARCHAR(255) NOT NULL,
    es_principal BOOLEAN DEFAULT FALSE,
    CONSTRAINT fk_imagenes_producto
        FOREIGN KEY (id_producto) REFERENCES tb_productos(id_producto)
        ON DELETE CASCADE ON UPDATE CASCADE,
    INDEX idx_imagen_producto (id_producto),
    INDEX idx_imagen_principal (es_principal)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- =====================================================
-- TABLA: tb_productos_fincas
-- Relación: N:1 con Finca, N:1 con Producto (Tabla de asociación)
-- =====================================================
CREATE TABLE tb_productos_fincas (
    id_producto_finca INT AUTO_INCREMENT PRIMARY KEY,
    id_finca INT NOT NULL,
    id_producto INT NOT NULL,
    cantidad_produccion DECIMAL(10,2) DEFAULT NULL,
    fecha_cosecha DATE DEFAULT NULL,
    CONSTRAINT fk_productos_fincas_finca
        FOREIGN KEY (id_finca) REFERENCES tb_fincas(id_finca)
        ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT fk_productos_fincas_producto
        FOREIGN KEY (id_producto) REFERENCES tb_productos(id_producto)
        ON DELETE CASCADE ON UPDATE CASCADE,
    INDEX idx_producto_finca_finca (id_finca),
    INDEX idx_producto_finca_producto (id_producto)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- =====================================================
-- TABLA: tb_compras
-- Relación: N:1 con Cliente
-- =====================================================
CREATE TABLE tb_compras (
    id_compra INT AUTO_INCREMENT PRIMARY KEY,
    id_cliente INT NOT NULL,
    fecha_hora_compra DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    subtotal DECIMAL(10,2) NOT NULL DEFAULT 0.00,
    impuestos DECIMAL(10,2) NOT NULL DEFAULT 0.00,
    valor_envio DECIMAL(10,2) DEFAULT 0.00,
    total DECIMAL(10,2) NOT NULL DEFAULT 0.00,
    direccion_entrega VARCHAR(200) DEFAULT NULL,
    metodo_pago VARCHAR(50) DEFAULT NULL,
    CONSTRAINT fk_compras_cliente
        FOREIGN KEY (id_cliente) REFERENCES tb_clientes(id_usuario)
        ON DELETE CASCADE ON UPDATE CASCADE,
    INDEX idx_compra_cliente (id_cliente),
    INDEX idx_compra_fecha (fecha_hora_compra),
    INDEX idx_compra_total (total)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- =====================================================
-- TABLA: tb_detalles_compra
-- Relación: N:1 con Compra, N:1 con Producto
-- =====================================================
CREATE TABLE tb_detalles_compra (
    id_detalle INT AUTO_INCREMENT PRIMARY KEY,
    id_compra INT NOT NULL,
    id_producto INT NOT NULL,
    cantidad INT NOT NULL DEFAULT 1,
    precio_unitario DECIMAL(12,2) NOT NULL,
    subtotal DECIMAL(12,2) DEFAULT 0.00,
    CONSTRAINT fk_detalles_compra
        FOREIGN KEY (id_compra) REFERENCES tb_compras(id_compra)
        ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT fk_detalles_producto
        FOREIGN KEY (id_producto) REFERENCES tb_productos(id_producto)
        ON DELETE RESTRICT ON UPDATE CASCADE,
    INDEX idx_detalle_compra (id_compra),
    INDEX idx_detalle_producto (id_producto)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- =====================================================
-- TABLA: tb_vehiculos
-- Relación: N:1 con Transportista
-- =====================================================
CREATE TABLE tb_vehiculos (
    id_vehiculo INT AUTO_INCREMENT PRIMARY KEY,
    id_transportista INT NOT NULL,
    tipo_vehiculo VARCHAR(50) DEFAULT NULL,
    capacidad_carga DECIMAL(10,2) DEFAULT NULL,
    documento_propiedad VARCHAR(250) DEFAULT NULL,
    placa_vehiculo VARCHAR(15) DEFAULT NULL,
    CONSTRAINT fk_vehiculos_transportista
        FOREIGN KEY (id_transportista) REFERENCES tb_transportistas(id_usuario)
        ON DELETE CASCADE ON UPDATE CASCADE,
    INDEX idx_vehiculo_transportista (id_transportista),
    INDEX idx_vehiculo_placa (placa_vehiculo)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- =====================================================
-- TABLA: tb_envios
-- Relación: 1:1 con Compra, N:1 con Vehiculo, N:1 con Transportista
-- =====================================================
CREATE TABLE tb_envios (
    id_envio INT AUTO_INCREMENT PRIMARY KEY,
    id_compra INT DEFAULT NULL,
    id_vehiculo INT DEFAULT NULL,
    id_transportista INT DEFAULT NULL,
    estado_envio VARCHAR(50) DEFAULT 'Buscando_Transporte',
    fecha_salida DATE DEFAULT NULL,
    fecha_entrega DATE DEFAULT NULL,
    numero_seguimiento VARCHAR(50) DEFAULT NULL,
    direccion_origen VARCHAR(300) DEFAULT NULL,
    direccion_destino VARCHAR(300) DEFAULT NULL,
    latitud_origen DOUBLE DEFAULT NULL,
    longitud_origen DOUBLE DEFAULT NULL,
    latitud_destino DOUBLE DEFAULT NULL,
    longitud_destino DOUBLE DEFAULT NULL,
    distancia_km DOUBLE DEFAULT NULL,
    peso_total_kg DOUBLE DEFAULT NULL,
    costo_base DECIMAL(10,2) DEFAULT 0.00,
    costo_peso DECIMAL(10,2) DEFAULT 0.00,
    costo_total DECIMAL(10,2) DEFAULT 0.00,
    tarifa_por_km DECIMAL(10,2) DEFAULT 2500.00,
    tarifa_por_kg DECIMAL(10,2) DEFAULT 50.00,
    CONSTRAINT fk_envios_compra
        FOREIGN KEY (id_compra) REFERENCES tb_compras(id_compra)
        ON DELETE SET NULL ON UPDATE CASCADE,
    CONSTRAINT fk_envios_vehiculo
        FOREIGN KEY (id_vehiculo) REFERENCES tb_vehiculos(id_vehiculo)
        ON DELETE SET NULL ON UPDATE CASCADE,
    CONSTRAINT fk_envios_transportista
        FOREIGN KEY (id_transportista) REFERENCES tb_transportistas(id_usuario)
        ON DELETE SET NULL ON UPDATE CASCADE,
    INDEX idx_envio_compra (id_compra),
    INDEX idx_envio_vehiculo (id_vehiculo),
    INDEX idx_envio_transportista (id_transportista),
    INDEX idx_envio_estado (estado_envio),
    INDEX idx_envio_seguimiento (numero_seguimiento)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- =====================================================
-- TABLA: tb_servicios
-- Relación: N:1 con Asesor
-- =====================================================
CREATE TABLE tb_servicios (
    id_servicio INT AUTO_INCREMENT PRIMARY KEY,
    id_asesor INT NOT NULL,
    descripcion VARCHAR(250) DEFAULT NULL,
    estado VARCHAR(20) DEFAULT NULL,
    CONSTRAINT fk_servicios_asesor
        FOREIGN KEY (id_asesor) REFERENCES tb_asesores(id_usuario)
        ON DELETE CASCADE ON UPDATE CASCADE,
    INDEX idx_servicio_asesor (id_asesor),
    INDEX idx_servicio_estado (estado)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- =====================================================
-- TABLA: tb_maquinas
-- Relación: N:1 con Asesor
-- =====================================================
CREATE TABLE tb_maquinas (
    id_maquina INT AUTO_INCREMENT PRIMARY KEY,
    id_asesor INT NOT NULL,
    tipo_maquina VARCHAR(50) NOT NULL,
    documento_propiedad VARCHAR(50) NOT NULL,
    modelo VARCHAR(50) DEFAULT NULL,
    registro_RNMA VARCHAR(200) DEFAULT 'Sin Certificado',
    tarjeta_registro_maquinaria VARCHAR(300) DEFAULT 'Sin Tarjeta',
    CONSTRAINT fk_maquinas_asesor
        FOREIGN KEY (id_asesor) REFERENCES tb_asesores(id_usuario)
        ON DELETE CASCADE ON UPDATE CASCADE,
    INDEX idx_maquina_asesor (id_asesor),
    INDEX idx_maquina_tipo (tipo_maquina)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- =====================================================
-- TABLA: tb_certificados
-- Relación: N:1 con Asesor
-- =====================================================
CREATE TABLE tb_certificados (
    id_certificado INT AUTO_INCREMENT PRIMARY KEY,
    id_usuario INT NOT NULL,
    tipo_certificado VARCHAR(100) NOT NULL,
    descripcion_cert VARCHAR(255) NOT NULL,
    fecha_expedicion DATE NOT NULL,
    CONSTRAINT fk_certificados_asesor
        FOREIGN KEY (id_usuario) REFERENCES tb_asesores(id_usuario)
        ON DELETE CASCADE ON UPDATE CASCADE,
    INDEX idx_certificado_asesor (id_usuario),
    INDEX idx_certificado_tipo (tipo_certificado)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- =====================================================
-- INSERCIÓN DE DATOS INICIALES
-- =====================================================

-- Insertar categorías de productos
INSERT INTO tb_categorias_productos (nombre_categoria) VALUES
('Frutas'),
('Verduras'),
('Hortalizas'),
('Legumbres'),
('Cereales'),
('Tubérculos'),
('Hierbas Aromáticas'),
('Productos Orgánicos'),
('Lácteos'),
('Otros');

-- Insertar calificaciones iniciales
INSERT INTO tb_calificacion (puntaje, promedio) VALUES
(5.0, 5.0),
(4.5, 4.5),
(4.0, 4.0),
(3.5, 3.5),
(3.0, 3.0);

-- Insertar usuario ADMINISTRADOR
INSERT INTO tb_usuarios (nombre, nombre_usuario, contrasena_usuario, apellido, correo, ciudad, departamento, direccion, cedula, telefono, rol, latitud, longitud) VALUES
('Admin', 'admin', '$2a$10$ZqXvI.KqQxJh8xL0hP3xD.YZ8xX7z5cXF0wQ2Qo0qE0K8qY0K0K0K', 'Sistema', 'admin@agrolink.com', 'Bogotá', 'Cundinamarca', 'Calle 100 #10-10', '1000000000', '3000000000', 'ROLE_ADMIN', 4.7110, -74.0721);

-- Crear registro en tb_administradores
INSERT INTO tb_administradores (id_usuario, privilegios_admin) VALUES
(1, 'SUPER_ADMIN');

-- Insertar usuarios PRODUCTORES con coordenadas GPS
INSERT INTO tb_usuarios (nombre, nombre_usuario, contrasena_usuario, apellido, correo, ciudad, departamento, direccion, cedula, telefono, rol, latitud, longitud) VALUES
('Juan', 'juan_productor', '$2a$10$ZqXvI.KqQxJh8xL0hP3xD.YZ8xX7z5cXF0wQ2Qo0qE0K8qY0K0K0K', 'Pérez', 'juan@agrolink.com', 'Chía', 'Cundinamarca', 'Vereda San José', '1000000001', '3001111111', 'ROLE_PRODUCTOR', 4.8607, -74.0582),
('María', 'maria_productora', '$2a$10$ZqXvI.KqQxJh8xL0hP3xD.YZ8xX7z5cXF0wQ2Qo0qE0K8qY0K0K0K', 'González', 'maria@agrolink.com', 'Cajicá', 'Cundinamarca', 'Finca La Esperanza', '1000000002', '3002222222', 'ROLE_PRODUCTOR', 4.9180, -74.0283),
('Carlos', 'carlos_productor', '$2a$10$ZqXvI.KqQxJh8xL0hP3xD.YZ8xX7z5cXF0wQ2Qo0qE0K8qY0K0K0K', 'Rodríguez', 'carlos@agrolink.com', 'Zipaquirá', 'Cundinamarca', 'Vereda El Carmen', '1000000003', '3003333333', 'ROLE_PRODUCTOR', 5.0220, -74.0047);

-- Crear registros en tb_productores
INSERT INTO tb_productores (id_usuario, id_calificacion, tipo_cultivo) VALUES
(2, 1, 'Monocultivo'),
(3, 2, 'Policultivo'),
(4, 1, 'Huerta');

-- Insertar fincas para los productores
INSERT INTO tb_fincas (id_usuario, nombre_finca, direccion_finca, latitud, longitud, ciudad, departamento, certificado_BPA) VALUES
(2, 'Finca El Paraíso', 'Vereda San José, Chía', 4.8607, -74.0582, 'Chía', 'Cundinamarca', 'BPA-2024-001'),
(3, 'Finca La Esperanza', 'Km 5 vía Cajicá', 4.9180, -74.0283, 'Cajicá', 'Cundinamarca', 'BPA-2024-002'),
(4, 'Finca Verde', 'Vereda El Carmen, Zipaquirá', 5.0220, -74.0047, 'Zipaquirá', 'Cundinamarca', 'Sin Certificado');

-- Insertar productos
INSERT INTO tb_productos (id_usuario, id_categoria, id_calificacion, precio, nombre_producto, descripcion_producto, stock, peso_kg) VALUES
(2, 1, 1, 8500.00, 'Tomate Chonto', 'Tomate fresco de primera calidad, cultivado orgánicamente', 500, 1.00),
(2, 2, 1, 3500.00, 'Lechuga Crespa', 'Lechuga fresca, ideal para ensaladas', 300, 0.50),
(3, 3, 2, 2500.00, 'Zanahoria', 'Zanahoria orgánica, rica en vitaminas', 400, 1.00),
(3, 1, 2, 12000.00, 'Fresa', 'Fresas frescas y dulces', 200, 0.50),
(4, 6, 1, 4500.00, 'Papa Criolla', 'Papa criolla de primera calidad', 600, 1.00);

-- Insertar imágenes de productos (URLs de ejemplo)
INSERT INTO tb_imagenes_productos (id_producto, url_imagen, es_principal) VALUES
(1, '/images/products/tomate-chonto-1.jpg', TRUE),
(1, '/images/products/tomate-chonto-2.jpg', FALSE),
(2, '/images/products/lechuga-crespa-1.jpg', TRUE),
(3, '/images/products/zanahoria-1.jpg', TRUE),
(4, '/images/products/fresa-1.jpg', TRUE),
(4, '/images/products/fresa-2.jpg', FALSE),
(5, '/images/products/papa-criolla-1.jpg', TRUE);

-- Asociar productos con fincas
INSERT INTO tb_productos_fincas (id_finca, id_producto, cantidad_produccion, fecha_cosecha) VALUES
(1, 1, 500.00, '2025-01-15'),
(1, 2, 300.00, '2025-01-10'),
(2, 3, 400.00, '2025-01-20'),
(2, 4, 200.00, '2025-01-18'),
(3, 5, 600.00, '2025-01-12');

-- Insertar usuarios CLIENTES con coordenadas GPS
INSERT INTO tb_usuarios (nombre, nombre_usuario, contrasena_usuario, apellido, correo, ciudad, departamento, direccion, cedula, telefono, rol, latitud, longitud) VALUES
('Laura', 'laura_cliente', '$2a$10$ZqXvI.KqQxJh8xL0hP3xD.YZ8xX7z5cXF0wQ2Qo0qE0K8qY0K0K0K', 'Martínez', 'laura@cliente.com', 'Bogotá', 'Cundinamarca', 'Calle 50 #20-30', '2000000001', '3004444444', 'ROLE_CLIENTE', 4.6482, -74.0816),
('Pedro', 'pedro_cliente', '$2a$10$ZqXvI.KqQxJh8xL0hP3xD.YZ8xX7z5cXF0wQ2Qo0qE0K8qY0K0K0K', 'López', 'pedro@cliente.com', 'Bogotá', 'Cundinamarca', 'Carrera 15 #85-40', '2000000002', '3005555555', 'ROLE_CLIENTE', 4.6889, -74.0583);

-- Crear registros en tb_clientes
INSERT INTO tb_clientes (id_usuario, id_calificacion, preferencias) VALUES
(5, 1, 'Productos orgánicos'),
(6, 2, 'Frutas y verduras frescas');

-- Insertar usuarios TRANSPORTISTAS con coordenadas GPS
INSERT INTO tb_usuarios (nombre, nombre_usuario, contrasena_usuario, apellido, correo, ciudad, departamento, direccion, cedula, telefono, rol, latitud, longitud) VALUES
('Miguel', 'miguel_trans', '$2a$10$ZqXvI.KqQxJh8xL0hP3xD.YZ8xX7z5cXF0wQ2Qo0qE0K8qY0K0K0K', 'Ramírez', 'miguel@transporte.com', 'Bogotá', 'Cundinamarca', 'Calle 80 #90-20', '3000000001', '3006666666', 'ROLE_TRANSPORTISTA', 4.7007, -74.0621);

-- Crear registros en tb_transportistas
INSERT INTO tb_transportistas (id_usuario, id_calificacion, zonas_entrega) VALUES
(7, 1, 'Bogotá, Chía, Cajicá, Zipaquirá, Soacha');

-- Insertar vehículos
INSERT INTO tb_vehiculos (id_transportista, tipo_vehiculo, capacidad_carga, placa_vehiculo, documento_propiedad) VALUES
(7, 'Camioneta', 1500.00, 'ABC123', 'DOC-VEH-001'),
(7, 'Camión', 5000.00, 'XYZ789', 'DOC-VEH-002');

-- Insertar usuarios ASESORES
INSERT INTO tb_usuarios (nombre, nombre_usuario, contrasena_usuario, apellido, correo, ciudad, departamento, direccion, cedula, telefono, rol, latitud, longitud) VALUES
('Ana', 'ana_asesora', '$2a$10$ZqXvI.KqQxJh8xL0hP3xD.YZ8xX7z5cXF0wQ2Qo0qE0K8qY0K0K0K', 'Torres', 'ana@asesor.com', 'Bogotá', 'Cundinamarca', 'Carrera 7 #40-50', '4000000001', '3007777777', 'ROLE_ASESOR', 4.6533, -74.0836);

-- Crear registros en tb_asesores
INSERT INTO tb_asesores (id_usuario, id_calificacion, tipo_asesoria) VALUES
(8, 1, 'Técnica Agrícola');

-- Insertar servicios
INSERT INTO tb_servicios (id_asesor, descripcion, estado) VALUES
(8, 'Asesoría en cultivos orgánicos y certificación BPA', 'Activo'),
(8, 'Capacitación en manejo de plagas', 'Activo');

-- Insertar certificados de asesores
INSERT INTO tb_certificados (id_usuario, tipo_certificado, descripcion_cert, fecha_expedicion) VALUES
(8, 'Ingeniero Agrónomo', 'Universidad Nacional de Colombia', '2020-06-15'),
(8, 'Certificación BPA', 'ICA - Instituto Colombiano Agropecuario', '2021-03-10');

-- Insertar maquinaria
INSERT INTO tb_maquinas (id_asesor, tipo_maquina, documento_propiedad, modelo, registro_RNMA) VALUES
(8, 'Tractor', 'DOC-MAQ-001', 'John Deere 5055E', 'RNMA-2023-001'),
(8, 'Fumigadora', 'DOC-MAQ-002', 'Jacto PJH', 'RNMA-2023-002');

-- Insertar compra de ejemplo
INSERT INTO tb_compras (id_cliente, fecha_hora_compra, subtotal, impuestos, valor_envio, total, direccion_entrega, metodo_pago) VALUES
(5, '2025-01-05 10:30:00', 50000.00, 9500.00, 15000.00, 74500.00, 'Calle 50 #20-30, Bogotá', 'Tarjeta de Crédito');

-- Insertar detalles de compra
INSERT INTO tb_detalles_compra (id_compra, id_producto, cantidad, precio_unitario, subtotal) VALUES
(1, 1, 3, 8500.00, 25500.00),
(1, 2, 5, 3500.00, 17500.00),
(1, 4, 2, 12000.00, 24000.00);

-- Actualizar stock de productos después de la compra
UPDATE tb_productos SET stock = stock - 3 WHERE id_producto = 1;
UPDATE tb_productos SET stock = stock - 5 WHERE id_producto = 2;
UPDATE tb_productos SET stock = stock - 2 WHERE id_producto = 4;

-- Insertar envío asociado a la compra
INSERT INTO tb_envios (
    id_compra,
    id_vehiculo,
    id_transportista,
    estado_envio,
    direccion_origen,
    direccion_destino,
    latitud_origen,
    longitud_origen,
    latitud_destino,
    longitud_destino,
    distancia_km,
    peso_total_kg,
    costo_base,
    costo_peso,
    costo_total,
    fecha_salida,
    numero_seguimiento
) VALUES (
    1,
    1,
    7,
    'En_Camino',
    'Vereda San José, Chía',
    'Calle 50 #20-30, Bogotá',
    4.8607,
    -74.0582,
    4.6482,
    -74.0816,
    25.5,
    4.0,
    63750.00,
    200.00,
    15000.00,
    '2025-01-06',
    'ENV-2025-000001'
);

-- =====================================================
-- VISTAS ÚTILES
-- =====================================================

-- Vista de productos con información completa
CREATE OR REPLACE VIEW v_productos_completos AS
SELECT
    p.id_producto,
    p.nombre_producto,
    p.descripcion_producto,
    p.precio,
    p.stock,
    p.peso_kg,
    c.nombre_categoria,
    u.nombre AS productor_nombre,
    u.apellido AS productor_apellido,
    u.ciudad AS productor_ciudad,
    u.departamento AS productor_departamento,
    cal.promedio AS calificacion_promedio,
    (SELECT url_imagen FROM tb_imagenes_productos WHERE id_producto = p.id_producto AND es_principal = TRUE LIMIT 1) AS imagen_principal
FROM tb_productos p
INNER JOIN tb_productores prod ON p.id_usuario = prod.id_usuario
INNER JOIN tb_usuarios u ON prod.id_usuario = u.id_usuario
INNER JOIN tb_categorias_productos c ON p.id_categoria = c.id_categoria
LEFT JOIN tb_calificacion cal ON p.id_calificacion = cal.id_calificacion;

-- Vista de compras con detalles
CREATE OR REPLACE VIEW v_compras_detalladas AS
SELECT
    c.id_compra,
    c.fecha_hora_compra,
    c.total,
    c.metodo_pago,
    c.direccion_entrega,
    u.nombre AS cliente_nombre,
    u.apellido AS cliente_apellido,
    u.correo AS cliente_correo,
    u.telefono AS cliente_telefono,
    e.estado_envio,
    e.numero_seguimiento,
    e.fecha_entrega
FROM tb_compras c
INNER JOIN tb_clientes cl ON c.id_cliente = cl.id_usuario
INNER JOIN tb_usuarios u ON cl.id_usuario = u.id_usuario
LEFT JOIN tb_envios e ON c.id_compra = e.id_compra;

-- Vista de fincas con productores
CREATE OR REPLACE VIEW v_fincas_productores AS
SELECT
    f.id_finca,
    f.nombre_finca,
    f.direccion_finca,
    f.ciudad,
    f.departamento,
    f.latitud,
    f.longitud,
    f.certificado_BPA,
    u.nombre AS productor_nombre,
    u.apellido AS productor_apellido,
    u.telefono AS productor_telefono,
    u.correo AS productor_correo
FROM tb_fincas f
INNER JOIN tb_productores p ON f.id_usuario = p.id_usuario
INNER JOIN tb_usuarios u ON p.id_usuario = u.id_usuario;

-- =====================================================
-- PROCEDIMIENTOS ALMACENADOS
-- =====================================================

-- Procedimiento para registrar una compra completa
DELIMITER $$

CREATE PROCEDURE sp_registrar_compra(
    IN p_id_cliente INT,
    IN p_direccion_entrega VARCHAR(200),
    IN p_metodo_pago VARCHAR(50),
    IN p_productos JSON,
    OUT p_id_compra INT
)
BEGIN
    DECLARE v_subtotal DECIMAL(10,2) DEFAULT 0.00;
    DECLARE v_impuestos DECIMAL(10,2) DEFAULT 0.00;
    DECLARE v_total DECIMAL(10,2) DEFAULT 0.00;
    DECLARE v_id_producto INT;
    DECLARE v_cantidad INT;
    DECLARE v_precio_unitario DECIMAL(12,2);
    DECLARE v_subtotal_detalle DECIMAL(12,2);
    DECLARE v_index INT DEFAULT 0;
    DECLARE v_productos_count INT;

    -- Iniciar transacción
    START TRANSACTION;

    -- Obtener cantidad de productos
    SET v_productos_count = JSON_LENGTH(p_productos);

    -- Calcular subtotal
    WHILE v_index < v_productos_count DO
        SET v_id_producto = JSON_EXTRACT(p_productos, CONCAT('$[', v_index, '].id_producto'));
        SET v_cantidad = JSON_EXTRACT(p_productos, CONCAT('$[', v_index, '].cantidad'));

        SELECT precio INTO v_precio_unitario FROM tb_productos WHERE id_producto = v_id_producto;
        SET v_subtotal_detalle = v_precio_unitario * v_cantidad;
        SET v_subtotal = v_subtotal + v_subtotal_detalle;

        SET v_index = v_index + 1;
    END WHILE;

    -- Calcular impuestos (19% IVA)
    SET v_impuestos = v_subtotal * 0.19;
    SET v_total = v_subtotal + v_impuestos;

    -- Insertar compra
    INSERT INTO tb_compras (id_cliente, fecha_hora_compra, subtotal, impuestos, total, direccion_entrega, metodo_pago)
    VALUES (p_id_cliente, NOW(), v_subtotal, v_impuestos, v_total, p_direccion_entrega, p_metodo_pago);

    SET p_id_compra = LAST_INSERT_ID();

    -- Insertar detalles de compra
    SET v_index = 0;
    WHILE v_index < v_productos_count DO
        SET v_id_producto = JSON_EXTRACT(p_productos, CONCAT('$[', v_index, '].id_producto'));
        SET v_cantidad = JSON_EXTRACT(p_productos, CONCAT('$[', v_index, '].cantidad'));

        SELECT precio INTO v_precio_unitario FROM tb_productos WHERE id_producto = v_id_producto;
        SET v_subtotal_detalle = v_precio_unitario * v_cantidad;

        INSERT INTO tb_detalles_compra (id_compra, id_producto, cantidad, precio_unitario, subtotal)
        VALUES (p_id_compra, v_id_producto, v_cantidad, v_precio_unitario, v_subtotal_detalle);

        -- Actualizar stock
        UPDATE tb_productos SET stock = stock - v_cantidad WHERE id_producto = v_id_producto;

        SET v_index = v_index + 1;
    END WHILE;

    COMMIT;
END$$

DELIMITER ;

-- =====================================================
-- TRIGGERS
-- =====================================================

-- Trigger para actualizar subtotal en detalles de compra
DELIMITER $$

CREATE TRIGGER trg_calcular_subtotal_detalle
BEFORE INSERT ON tb_detalles_compra
FOR EACH ROW
BEGIN
    SET NEW.subtotal = NEW.cantidad * NEW.precio_unitario;
END$$

DELIMITER ;

-- Trigger para validar stock antes de venta
DELIMITER $$

CREATE TRIGGER trg_validar_stock
BEFORE INSERT ON tb_detalles_compra
FOR EACH ROW
BEGIN
    DECLARE v_stock_actual INT;
    SELECT stock INTO v_stock_actual FROM tb_productos WHERE id_producto = NEW.id_producto;

    IF v_stock_actual < NEW.cantidad THEN
        SIGNAL SQLSTATE '45000'
        SET MESSAGE_TEXT = 'Stock insuficiente para completar la compra';
    END IF;
END$$

DELIMITER ;

-- =====================================================
-- ÍNDICES ADICIONALES PARA OPTIMIZACIÓN
-- =====================================================

-- Índices para búsquedas frecuentes
CREATE INDEX idx_usuario_nombre ON tb_usuarios(nombre, apellido);
CREATE INDEX idx_producto_precio ON tb_productos(precio);
CREATE INDEX idx_compra_estado ON tb_envios(estado_envio);

-- =====================================================
-- SCRIPT COMPLETO FINALIZADO
-- =====================================================
-- Este script crea la base de datos completa de Agrolink
-- incluyendo todas las tablas, relaciones, datos iniciales,
-- vistas, procedimientos almacenados y triggers.
-- =====================================================

