package com.example.springbootagrolink.repository;

import com.example.springbootagrolink.model.Servicio;
import org.hibernate.boot.models.JpaAnnotations;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ServicioRepository extends JpaRepository<Servicio, Integer> {

    // Sobrescribir findAll para cargar el usuario junto con el servicio/asesor
    @Override
    @EntityGraph(attributePaths = {"usuario"})
    List<Servicio> findAll();
}
