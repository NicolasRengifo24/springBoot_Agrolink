package com.example.springbootagrolink.repository;

import com.example.springbootagrolink.model.Maquina;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MaquinaRepository extends JpaRepository<Maquina, Integer> {
}
