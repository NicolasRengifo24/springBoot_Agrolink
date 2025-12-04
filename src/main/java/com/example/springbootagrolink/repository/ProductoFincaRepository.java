package com.example.springbootagrolink.repository;

import com.example.springbootagrolink.model.ProductoFinca;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repositorio para ProductoFinca (tabla intermedia entre Finca y Producto)
 *
 * ¿Por qué necesitamos métodos adicionales aquí?
 *
 * JpaRepository ya nos da:
 * - findAll() → Todas las asociaciones
 * - findById(id) → Una asociación específica por su ID
 * - save() → Guardar/actualizar
 * - deleteById(id) → Eliminar
 *
 * PERO necesitamos consultas más específicas para esta relación N:N:
 * 1. Ver productos de UNA finca → caso de uso: "Ver mi catálogo de productos"
 * 2. Ver fincas de UN producto → caso de uso: "¿Dónde comprar Aguacate?"
 * 3. Verificar duplicados → evitar asociar el mismo producto 2 veces a una finca
 *
 * Usamos @Query para tener nombres de métodos más claros y legibles
 */
@Repository
public interface ProductoFincaRepository extends JpaRepository<ProductoFinca, Integer> {

    /**
     * Busca todos los productos cultivados en una finca específica
     *
     * CASO DE USO: Un productor quiere ver qué productos cultiva en su finca
     *
     * @Query personalizado:
     * - Usamos JPQL (Java Persistence Query Language)
     * - "pf" es el alias de ProductoFinca
     * - "pf.finca.idFinca" navega por la relación @ManyToOne
     * - :idFinca es un parámetro nombrado
     *
     * SQL generado:
     * SELECT pf.* FROM tb_productos_fincas pf WHERE pf.id_finca = ?
     *
     * @param idFinca ID de la finca
     * @return Lista de ProductoFinca (cada uno tiene producto, cantidad, fecha)
     */
    @Query("SELECT pf FROM ProductoFinca pf WHERE pf.finca.idFinca = :idFinca")
    List<ProductoFinca> buscarProductosDeFinca(@Param("idFinca") Integer idFinca);

    /**
     * Busca todas las fincas que cultivan un producto específico
     *
     * CASO DE USO: Un comprador busca "¿Dónde puedo comprar Aguacate?"
     *
     * SQL generado:
     * SELECT pf.* FROM tb_productos_fincas pf WHERE pf.id_producto = ?
     *
     * @param idProducto ID del producto
     * @return Lista de ProductoFinca (cada uno tiene finca, cantidad, fecha)
     */
    @Query("SELECT pf FROM ProductoFinca pf WHERE pf.producto.idProducto = :idProducto")
    List<ProductoFinca> buscarFincasConProducto(@Param("idProducto") Integer idProducto);

    /**
     * Verifica si ya existe una asociación entre finca y producto
     *
     * CASO DE USO: EVITAR DUPLICADOS antes de crear una asociación
     *
     * COUNT(pf) > 0:
     * - Si hay registros, retorna true
     * - Si no hay registros, retorna false
     *
     * SQL generado:
     * SELECT COUNT(pf) > 0 FROM tb_productos_fincas pf
     * WHERE pf.id_finca = ? AND pf.id_producto = ?
     *
     * @param idFinca ID de la finca
     * @param idProducto ID del producto
     * @return true si ya existe la asociación, false si no
     */
    @Query("SELECT COUNT(pf) > 0 FROM ProductoFinca pf WHERE pf.finca.idFinca = :idFinca AND pf.producto.idProducto = :idProducto")
    boolean existeAsociacion(@Param("idFinca") Integer idFinca, @Param("idProducto") Integer idProducto);

    /**
     * Busca una asociación específica entre finca y producto
     *
     * CASO DE USO: Para actualizar si existe, o crear si no existe (en asociarProductoConFinca)
     *
     * SQL generado:
     * SELECT pf.* FROM tb_productos_fincas pf
     * WHERE pf.id_finca = ? AND pf.id_producto = ?
     *
     * @param idFinca ID de la finca
     * @param idProducto ID del producto
     * @return Optional con la asociación si existe, vacío si no
     */
    @Query("SELECT pf FROM ProductoFinca pf WHERE pf.finca.idFinca = :idFinca AND pf.producto.idProducto = :idProducto")
    Optional<ProductoFinca> buscarAsociacion(@Param("idFinca") Integer idFinca, @Param("idProducto") Integer idProducto);
}
