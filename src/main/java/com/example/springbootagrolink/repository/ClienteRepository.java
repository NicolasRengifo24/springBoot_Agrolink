package com.example.springbootagrolink.repository;

import com.example.springbootagrolink.model.Cliente;
import com.example.springbootagrolink.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ClienteRepository extends JpaRepository<Cliente, Integer> {

    /**
     * Encuentra un cliente por su usuario asociado
     * @param usuario El usuario asociado al cliente
     * @return Optional con el cliente si existe
     */
    Optional<Cliente> findByUsuario(Usuario usuario);
}
