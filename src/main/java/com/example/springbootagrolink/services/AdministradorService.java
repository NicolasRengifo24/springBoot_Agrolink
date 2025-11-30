package com.example.springbootagrolink.services;

import com.example.springbootagrolink.model.Administrador;
import com.example.springbootagrolink.repository.AdministradorRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class AdministradorService {

    @Autowired
    private  AdministradorRepository administradorRepository;

    // Obtener todos los administradores
    public List<Administrador> obtenerTodosLosAdministradores() {
        return administradorRepository.findAll();
    }

    // Obtener administrador por ID
    public Optional <Administrador> obtenerAdministradorPorId(Integer id) {
        return administradorRepository.findById(id);
    }

    // Crear nuevo administrador
    public Administrador crearAdministrador(Administrador administrador) {
        return administradorRepository.save(administrador);
    }

    // Actualizar administrador
    public Administrador actualizarAdministrador(Integer id, Administrador administradorActualizado) {
        Optional<Administrador> administradorExistente = administradorRepository.findById(id);
        if (administradorExistente.isPresent()) {
            Administrador administrador = administradorExistente.get();
            administrador.setPrivilegiosAdmin(administradorActualizado.getPrivilegiosAdmin());
            return administradorRepository.save(administrador);
        }
        return null;
    }

    // Eliminar administrador
    public boolean eliminarAdministrador(Integer id) {
        if (administradorRepository.existsById(id)) {
            administradorRepository.deleteById(id);
            return true;
        }
        return  false;
    }


}
