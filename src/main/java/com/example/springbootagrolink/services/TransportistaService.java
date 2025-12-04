package com.example.springbootagrolink.services;

import com.example.springbootagrolink.model.Transportista;
import com.example.springbootagrolink.repository.TransportistaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class TransportistaService {

    @Autowired
    private TransportistaRepository transportistaRepository;

    // Listar todos
    public List<Transportista> obtenerTodos() {
        return transportistaRepository.findAll();
    }

    // Buscar por ID
    public Optional<Transportista> obtenerPorId(Integer id) {
        return transportistaRepository.findById(id);
    }

    // Crear
    public Transportista crear(Transportista transportista) {
        return transportistaRepository.save(transportista);
    }

    // Actualizar
    public Transportista actualizar(Integer id, Transportista detalles) {
        Optional<Transportista> encontrado = transportistaRepository.findById(id);

        if (encontrado.isPresent()) {
            Transportista t = encontrado.get();

            t.setUsuario(detalles.getUsuario());
            t.setCalificacion(detalles.getCalificacion());
            t.setZonasEntrega(detalles.getZonasEntrega());

            return transportistaRepository.save(t);
        }

        return null;
    }

    // Eliminar
    public boolean eliminar(Integer id) {
        if (transportistaRepository.existsById(id)) {
            transportistaRepository.deleteById(id);
            return true;
        }
        return false;
    }
}


