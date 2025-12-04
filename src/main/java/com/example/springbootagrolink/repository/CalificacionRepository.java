package com.example.springbootagrolink.repository;

import com.example.springbootagrolink.model.Calificacion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface CalificacionRepository extends JpaRepository<Calificacion, Integer>, JpaSpecificationExecutor<Calificacion> {
}
