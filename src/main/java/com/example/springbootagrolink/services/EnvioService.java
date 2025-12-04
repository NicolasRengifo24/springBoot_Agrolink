package com.example.springbootagrolink.services;

import com.example.springbootagrolink.model.Envio;
import com.example.springbootagrolink.repository.EnvioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class EnvioService {

    @Autowired
    private EnvioRepository envioRepository;

    // Obtener todos los envíos
    public List<Envio> obtenerTodos() {
        return envioRepository.findAll();
    }

    // Obtener un envío por ID
    public Optional<Envio> obtenerPorId(Integer id) {
        return envioRepository.findById(id);
    }

    // Crear nuevo envío
    public Envio crear(Envio envio) {
        return envioRepository.save(envio);
    }

    // Actualizar envío existente
    public Envio actualizar(Integer id, Envio envioActualizado) {
        Optional<Envio> envioExistente = envioRepository.findById(id);

        if (envioExistente.isPresent()) {
            Envio envio = envioExistente.get();

            envio.setCompra(envioActualizado.getCompra());
            envio.setVehiculo(envioActualizado.getVehiculo());
            envio.setTransportista(envioActualizado.getTransportista());
            envio.setEstadoEnvio(envioActualizado.getEstadoEnvio());
            envio.setFechaSalida(envioActualizado.getFechaSalida());
            envio.setFechaEntrega(envioActualizado.getFechaEntrega());
            envio.setNumeroSeguimiento(envioActualizado.getNumeroSeguimiento());

            return envioRepository.save(envio);
        }

        return null;
    }

    // Eliminar un envío
    public boolean eliminar(Integer id) {
        if (envioRepository.existsById(id)) {
            envioRepository.deleteById(id);
            return true;
        }
        return false;
    }
}

