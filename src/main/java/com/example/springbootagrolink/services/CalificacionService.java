package com.example.springbootagrolink.services;

import com.example.springbootagrolink.model.Calificacion;
import com.example.springbootagrolink.repository.CalificacionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Service
public class CalificacionService {

    @Autowired
    private CalificacionRepository calificacionRepository;

    @Transactional(readOnly = true)
    public List<Calificacion> obtenerTodos() {
        return calificacionRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Optional<Calificacion> obtenerPorId(Integer id) {
        return calificacionRepository.findById(id);
    }

    @Transactional
    public Calificacion guardar(Calificacion entidad) {
        return calificacionRepository.save(entidad);
    }

    @Transactional
    public Calificacion actualizar(Integer id, Calificacion cambios) {
        Calificacion existente = calificacionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Calificación no encontrada con ID: " + id));
        existente.setPuntaje(cambios.getPuntaje());
        existente.setPromedio(cambios.getPromedio());
        return calificacionRepository.save(existente);
    }

    @Transactional
    public boolean eliminar(Integer id) {
        if (calificacionRepository.existsById(id)) {
            calificacionRepository.deleteById(id);
            return true;
        }
        return false;
    }

    // Búsqueda multicriterio: todos los parámetros son opcionales
    @Transactional(readOnly = true)
    public List<Calificacion> buscar(BigDecimal puntajeMin,
                                     BigDecimal puntajeMax,
                                     BigDecimal promedioMin,
                                     BigDecimal promedioMax) {
        Specification<Calificacion> spec = (root, query, cb) -> cb.conjunction();
        if (puntajeMin != null) {
            spec = spec.and((root, query, cb) -> cb.greaterThanOrEqualTo(root.get("puntaje"), puntajeMin));
        }
        if (puntajeMax != null) {
            spec = spec.and((root, query, cb) -> cb.lessThanOrEqualTo(root.get("puntaje"), puntajeMax));
        }
        if (promedioMin != null) {
            spec = spec.and((root, query, cb) -> cb.greaterThanOrEqualTo(root.get("promedio"), promedioMin));
        }
        if (promedioMax != null) {
            spec = spec.and((root, query, cb) -> cb.lessThanOrEqualTo(root.get("promedio"), promedioMax));
        }
        return calificacionRepository.findAll(spec);
    }
}
