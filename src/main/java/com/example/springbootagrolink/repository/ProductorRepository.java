package com.example.springbootagrolink.repository;

import com.example.springbootagrolink.model.Productor;
import org.hibernate.boot.models.JpaAnnotations;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProductorRepository extends JpaRepository<Productor, Integer> {
}
