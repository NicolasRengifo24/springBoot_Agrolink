package com.example.springbootagrolink.repository;

import com.example.springbootagrolink.model.Producto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductoRepository extends JpaRepository<Producto, Integer> {

    @Query("""
        SELECT DISTINCT p
        FROM Producto p
        JOIN p.categoria c
        JOIN com.example.springbootagrolink.model.ProductoFinca pf ON pf.producto = p
        JOIN pf.finca f
        WHERE (:ubicacion IS NULL OR LOWER(f.direccionFinca) LIKE LOWER(CONCAT('%', :ubicacion, '%')))
          AND (:categoriaId IS NULL OR c.idCategoria = :categoriaId)
    """)
    List<Producto> buscarPorUbicacionYCategoria(@Param("ubicacion") String ubicacion,
                                                @Param("categoriaId") Integer categoriaId);
}
