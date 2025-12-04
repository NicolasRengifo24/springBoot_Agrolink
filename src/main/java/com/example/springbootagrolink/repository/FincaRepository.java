package com.example.springbootagrolink.repository;

import com.example.springbootagrolink.model.Finca;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FincaRepository extends JpaRepository<Finca, Integer> {

    /**
     * Busca todas las fincas de un productor específico
     *
     * CASO DE USO: Un productor quiere ver todas sus fincas
     **/
    List<Finca> findByProductorIdProductor(Integer idProductor);

    /**
     * Cuenta cuántas fincas tiene un productor
     *
     * CASO DE USO: "Este productor tiene 5 fincas"
     */

    long countByProductorIdProductor(Integer idProductor);
}
