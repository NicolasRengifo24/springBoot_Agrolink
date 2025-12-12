package com.example.springbootagrolink.repository;

import com.example.springbootagrolink.model.Cliente;
import com.example.springbootagrolink.model.Usuario;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ClienteRepository extends JpaRepository<Cliente, Integer> {

    // Sobrescribir findAll para cargar el usuario junto con el cliente
    @Override
    @EntityGraph(attributePaths = {"usuario"})
    List<Cliente> findAll();

    /**
     * Encuentra un cliente por su usuario asociado
     * @param usuario El usuario asociado al cliente
     * @return Optional con el cliente si existe
     */
    Optional<Cliente> findByUsuario(Usuario usuario);
}
