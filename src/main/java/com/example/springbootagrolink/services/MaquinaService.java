package com.example.springbootagrolink.services;

import com.example.springbootagrolink.model.Maquina;
import com.example.springbootagrolink.repository.MaquinaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class MaquinaService {

    @Autowired
    private MaquinaRepository maquinaRepository;

    // Listar todas las máquinas
    public List<Maquina> obtenerTodasLasMaquinas() {
        return maquinaRepository.findAll();
    }

    // Buscar por ID
    public Optional<Maquina> obtenerMaquinaPorId(Integer id) {
        return maquinaRepository.findById(id);
    }

    // Crear o actualizar
    public Maquina guardarMaquina(Maquina maquina) {
        return maquinaRepository.save(maquina);
    }

    // Eliminar por ID
    public void eliminarMaquina(Integer id) {
        maquinaRepository.deleteById(id);
    }
}
