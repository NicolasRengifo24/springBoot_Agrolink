package com.example.springbootagrolink.repository;

import com.example.springbootagrolink.model.Envio;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EnvioRepository extends JpaRepository<Envio, Integer> {

    // Contar envíos finalizados (entregas exitosas)
    @Query("SELECT COUNT(e) FROM Envio e WHERE e.estadoEnvio = 'Finalizado'")
    Long countEnviosFinalizados();

    // Buscar envíos por transportista con relaciones inicializadas
    @Query("SELECT DISTINCT e FROM Envio e " +
           "LEFT JOIN FETCH e.compra c " +
           "LEFT JOIN FETCH c.cliente cl " +
           "LEFT JOIN FETCH cl.usuario cu " +
           "WHERE e.transportista.idUsuario = :idUsuario")
    List<Envio> findByTransportista_IdUsuario(@Param("idUsuario") Integer idUsuario);

    // Buscar envíos por estado con relaciones inicializadas
    @Query("SELECT DISTINCT e FROM Envio e " +
           "LEFT JOIN FETCH e.compra c " +
           "LEFT JOIN FETCH c.cliente cl " +
           "LEFT JOIN FETCH cl.usuario cu " +
           "WHERE e.estadoEnvio = :estado")
    List<Envio> findByEstadoEnvio(@Param("estado") Envio.EstadoEnvio estado);

    // No es necesario declarar count() porque ya está heredado de JpaRepository
    // El método count() devuelve long automáticamente
}


