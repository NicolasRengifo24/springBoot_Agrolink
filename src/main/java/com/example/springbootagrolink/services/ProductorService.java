package com.example.springbootagrolink.services;

import com.example.springbootagrolink.model.Productor;
import com.example.springbootagrolink.repository.ProductoRepository;
import com.example.springbootagrolink.repository.ProductorRepository;
import com.example.springbootagrolink.services.Idao.Idao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ProductorService implements Idao<Productor, Integer> {
    @Autowired
    private ProductorRepository productorRepository;

    @Override
    public List<Productor> obtenerTodos() {
        return productorRepository.findAll();
    }

    @Override
    public Optional <Productor> obtenerPorId(Integer id){
        return productorRepository.findById(id);
    }

    // Nuevo método para buscar productor por nombreUsuario
    public Optional<Productor> obtenerPorNombreUsuario(String nombreUsuario) {
        return productorRepository.findByUsuarioNombreUsuario(nombreUsuario);
    }

    @Override
    public Productor guardar(Productor productor) {
        return productorRepository.save(productor);
    }

    @Override
    public Productor actualizar(Integer id, Productor productor) {
        Optional<Productor> productorEscistente = productorRepository.findById(id);
        if (productorEscistente.isPresent()) {
            Productor prodActual = productorEscistente.get();
            prodActual.setCalificacion(productor.getCalificacion());
            prodActual.setTipoCultivo(productor.getTipoCultivo());
            return productorRepository.save(prodActual);
        } else {
            return null;
        }
    }

    @Override
    public boolean eliminar(Integer id) {
        if (productorRepository.existsById(id)) {
            productorRepository.deleteById(id);
            return true;
        } else {
            return false;
        }
    }
}
