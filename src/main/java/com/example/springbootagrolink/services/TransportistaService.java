package com.example.springbootagrolink.services;

import com.example.springbootagrolink.model.Transportista;
import com.example.springbootagrolink.model.Usuario;
import com.example.springbootagrolink.repository.TransportistaRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class TransportistaService {

    private static final Logger log = LoggerFactory.getLogger(TransportistaService.class);

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

    /**
     * Crear automáticamente un registro de transportista si no existe
     */
    @Transactional
    public Transportista crearTransportistaAutomatico(Usuario usuario) {
        try {
            log.info("Creando transportista automáticamente para usuario: {}", usuario.getNombreUsuario());

            Transportista transportista = new Transportista();
            transportista.setIdUsuario(usuario.getIdUsuario());
            transportista.setUsuario(usuario);
            transportista.setZonasEntrega("No especificado");
            transportista.setCalificacion(null);

            transportista = transportistaRepository.saveAndFlush(transportista);
            log.info("✓ Transportista creado exitosamente para usuario: {}", usuario.getNombreUsuario());
            return transportista;
        } catch (Exception e) {
            log.error("✗ Error al crear transportista automáticamente: {}", e.getMessage(), e);
            throw new RuntimeException("No se pudo crear el registro de transportista: " + e.getMessage());
        }
    }
}


