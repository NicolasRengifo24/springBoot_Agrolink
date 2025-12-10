package com.example.springbootagrolink.repository;

import com.example.springbootagrolink.model.Calificacion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;

@Repository
public interface CalificacionRepository extends JpaRepository<Calificacion, Integer>, JpaSpecificationExecutor<Calificacion> {

    // filtrar por puntaje y promedio con JPQL
    @Query("SELECT c FROM Calificacion c \n" +
           "WHERE (:puntajeMin IS NULL OR c.puntaje >= :puntajeMin) \n" +
           "AND (:puntajeMax IS NULL OR c.puntaje <= :puntajeMax) \n" +
           "AND (:promedioMin IS NULL OR c.promedio >= :promedioMin) \n" +
           "AND (:promedioMax IS NULL OR c.promedio <= :promedioMax)")
    List<Calificacion> buscarPorCriterios(
            @Param("puntajeMin") BigDecimal puntajeMin,
            @Param("puntajeMax") BigDecimal puntajeMax,
            @Param("promedioMin") BigDecimal promedioMin,
            @Param("promedioMax") BigDecimal promedioMax
    );

    // Calcular el promedio general de todas las calificaciones
    @Query("SELECT AVG(c.promedio) FROM Calificacion c")
    Double calcularPromedioGeneral();
}
