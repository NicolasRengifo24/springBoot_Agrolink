package com.example.springbootagrolink.services;

import com.example.springbootagrolink.model.Finca;
import com.example.springbootagrolink.model.Producto;
import com.example.springbootagrolink.model.ProductoFinca;
import com.example.springbootagrolink.repository.FincaRepository;
import com.example.springbootagrolink.repository.ProductoFincaRepository;
import com.example.springbootagrolink.repository.ProductoRepository;
import com.example.springbootagrolink.services.Idao.Idao;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Servicio para gestionar la relación Producto-Finca (tabla intermedia con datos adicionales)
 *
 * ANÁLISIS DE LA RELACIÓN:
 * ========================
 * ProductoFinca es una tabla intermedia en una relación Many-to-Many (N:N):
 * - Una Finca puede tener MUCHOS Productos (cultiva varios productos)
 * - Un Producto puede estar en MUCHAS Fincas (se cultiva en varios lugares)
 *
 * Relaciones en el modelo:
 * 1. @ManyToOne con Finca → muchos ProductoFinca pertenecen a una Finca
 * 2. @ManyToOne con Producto → muchos ProductoFinca referencian un Producto
 *
 * Datos adicionales en la tabla:
 * - cantidadProduccion: cuánto se produce (BigDecimal)
 * - fechaCosecha: cuándo se cosechó (LocalDate)
 *
 * ¿POR QUÉ 3 REPOSITORIOS?
 * ========================
 * Necesitamos inyectar 3 repositorios porque ProductoFinca DEPENDE de ambas entidades:
 * - ProductoFincaRepository: para operaciones sobre la tabla intermedia
 * - FincaRepository: para validar que las fincas existen antes de asociar
 * - ProductoRepository: para validar que los productos existen antes de asociar
 *
 * ¿POR QUÉ @Transactional?
 * ========================
 * readOnly = true (en consultas):
 * - Le dice a la BD: "esto es solo lectura, no guardes cambios"
 * - Optimiza el rendimiento (no mantiene datos en memoria)
 * - Permite a la BD usar optimizaciones específicas para lecturas
 *
 * Sin readOnly (en escrituras):
 * - Si algo falla, revierte TODA la operación (rollback)
 * - Garantiza integridad de datos
 * - Esencial cuando haces múltiples operaciones en una sola transacción
 */
@Service
public class ProductoFincaService implements Idao<ProductoFinca, Integer> {

    private final ProductoFincaRepository productoFincaRepository;
    private final FincaRepository fincaRepository;
    private final ProductoRepository productoRepository;

    /**
     * Constructor con inyección de dependencias
     * Spring automáticamente inyecta los 3 repositorios
     */
    public ProductoFincaService(ProductoFincaRepository productoFincaRepository,
                                FincaRepository fincaRepository,
                                ProductoRepository productoRepository) {
        this.productoFincaRepository = productoFincaRepository;
        this.fincaRepository = fincaRepository;
        this.productoRepository = productoRepository;
    }

    // ==================== MÉTODOS DE IDAO (CRUD BÁSICO) ====================

    /**
     *
     * @Transactional(readOnly = true):
     * - Optimiza la consulta (solo lectura, no mantiene cambios)
     * - Más eficiente en memoria y conexiones
     */
    @Override
    @Transactional(readOnly = true)
    public List<ProductoFinca> obtenerTodos() {
        return productoFincaRepository.findAll();
    }

    /**
     * Busca una asociación Producto-Finca por su ID único
     *
     * SQL: SELECT * FROM tb_productos_fincas WHERE id_producto_finca = ?
     *
     * Optional evita NullPointerException:
     * - isPresent(): verifica si existe
     * - get(): obtiene el valor
     * - orElse(): valor por defecto si no existe
     * - orElseThrow(): lanza excepción si no existe
     */
    @Override
    @Transactional(readOnly = true)
    public Optional<ProductoFinca> obtenerPorId(Integer id) {
        return productoFincaRepository.findById(id);
    }

