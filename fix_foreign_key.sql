-- Script para actualizar la constraint existente sin perder datos
USE agro;

-- 1. Eliminar la constraint actual
ALTER TABLE tb_detalles_compra
DROP FOREIGN KEY fk_detalles_producto;

-- 2. Agregar la nueva constraint con ON UPDATE CASCADE
ALTER TABLE tb_detalles_compra
ADD CONSTRAINT fk_detalles_producto
FOREIGN KEY (id_producto) REFERENCES tb_productos(id_producto)
ON DELETE RESTRICT ON UPDATE CASCADE;

-- Verificar que se aplicó correctamente
SHOW CREATE TABLE tb_detalles_compra;
