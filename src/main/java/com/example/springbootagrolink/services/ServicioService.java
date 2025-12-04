package com.example.springbootagrolink.services;

import com.example.springbootagrolink.model.Servicio;
import com.example.springbootagrolink.repository.ServicioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ServicioService {

    @Autowired
    private ServicioRepository servicioRepository;

    // Obtener todos los servicios
    public List<Servicio> obtenerTodosLosServicios() {
        return servicioRepository.findAll();
    }

    // Obtener servicio por ID
    public Optional<Servicio> obtenerServicioPorId(Integer id) {
        return servicioRepository.findById(id);
    }

    // Crear un servicio nuevo
    public Servicio crearServicio(Servicio servicio) {
        return servicioRepository.save(servicio);
    }

    // Actualizar un servicio existente
    public Servicio actualizarServicio(Integer id, Servicio servicioActualizado) {
        Optional<Servicio> servicioExistente = servicioRepository.findById(id);

        if (servicioExistente.isPresent()) {
            Servicio servicio = servicioExistente.get();

            // Actualizar los campos disponibles
            servicio.setAsesor(servicioActualizado.getAsesor());
            servicio.setDescripcion(servicioActualizado.getDescripcion());
            servicio.setEstado(servicioActualizado.getEstado());

            return servicioRepository.save(servicio);
        }

        return null;
    }

    // Eliminar servicio por ID
    public boolean eliminarServicio(Integer id) {
        if (servicioRepository.existsById(id)) {
            servicioRepository.deleteById(id);
            return true;
        }
        return false;
    }
}

