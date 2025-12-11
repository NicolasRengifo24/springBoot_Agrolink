package com.example.springbootagrolink.repository;

import com.example.springbootagrolink.model.Vehiculo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface VehiculoRepository extends JpaRepository<Vehiculo, Integer> {

    // Buscar vehículos por transportista
    List<Vehiculo> findByTransportista_IdUsuario(Integer idUsuario);
}