    /**
     * Guarda una nueva asociación Producto-Finca
     *
     * VALIDACIONES IMPORTANTES:
     * 1. Verifica que tenga una Finca asignada
     * 2. Verifica que tenga un Producto asignado
     * 3. Verifica que la Finca exista en la BD
     * 4. Verifica que el Producto exista en la BD
     * 5. Verifica que NO exista duplicado (misma finca + mismo producto)
     *
     * ¿Por qué estas validaciones?
     * - Sin ellas, podríamos crear asociaciones con IDs inexistentes
     * - Podríamos tener duplicados (Finca 1 + Producto 5 dos veces)
     * - La BD lanzaría errores de integridad referencial
     *
     * SQL: INSERT INTO tb_productos_fincas (...) VALUES (...)
     *
     * @Transactional (sin readOnly):
     * - Si alguna validación falla, NO se guarda nada
     * - Si el save() falla, revierte TODO
     * - Garantiza integridad de datos
     */
    @Override
    @Transactional
    public ProductoFinca guardar(ProductoFinca entidad) {
        // Validación 1: Debe tener una Finca asignada
        if (entidad.getFinca() == null || entidad.getFinca().getIdFinca() == null) {
            throw new IllegalArgumentException("Debe especificar una finca válida");
        }

        // Validación 2: Debe tener un Producto asignado
        if (entidad.getProducto() == null || entidad.getProducto().getIdProducto() == null) {
            throw new IllegalArgumentException("Debe especificar un producto válido");
        }

        Integer fincaId = entidad.getFinca().getIdFinca();
        Integer productoId = entidad.getProducto().getIdProducto();

        // Validación 3: La Finca debe existir en la base de datos
        if (!fincaRepository.existsById(fincaId)) {
            throw new IllegalArgumentException("La finca con ID " + fincaId + " no existe");
        }

        // Validación 4: El Producto debe existir en la base de datos
        if (!productoRepository.existsById(productoId)) {
            throw new IllegalArgumentException("El producto con ID " + productoId + " no existe");
        }

        // Validación 5: No debe existir duplicado
        if (productoFincaRepository.existeAsociacion(fincaId, productoId)) {
            throw new IllegalStateException(
                "Ya existe una asociación entre la finca " + fincaId +
                " y el producto " + productoId
            );
        }

        // Si pasa todas las validaciones, guardar
        return productoFincaRepository.save(entidad);
    }

    /**
     * Actualiza una asociación Producto-Finca existente
     *
     * Campos que se pueden actualizar:
     * - cantidadProduccion: cambiar la cantidad producida
     * - fechaCosecha: actualizar la fecha de cosecha
     *
     * SQL: UPDATE tb_productos_fincas SET ... WHERE id_producto_finca = ?
     *
     * @Transactional: Si el update falla, revierte cambios
     */
    @Override
    @Transactional
    public ProductoFinca actualizar(Integer id, ProductoFinca entidad) {
        // Verificar que la asociación existe
        ProductoFinca existente = productoFincaRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("ProductoFinca no encontrado con ID: " + id));

        // Actualizar los campos
        existente.setCantidadProduccion(entidad.getCantidadProduccion());
        existente.setFechaCosecha(entidad.getFechaCosecha());

        // Si se cambian las relaciones, actualizarlas
        if (entidad.getFinca() != null) {
            existente.setFinca(entidad.getFinca());
        }
        if (entidad.getProducto() != null) {
            existente.setProducto(entidad.getProducto());
        }

