select * from tb_usuarios;

-- Usuarios
INSERT INTO tb_usuarios (nombre, nombre_usuario, contrasena_usuario, apellido, correo, ciudad, departamento, direccion, cedula, telefono) VALUES
('Carlos','carlosagricola','claveSegura123','Gómez','c.gomez@email.com','Medellín','Antioquia','Carrera 45 #12-34','123456789012','3101111111'),
('María','mariacampesina','password1234','Rodríguez','m.rodriguez@email.com','Bogotá','Cundinamarca','Calle 23 #45-67','234567890123','3202222222'),
('Juan','juanproductor','juanito2023','Pérez','j.perez@email.com','Cali','Valle del Cauca','Avenida 6N #23-45','345678901234','3003333333'),
('Ana','anitacliente','anaclave456','López','a.lopez@email.com','Barranquilla','Atlántico','Carrera 54 #78-90','456789012345','3014444444'),
('Pedro','pedrotransport','transporte2023','Martínez','p.martinez@email.com','Pereira','Risaralda','Calle 30 #12-34','567890123456','3025555555'),
('Luisa','luisaasesora','luisapass789','García','l.garcia@email.com','Manizales','Caldas','Carrera 22 #33-44','678901234567','3036666666'),
('Jorge','jorgeadmin','adminjorge12','Fernández','j.fernandez@email.com','Armenia','Quindío','Avenida Bolívar #15-25','789012345678','3047777777'),
('Sofía','sofiadmin','sofiaadmin456','Hernández','s.hernandez@email.com','Ibagué','Tolima','Calle 18 #20-30','890123456789','3058888888'),
('Diego','diegoproductor','diego2023pass','Díaz','d.diaz@email.com','Neiva','Huila','Carrera 5 #10-15','901234567890','3069999999'),
('Camila','camilacliente','camipass789','Vargas','c.vargas@email.com','Villavicencio','Meta','Avenida 40 #50-60','012345678901','3070000000'),
('Nicolas','clienteNicolas','nicopass789','Mendez','mendez@email.com','Villavicencio','Meta','Avenida 67 #23-5','1007846932','3111111111'),
('Pepe','MaquinistaPepe','pepepass769','Mujica','pepe@email.com','Madrid','Cundinamarca','cra 77 #23-5','1887846932','3122222222'),
('Laura','lauracafe','laurapass2024','Ramírez','l.ramirez@email.com','Popayán','Cauca','Cra 12 #8-23','345999888777','3133333333'),
('Mateo','mateotransport','mateo2025','Suárez','m.suarez@email.com','Tunja','Boyacá','Cl 10 #22-14','234888777666','3144444444'),
('Valentina','valeclient','valepass123','Castro','v.castro@email.com','Santa Marta','Magdalena','Av 1 #20-45','123777666555','3155555555');

-- Calificaciones
INSERT INTO tb_calificacion (puntaje, promedio) VALUES
(4.5,4.3),(4.8,4.6),(3.9,4.0),(4.2,4.1),(4.7,4.5),
(4.0,4.2),(4.9,4.7),(3.8,4.0),(4.3,4.2),(4.6,4.4),
(2.2,2.4),(5.0,5.0);



-- Categorías
INSERT INTO tb_categorias_productos (nombre_categoria) VALUES
('Lácteos'),('Cárnicos'),('Frutas'),('Verduras'),('Granos'),('Café'),
('Tubérculos'),('Aceites'),('Semillas'),('Hierbas');

-- Roles
select * from tb_productores;
INSERT INTO tb_productores (id_usuario,id_calificacion,tipo_cultivo) VALUES
(1,1,'Monocultivo'),(3,3,'Policultivo'),(9,9,'Huerta'),(13,2,'Cultivo_Urbano');
show create table tb_productores;

select * from tb_clientes;
INSERT INTO tb_clientes (id_usuario,id_calificacion,preferencias) VALUES
(4,4,'Prefiere productos orgánicos'),
(10,10,'Sin preferencias'),
(11,11,'Productos locales'),
(15,6,'Compra frutas y verduras frescas');

select * from tb_transportistas;
INSERT INTO tb_transportistas (id_usuario,id_calificacion,zonas_entrega) VALUES
(5,5,'Antioquia, Risaralda, Caldas'),(14,2,'Boyacá, Santander, Meta');
select * from tb_administradores;
INSERT INTO tb_administradores (id_usuario,privilegios_admin) VALUES
(7,'Super administrador con acceso total'),(8,'Administrador de usuarios y productos');

select * from tb_asesores;
INSERT INTO tb_asesores (id_usuario,id_calificacion,tipo_asesoria) VALUES
(6,6,'Asesor_Agricola'),(2,2,'Veterinario'),(12,12,'Maquinista');

