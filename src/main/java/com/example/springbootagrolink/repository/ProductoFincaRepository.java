package com.example.springbootagrolink.repository;

import com.example.springbootagrolink.model.ProductoFinca;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProductoFincaRepository extends JpaRepository<ProductoFinca, Integer> {
}
