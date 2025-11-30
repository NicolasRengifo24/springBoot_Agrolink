package com.example.springbootagrolink.repository;

import com.example.springbootagrolink.model.Asesor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AsesorRepository extends JpaRepository<Asesor, Integer> {
}
