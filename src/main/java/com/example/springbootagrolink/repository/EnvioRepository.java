package com.example.springbootagrolink.repository;

import com.example.springbootagrolink.model.Envio;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface EnvioRepository extends JpaRepository<Envio, Integer> {

    // Contar envíos finalizados (entregas exitosas)
    @Query("SELECT COUNT(e) FROM Envio e WHERE e.estadoEnvio = 'Finalizado'")
    Long countEnviosFinalizados();

    // No es necesario declarar count() porque ya está heredado de JpaRepository
    // El método count() devuelve long automáticamente
}
