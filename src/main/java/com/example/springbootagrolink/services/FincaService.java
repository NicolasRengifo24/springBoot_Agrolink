package com.example.springbootagrolink.services;

import com.example.springbootagrolink.model.Finca;
import com.example.springbootagrolink.model.Productor;
import com.example.springbootagrolink.repository.FincaRepository;
import com.example.springbootagrolink.repository.ProductorRepository;
import com.example.springbootagrolink.services.Idao.Idao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * Servicio para gestionar las operaciones CRUD de la entidad Finca
 *
 * ANÁLISIS DE LA RELACIÓN:
 * ========================
 * Finca tiene una relación @ManyToOne con Productor:
 * - Muchas Fincas pueden pertenecer a UN Productor
 * - Un Productor puede tener MUCHAS Fincas
 *
 * Campos importantes:
 * - nombreFinca: nombre de la finca
 * - direccionFinca: ubicación de la finca
 * - certificados: BPA, MIRFE, MIPE (con valores por defecto)
 * - registroICA: registro ICA (con valor por defecto)
 * - productor: relación con el Productor dueño
 *
 * ¿POR QUÉ 2 REPOSITORIOS?
 * ========================
 * - FincaRepository: para operaciones sobre Finca
 * - ProductorRepository: para validar que el productor existe antes de asignar una finca
 */
@Service
public class FincaService implements Idao<Finca, Integer> {

    @Autowired
    private FincaRepository fincaRepository;

    @Autowired
    private ProductorRepository productorRepository;

    // ==================== MÉTODOS DE IDAO (CRUD BÁSICO) ====================

    /**
     * Obtiene todas las fincas de la base de datos
     */
    @Override
    @Transactional(readOnly = true)
    public List<Finca> obtenerTodos() {
        return fincaRepository.findAll();
    }

    /**
     * Busca una finca por su ID único
     */
    @Override
    @Transactional(readOnly = true)
    public Optional<Finca> obtenerPorId(Integer id) {
        return fincaRepository.findById(id);
    }

    /**
     * Guarda una nueva finca en la base de datos
     *
     * VALIDACIONES:
     * 1. Verifica que tenga un Productor asignado
     * 2. Verifica que el Productor exista en la BD
     */
    @Override
    @Transactional
    public Finca guardar(Finca entidad) {
        // Validación 1: Debe tener un Productor asignado
        if (entidad.getProductor() == null || entidad.getProductor().getIdProductor() == null) {
            throw new IllegalArgumentException("Debe especificar un productor válido para la finca");
        }

        // Validación 2: El Productor debe existir en la base de datos
        Integer productorId = entidad.getProductor().getIdProductor();
        if (!productorRepository.existsById(productorId)) {
            throw new IllegalArgumentException("El productor con ID " + productorId + " no existe");
        }

        // Guardar la finca
        return fincaRepository.save(entidad);
    }

    /**
     * Actualiza una finca existente
     *
     * Campos que se pueden actualizar:
     * - nombreFinca
     * - direccionFinca
     * - certificados (BPA, MIRFE, MIPE)
     * - registroICA
     * - productor (cambiar de dueño)
     */

    @Override
    @Transactional
    public Finca actualizar(Integer id, Finca entidad) {
        // Verificar que la finca existe
        Finca fincaExistente = fincaRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Finca no encontrada con ID: " + id));

        // Actualizar los campos
        fincaExistente.setNombreFinca(entidad.getNombreFinca());
        fincaExistente.setDireccionFinca(entidad.getDireccionFinca());
        fincaExistente.setCertificadoBPA(entidad.getCertificadoBPA());
        fincaExistente.setCertificadoMIRFE(entidad.getCertificadoMIRFE());
        fincaExistente.setCertificadoMIPE(entidad.getCertificadoMIPE());
        fincaExistente.setRegistroICA(entidad.getRegistroICA());

        // Si se cambia el productor, validar y actualizar
        if (entidad.getProductor() != null) {
            Integer nuevoProductorId = entidad.getProductor().getIdProductor();
            if (nuevoProductorId == null) {
                throw new IllegalArgumentException("Debe especificar un ID de productor válido al actualizar la finca");
            }
            if (!productorRepository.existsById(nuevoProductorId)) {
                throw new IllegalArgumentException("El productor con ID " + nuevoProductorId + " no existe");
            }
            fincaExistente.setProductor(entidad.getProductor());
        }

        return fincaRepository.save(fincaExistente);
    }

    /**
     * Elimina una finca por su ID
     *
     * IMPORTANTE: Si la finca tiene productos asociados (en ProductoFinca),
     * puede fallar por integridad referencial. Considera eliminar primero
     * las asociaciones en ProductoFinca.
     */
    @Override
    @Transactional
    public boolean eliminar(Integer id) {
        if (fincaRepository.existsById(id)) {
            fincaRepository.deleteById(id);
            return true;
        }
        return false;
    }



    // ==================== MÉTODOS PERSONALIZADOS ====================

    /**
     * Obtiene todas las fincas de un productor específico
     *
     * CASO DE USO: Un productor quiere ver todas sus fincas
     */
    @Transactional(readOnly = true)
    public List<Finca> obtenerFincasPorProductor(Integer productorId) {
        return fincaRepository.findByProductorIdProductor(productorId);
    }



    /**
     * Asigna una finca a un productor
     *
     * CASO DE USO: Cambiar el dueño de una finca
     */
    @Transactional
    public Finca asignarProductor(Integer fincaId, Integer productorId) {
        // Buscar la finca
        Finca finca = fincaRepository.findById(fincaId)
            .orElseThrow(() -> new RuntimeException("Finca no encontrada con ID: " + fincaId));

        // Buscar el productor
        Productor productor = productorRepository.findById(productorId)
            .orElseThrow(() -> new RuntimeException("Productor no encontrado con ID: " + productorId));

        // Asignar el productor a la finca
        finca.setProductor(productor);

        return fincaRepository.save(finca);
    }
}
