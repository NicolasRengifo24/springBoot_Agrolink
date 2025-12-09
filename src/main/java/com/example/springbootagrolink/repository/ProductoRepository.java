package com.example.springbootagrolink.repository;

import com.example.springbootagrolink.model.Producto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductoRepository extends JpaRepository<Producto, Integer>, JpaSpecificationExecutor<Producto> {

    @Query("SELECT DISTINCT p FROM Producto p " +
            "LEFT JOIN FETCH p.imagenesProducto " +
            "JOIN p.productor prod " +
            "JOIN prod.usuario u " +
            "LEFT JOIN p.categoria c " +
            "WHERE (:ubicacion IS NULL OR LOWER(u.ciudad) LIKE LOWER(CONCAT('%', :ubicacion, '%'))) " +
            "AND (:categoriaId IS NULL OR c.idCategoria = :categoriaId)")
    List<Producto> buscarPorUbicacionYCategoria(@Param("ubicacion") String ubicacion,
                                                @Param("categoriaId") Integer categoriaId);

    // Buscar productos por categoría
    @Query("SELECT DISTINCT p FROM Producto p LEFT JOIN FETCH p.imagenesProducto WHERE p.categoria.idCategoria = :categoriaId")
    List<Producto> findByCategoriaIdCategoria(@Param("categoriaId") Integer categoriaId);

    // Buscar productos por nombre (búsqueda básica)
    @Query("SELECT DISTINCT p FROM Producto p LEFT JOIN FETCH p.imagenesProducto WHERE LOWER(p.nombreProducto) LIKE LOWER(CONCAT('%', :nombre, '%'))")
    List<Producto> findByNombreProductoContainingIgnoreCase(@Param("nombre") String nombre);

    // Obtener todos los productos con sus imágenes (JOIN FETCH para evitar N+1)
    @Query("SELECT DISTINCT p FROM Producto p LEFT JOIN FETCH p.imagenesProducto")
    List<Producto> findAllWithImages();

    // Búsqueda avanzada en múltiples campos
    @Query("SELECT DISTINCT p FROM Producto p " +
           "LEFT JOIN FETCH p.imagenesProducto " +
           "LEFT JOIN p.categoria c " +
           "LEFT JOIN p.productor prod " +
           "LEFT JOIN prod.usuario u " +
           "WHERE LOWER(p.nombreProducto) LIKE LOWER(CONCAT('%', :termino, '%')) " +
           "OR LOWER(p.descripcionProducto) LIKE LOWER(CONCAT('%', :termino, '%')) " +
           "OR LOWER(c.nombreCategoria) LIKE LOWER(CONCAT('%', :termino, '%')) " +
           "OR LOWER(u.nombre) LIKE LOWER(CONCAT('%', :termino, '%')) " +
           "OR LOWER(u.apellido) LIKE LOWER(CONCAT('%', :termino, '%')) " +
           "OR LOWER(u.ciudad) LIKE LOWER(CONCAT('%', :termino, '%'))")
    List<Producto> busquedaAvanzada(@Param("termino") String termino);

    // Filtrar productos por ubicación
    @Query("SELECT DISTINCT p FROM Producto p " +
           "LEFT JOIN FETCH p.imagenesProducto " +
           "LEFT JOIN p.productor prod " +
           "LEFT JOIN prod.usuario u " +
           "WHERE LOWER(u.ciudad) LIKE LOWER(CONCAT('%', :ubicacion, '%'))")
    List<Producto> filtrarPorUbicacion(@Param("ubicacion") String ubicacion);

    // Filtrar productos por rango de precio
    @Query("SELECT DISTINCT p FROM Producto p LEFT JOIN FETCH p.imagenesProducto WHERE " +
           "(:precioMin IS NULL OR p.precio >= :precioMin) AND " +
           "(:precioMax IS NULL OR p.precio <= :precioMax)")
    List<Producto> filtrarPorRangoPrecio(@Param("precioMin") Double precioMin, @Param("precioMax") Double precioMax);

    // Filtrar por múltiples criterios (versión simplificada)
    @Query("SELECT DISTINCT p FROM Producto p " +
           "LEFT JOIN FETCH p.imagenesProducto " +
           "LEFT JOIN p.categoria c " +
           "LEFT JOIN p.productor prod " +
           "LEFT JOIN prod.usuario u " +
           "WHERE (:categoriaId IS NULL OR c.idCategoria = :categoriaId) " +
           "AND (:ubicacion IS NULL OR LOWER(u.ciudad) LIKE LOWER(CONCAT('%', :ubicacion, '%'))) " +
           "AND (:precioMin IS NULL OR p.precio >= :precioMin) " +
           "AND (:precioMax IS NULL OR p.precio <= :precioMax)")
    List<Producto> filtrarPorMultiplesCriterios(@Param("categoriaId") Integer categoriaId,
                                               @Param("ubicacion") String ubicacion,
                                               @Param("precioMin") Double precioMin,
                                               @Param("precioMax") Double precioMax,
                                               @Param("calificacionMin") Double calificacionMin);

    // Obtener ubicaciones disponibles (ciudades de productores)
    @Query("SELECT DISTINCT u.ciudad FROM Producto p " +
           "JOIN p.productor prod " +
           "JOIN prod.usuario u " +
           "WHERE u.ciudad IS NOT NULL " +
           "ORDER BY u.ciudad")
    List<String> obtenerUbicacionesDisponibles();

    // Obtener precio mínimo de productos
    @Query("SELECT MIN(p.precio) FROM Producto p WHERE p.precio > 0")
    Double obtenerPrecioMinimo();

    // Obtener precio máximo de productos
    @Query("SELECT MAX(p.precio) FROM Producto p")
    Double obtenerPrecioMaximo();

    // Filtrar productos por calificación (versión simplificada - por ahora devuelve todos)
    @Query("SELECT p FROM Producto p")
    List<Producto> filtrarPorCalificacion(@Param("calificacionMin") Double calificacionMin);

    // Obtener el producto más vendido (versión simplificada)
    @Query("SELECT p FROM Producto p ORDER BY p.idProducto DESC")
    List<Producto> obtenerProductoMasVendido();

    // Método de respaldo para obtener el producto más reciente si no hay compras
    Producto findFirstByOrderByIdProductoDesc();
}
