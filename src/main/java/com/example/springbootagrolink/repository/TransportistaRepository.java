package com.example.springbootagrolink.repository;

import com.example.springbootagrolink.model.Transportista;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TransportistaRepository extends JpaRepository<Transportista, Integer> {

    // Sobrescribir findAll para cargar el usuario junto con el transportista
    @Override
    @EntityGraph(attributePaths = {"usuario"})
    List<Transportista> findAll();
}
