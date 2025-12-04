package com.example.springbootagrolink.services;

import com.example.springbootagrolink.model.Asesor;
import com.example.springbootagrolink.repository.AsesorRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class AsesorService {

    @Autowired
    private AsesorRepository asesorRepository;

    // Obtener todos los asesores
    public List<Asesor> obtenerTodosLosAsesores() {
        return asesorRepository.findAll();
    }

    // Obtener asesor por ID
    public Optional<Asesor> obtenerAsesorPorId(Integer id) {
        return asesorRepository.findById(id);
    }

    // Crear nuevo asesor
    public Asesor crearAsesor(Asesor asesor) {
        return asesorRepository.save(asesor);
    }

    // Actualizar asesor
    public Asesor actualizarAsesor(Integer id, Asesor asesorActualizado) {
        Optional<Asesor> asesorExistente = asesorRepository.findById(id);

        if (asesorExistente.isPresent()) {
            Asesor asesor = asesorExistente.get();

            // Actualizar los campos existentes en tu modelo
            asesor.setUsuario(asesorActualizado.getUsuario());
            asesor.setCalificacion(asesorActualizado.getCalificacion());
            asesor.setTipoAsesoria(asesorActualizado.getTipoAsesoria());

            return asesorRepository.save(asesor);
        }
        return null;
    }

    // Eliminar asesor
    public boolean eliminarAsesor(Integer id) {
        if (asesorRepository.existsById(id)) {
            asesorRepository.deleteById(id);
            return true;
        }
        return false;
    }
}

