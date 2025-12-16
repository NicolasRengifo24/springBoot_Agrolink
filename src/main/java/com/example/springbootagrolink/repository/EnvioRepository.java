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

    // Buscar envíos por transportista con relaciones inicializadas (sin referencias a campos inexistentes)
    @Query("SELECT DISTINCT e FROM Envio e " +
           "LEFT JOIN FETCH e.compra c " +
           "LEFT JOIN FETCH c.cliente cl " +
           "LEFT JOIN FETCH cl.usuario cu " +
           "WHERE e.transportista.idUsuario = :idUsuario")
    List<Envio> findByTransportista_IdUsuario(@Param("idUsuario") Integer idUsuario);

    // Buscar envíos por estado - versión simple sin FETCH
    @Query("SELECT e FROM Envio e WHERE e.estadoEnvio = :estado ORDER BY e.idEnvio DESC")
    List<Envio> findByEstadoEnvio(@Param("estado") Envio.EstadoEnvio estado);

    // Método alternativo con FETCH JOIN para cuando se necesitan relaciones (sin c.producto)
    @Query("SELECT DISTINCT e FROM Envio e " +
           "LEFT JOIN FETCH e.compra c " +
           "LEFT JOIN FETCH c.cliente cl " +
           "LEFT JOIN FETCH cl.usuario cu " +
           "WHERE e.estadoEnvio = :estado")
    List<Envio> findByEstadoEnvioWithRelations(@Param("estado") Envio.EstadoEnvio estado);

    // Método nativo SQL como alternativa para debugging
    @Query(value = "SELECT * FROM tb_envios WHERE estado_envio = ?1 ORDER BY id_envio DESC", nativeQuery = true)
    List<Envio> findByEstadoEnvioNative(String estado);

    // Obtener envíos disponibles con nombre del cliente usando SQL nativa
    @Query(value = "SELECT e.id_envio, e.direccion_origen, e.direccion_destino, e.distancia_km, " +
           "e.peso_total_kg, e.costo_total, e.estado_envio, e.id_compra, " +
           "COALESCE(u.nombre, 'Por asignar') as nombre_cliente " +
           "FROM tb_envios e " +
           "LEFT JOIN tb_compras c ON e.id_compra = c.id_compra " +
           "LEFT JOIN tb_clientes cl ON c.id_cliente = cl.id_cliente " +
           "LEFT JOIN tb_usuarios u ON cl.id_usuario = u.id_usuario " +
           "WHERE e.estado_envio = ?1 " +
           "ORDER BY e.id_envio DESC", nativeQuery = true)
    List<Object[]> findEnviosDisponiblesConCliente(String estado);

    // Buscar envíos disponibles sin fechas asignadas (realmente disponibles para aceptar)
    @Query("SELECT e FROM Envio e WHERE e.estadoEnvio = :estado " +
           "AND e.fechaSalida IS NULL AND e.fechaEntrega IS NULL " +
           "ORDER BY e.idEnvio DESC")
    List<Envio> findEnviosDisponiblesSinFechas(@Param("estado") Envio.EstadoEnvio estado);

}