-- Fincas
select * from tb_fincas;
INSERT INTO tb_fincas (id_usuario,nombre_finca,direccion_finca) VALUES
(1,'Finca Tierra Verde','Vereda El Roble, Antioquia'),
(1,'Finca Las Lomas','Vereda La Ceiba, Tolima'),
(3,'Finca El Mirador','Vereda El Trigal, Boyacá'),
(9,'Finca La Colina','Vereda La Pradera, Meta'),
(9,'Finca Agua Clara','Vereda San Isidro, Huila'),
(9,'Finca Brisa Fresca','Vereda Montebello, Cundinamarca'),
(13,'Finca AgroVida','Vereda El Cedro, Cauca');

-- Productos
INSERT INTO tb_productos (id_usuario,id_categoria,id_calificacion,precio,nombre_producto,descripcion_producto,stock,peso_kg) VALUES
(1,6,1,12000,'Café especial','Café de alta calidad cultivado en Antioquia',500,1.0),
(3,3,3,2500,'Plátano hartón','Plátano cultivado en el Valle del Cauca',800,1.2),
(9,1,9,3500,'Queso campesino','Queso fresco producido en Huila',300,0.5),
(1,4,1,1800,'Tomate chonto','Tomate cultivado sin pesticidas',600,0.3),
(3,5,3,4000,'Frijol rojo','Frijol de excelente calidad',400,0.2),
(13,2,2,15000,'Carne de res','Carne fresca certificada',250,1.5),
(9,7,9,1200,'Papa criolla','Papa de producción local',900,0.4);

-- Imágenes de productos
INSERT INTO tb_imagenes_productos (id_producto, url_imagen, es_principal) VALUES
(1, 'images/products/cafe_especial_1.webp', TRUE),
(1, 'images/products/cafe_especial_2.jpg', FALSE),
(3, 'images/products/queso_campesino.jpg', TRUE),
(4, 'images/products/tomate_chonto.jpg', TRUE),
(7, 'images/products/papa_criolla.jpg', TRUE);
select * from tb_fincas;
-- Productos de fincas
INSERT INTO tb_productos_fincas (id_finca,id_producto,cantidad_produccion,fecha_cosecha) VALUES
(7,1,150.00,'2025-06-01'),
(7,2,80.00,'2025-06-10'),
(2,3,120.00,'2025-05-15'),
(3,4,200.00,'2025-07-05');

-- Compras
select * from tb_compras;
INSERT INTO tb_compras (id_cliente,subtotal,impuestos,total,metodo_pago,direccion_entrega) VALUES
(4,24000,2000,26000,'Tarjeta débito','Calle 23 #45-67, Bogotá'),
(10,7000,500,7500,'Efectivo','Avenida 40 #50-60, Villavicencio'),
(11,15000,1200,16200,'Nequi','Avenida 67 #23-5, Villavicencio'),
(15,18000,1440,19440,'Tarjeta crédito','Av 1 #20-45, Santa Marta');

-- Detalles de compras
INSERT INTO tb_detalles_compra (id_compra,id_producto,cantidad,precio_unitario,subtotal) VALUES
(1,1,2,12000,24000),
(2,3,2,3500,7000),
(3,4,5,1800,9000),
(4,7,15,1200,18000);

-- Vehículos
INSERT INTO tb_vehiculos (id_transportista,tipo_vehiculo,capacidad_carga,documento_propiedad,placa_vehiculo) VALUES
(5,'Camión',5000,'Documento propiedad 123','ABC123'),
(5,'Camioneta',2000,'Documento propiedad 456','DEF456'),
(14,'Camión',4000,'Documento propiedad 789','GHI789');

-- Envíos
INSERT INTO tb_envios (id_compra) VALUES (1),(2),(3),(4);

-- Máquinas
INSERT INTO tb_maquinas (id_asesor,tipo_maquina,documento_propiedad,modelo) VALUES
(12,'Tractor','Documento tractor 101','John Deere 5100'),
(12,'Cosechadora','Documento cosechadora 202','New Holland CX8080');

-- Servicios
INSERT INTO tb_servicios (id_asesor,descripcion,estado) VALUES
(6,'Asesoría en cultivo de café','Activo'),
(6,'Análisis de suelos','Activo'),
(2,'Asesoría en ganadería','Inactivo'),
(12,'Alquiler de maquinaria y servicios de operación','Activo');

-- Certificados
INSERT INTO tb_certificados (id_usuario,tipo_certificado,descripcion_cert,fecha_expedicion) VALUES
(6,'Ingeniero Agrónomo','Título profesional','2018-06-15'),
(6,'Especialista en suelos','Posgrado','2020-11-20'),
(2,'Técnico pecuario','Certificación SENA','2019-03-10'),
(12,'Técnico Operario Agrícola','SENA CBA','2020-03-29');

