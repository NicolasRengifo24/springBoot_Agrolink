package com.example.springbootagrolink.services;

import com.example.springbootagrolink.model.Certificado;
import com.example.springbootagrolink.model.Asesor;
import com.example.springbootagrolink.repository.CertificadoRepository;
import com.example.springbootagrolink.repository.AsesorRepository;
import com.example.springbootagrolink.services.Idao.Idao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class CertificadoService implements Idao<Certificado, Integer> {

    @Autowired
    private CertificadoRepository certificadoRepository;

    @Autowired
    private AsesorRepository asesorRepository;

    @Override
    @Transactional(readOnly = true)
    public List<Certificado> obtenerTodos() {
        return certificadoRepository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Certificado> obtenerPorId(Integer id) {
        return certificadoRepository.findById(id);
    }

    @Override
    @Transactional
    public Certificado guardar(Certificado entidad) {
        // Validar asesor
        if (entidad.getAsesor() == null || entidad.getAsesor().getIdUsuario() == null) {
            throw new IllegalArgumentException("Debe especificar un asesor válido para el certificado");
        }
        Integer asesorId = entidad.getAsesor().getIdUsuario();
        if (!asesorRepository.existsById(asesorId)) {
            throw new IllegalArgumentException("El asesor con ID " + asesorId + " no existe");
        }
        // Validar campos obligatorios
        if (entidad.getTipoCertificado() == null || entidad.getTipoCertificado().isBlank()) {
            throw new IllegalArgumentException("El tipo de certificado es obligatorio");
        }
        if (entidad.getDescripcionCert() == null || entidad.getDescripcionCert().isBlank()) {
            throw new IllegalArgumentException("La descripción del certificado es obligatoria");
        }
        if (entidad.getFechaExpedicion() == null) {
            throw new IllegalArgumentException("La fecha de expedición es obligatoria");
        }
        return certificadoRepository.save(entidad);
    }

    @Override
    @Transactional
    public Certificado actualizar(Integer id, Certificado cambios) {
        Certificado existente = certificadoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Certificado no encontrado con ID: " + id));
        // Actualización parcial
        if (cambios.getTipoCertificado() != null && !cambios.getTipoCertificado().isBlank()) {
            existente.setTipoCertificado(cambios.getTipoCertificado());
        }
        if (cambios.getDescripcionCert() != null && !cambios.getDescripcionCert().isBlank()) {
            existente.setDescripcionCert(cambios.getDescripcionCert());
        }
        if (cambios.getFechaExpedicion() != null) {
            existente.setFechaExpedicion(cambios.getFechaExpedicion());
        }
        if (cambios.getAsesor() != null && cambios.getAsesor().getIdUsuario() != null) {
            Integer nuevoAsesorId = cambios.getAsesor().getIdUsuario();
            if (!asesorRepository.existsById(nuevoAsesorId)) {
                throw new IllegalArgumentException("El asesor con ID " + nuevoAsesorId + " no existe");
            }
            existente.setAsesor(cambios.getAsesor());
        }
        return certificadoRepository.save(existente);
    }

    @Override
    @Transactional
    public boolean eliminar(Integer id) {
        if (certificadoRepository.existsById(id)) {
            certificadoRepository.deleteById(id);
            return true;
        }
        return false;
    }

    // Filtro opcional por asesor
    @Transactional(readOnly = true)
    public List<Certificado> obtenerPorAsesor(Integer asesorId) {
        return certificadoRepository.findByAsesor_IdUsuario(asesorId);
    }
}
