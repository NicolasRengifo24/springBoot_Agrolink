package com.example.springbootagrolink.repository;

import com.example.springbootagrolink.model.Finca;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FincaRepository extends JpaRepository<Finca, Integer> {
}
