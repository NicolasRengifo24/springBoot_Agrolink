package com.example.springbootagrolink.repository;

import com.example.springbootagrolink.model.Productor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;

@Repository
public interface ProductorRepository extends JpaRepository<Productor, Integer> {
    @Query("""
        SELECT DISTINCT p
        FROM Productor p
        LEFT JOIN p.calificacion c
        LEFT JOIN p.fincas f
        WHERE (:puntaje IS NULL OR c.puntaje = :puntaje)
          AND (:direccion IS NULL OR f.direccionFinca = :direccion)
    """)
    List<Productor> buscarPorCalificacionYDireccion(@Param("puntaje") BigDecimal puntaje,
                                                    @Param("direccion") String direccion);

}
