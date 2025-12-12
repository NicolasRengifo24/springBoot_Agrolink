package com.example.springbootagrolink.repository;

import com.example.springbootagrolink.model.Productor;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Repository
public interface ProductorRepository extends JpaRepository<Productor, Integer> {

    // Sobrescribir findAll para cargar el usuario junto con el productor
    @Override
    @EntityGraph(attributePaths = {"usuario"})
    List<Productor> findAll();

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

    // Buscar productor por nombreUsuario del usuario
    Optional<Productor> findByUsuarioNombreUsuario(String nombreUsuario);

}
