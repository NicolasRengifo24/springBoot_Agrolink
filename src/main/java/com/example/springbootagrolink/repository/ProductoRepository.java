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

    @Query("SELECT p FROM Producto p " +
            "JOIN p.productor prod " +
            "JOIN prod.usuario u " +
            "LEFT JOIN p.categoria c " +
            "WHERE (:ubicacion IS NULL OR LOWER(u.ciudad) LIKE LOWER(CONCAT('%', :ubicacion, '%'))) " +
            "AND (:categoriaId IS NULL OR c.idCategoria = :categoriaId)")
    List<Producto> buscarPorUbicacionYCategoria(@Param("ubicacion") String ubicacion,
                                                @Param("categoriaId") Integer categoriaId);
}