        return productoFincaRepository.save(existente);
    }


    @Override
    @Transactional
    public boolean eliminar(Integer id) {
        if (productoFincaRepository.existsById(id)) {
            productoFincaRepository.deleteById(id);
            return true;
        }
        return false;
    }


    // ==================== MÉTODOS PERSONALIZADOS PARA LA RELACIÓN N:N ====================

    /**
     * Obtiene todos los productos cultivados en una finca específica
     *
     * CASO DE USO: Un productor quiere ver su catálogo de productos
     *
     * SQL: SELECT pf.* FROM tb_productos_fincas pf WHERE pf.id_finca = ?
     *
     * JPA automáticamente trae:
     * - productoFinca.getProducto() → el producto cultivado
     * - productoFinca.getCantidadProduccion() → cuánto se produce
     * - productoFinca.getFechaCosecha() → fecha de cosecha
     */
    @Transactional(readOnly = true)
    public List<ProductoFinca> obtenerProductosPorFinca(Integer fincaId) {
        return productoFincaRepository.buscarProductosDeFinca(fincaId);
    }

    /**
     * Obtiene todas las fincas que cultivan un producto específico
     *
     * CASO DE USO: Un comprador busca "¿Dónde puedo comprar Aguacate?"
     *
     * SQL: SELECT pf.* FROM tb_productos_fincas pf WHERE pf.id_producto = ?
     *
     * JPA automáticamente trae:
     * - productoFinca.getFinca() → la finca que lo cultiva
     * - productoFinca.getCantidadProduccion() → cuánto produce
     * - productoFinca.getFechaCosecha() → cuándo cosechó
     */
    @Transactional(readOnly = true)
    public List<ProductoFinca> obtenerFincasPorProducto(Integer productoId) {
        return productoFincaRepository.buscarFincasConProducto(productoId);
    }

    /**
     * Crea o actualiza una asociación entre producto y finca (lógica inteligente)
     *
     * LÓGICA:
     * 1. Busca si ya existe la asociación (finca + producto)
     * 2. Si EXISTE: actualiza cantidad y fecha (no crea duplicado)
     * 3. Si NO EXISTE: crea nueva asociación
     *
     * ¿Por qué este método es útil?
     * - Evita duplicados automáticamente
     * - Simplifica el código del controller
     * - Un solo método para crear y actualizar
     *
     * CASO DE USO: Formulario "Agregar producto a mi finca"
     *
     * @Transactional: Múltiples operaciones - si cualquiera falla, revierte TODO
     */
    @Transactional
    public ProductoFinca asociarProductoConFinca(Integer fincaId,
                                                  Integer productoId,
                                                  BigDecimal cantidad,
                                                  LocalDate fechaCosecha) {
        // Validar que la finca existe (trae la entidad completa)
        Finca finca = fincaRepository.findById(fincaId)
            .orElseThrow(() -> new RuntimeException("Finca no encontrada con ID: " + fincaId));

        // Validar que el producto existe (trae la entidad completa)
        Producto producto = productoRepository.findById(productoId)
            .orElseThrow(() -> new RuntimeException("Producto no encontrado con ID: " + productoId));

        // Buscar si ya existe la asociación
        Optional<ProductoFinca> existente =
            productoFincaRepository.buscarAsociacion(fincaId, productoId);

        ProductoFinca productoFinca;
        if (existente.isPresent()) {
            // Ya existe: actualizar
            productoFinca = existente.get();
            productoFinca.setCantidadProduccion(cantidad);
            productoFinca.setFechaCosecha(fechaCosecha);
        } else {
            // No existe: crear nueva asociación
            productoFinca = new ProductoFinca();
            productoFinca.setFinca(finca);
            productoFinca.setProducto(producto);
            productoFinca.setCantidadProduccion(cantidad);
            productoFinca.setFechaCosecha(fechaCosecha);
        }

        return productoFincaRepository.save(productoFinca);
    }

    /**
     * Desasocia un producto de una finca (elimina la relación)
     *
     * CASO DE USO: "Ya no cultivo Aguacate en mi finca"
     *
     * Esto NO elimina la finca ni el producto, solo la relación
     */
    @Transactional
    public void desasociarProductoDeFinca(Integer fincaId, Integer productoId) {
        ProductoFinca productoFinca = productoFincaRepository
            .buscarAsociacion(fincaId, productoId)
            .orElseThrow(() -> new RuntimeException(
                "No existe asociación entre finca " + fincaId + " y producto " + productoId
            ));

        productoFincaRepository.delete(productoFinca);
    }

    /**
     * Verifica si un producto está asociado a una finca
     *
     * SQL: SELECT COUNT(*) > 0 FROM tb_productos_fincas
     *      WHERE id_finca = ? AND id_producto = ?
     *
     * CASO DE USO: Validar antes de crear para mostrar mensaje
     */
    @Transactional(readOnly = true)
    public boolean estaAsociado(Integer fincaId, Integer productoId) {
        return productoFincaRepository.existeAsociacion(fincaId, productoId);
    }
}
