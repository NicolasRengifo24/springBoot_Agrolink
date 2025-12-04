package com.example.springbootagrolink.services;

import com.example.springbootagrolink.model.CategoriaProducto;
import com.example.springbootagrolink.repository.CategoriaProductoRepository;
import com.example.springbootagrolink.services.Idao.Idao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class CategoriaProductoService implements Idao<CategoriaProducto, Integer> {

    @Autowired
    private CategoriaProductoRepository categoriaProductoRepository;

    @Override
    @Transactional(readOnly = true)
    public List<CategoriaProducto> obtenerTodos() {
        return categoriaProductoRepository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<CategoriaProducto> obtenerPorId(Integer id) {
        return categoriaProductoRepository.findById(id);
    }

    @Override
    @Transactional
    public CategoriaProducto guardar(CategoriaProducto entidad) {
        return categoriaProductoRepository.save(entidad);
    }

    @Override
    @Transactional
    public CategoriaProducto actualizar(Integer id, CategoriaProducto cambios) {
        CategoriaProducto existente = categoriaProductoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Categoría no encontrada con ID: " + id));
        // Actualizar campos simples
        existente.setNombreCategoria(cambios.getNombreCategoria());
        return categoriaProductoRepository.save(existente);
    }

    @Override
    @Transactional
    public boolean eliminar(Integer id) {
        if (categoriaProductoRepository.existsById(id)) {
            categoriaProductoRepository.deleteById(id);
            return true;
        }
        return false;
    }
}
