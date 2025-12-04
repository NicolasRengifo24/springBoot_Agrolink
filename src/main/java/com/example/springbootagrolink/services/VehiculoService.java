package com.example.springbootagrolink.services;

import com.example.springbootagrolink.model.Vehiculo;
import com.example.springbootagrolink.repository.VehiculoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class VehiculoService {

    @Autowired
    private VehiculoRepository vehiculoRepository;

    // Obtener todos los vehículos
    public List<Vehiculo> obtenerTodos() {
        return vehiculoRepository.findAll();
    }

    // Obtener un vehículo por ID
    public Optional<Vehiculo> obtenerPorId(Integer id) {
        return vehiculoRepository.findById(id);
    }

    // Crear un vehículo
    public Vehiculo crear(Vehiculo vehiculo) {
        return vehiculoRepository.save(vehiculo);
    }

    // Actualizar un vehículo
    public Vehiculo actualizar(Integer id, Vehiculo vehiculoActualizado) {
        Optional<Vehiculo> vehiculoExistente = vehiculoRepository.findById(id);

        if (vehiculoExistente.isPresent()) {
            Vehiculo vehiculo = vehiculoExistente.get();

            vehiculo.setTransportista(vehiculoActualizado.getTransportista());
            vehiculo.setTipoVehiculo(vehiculoActualizado.getTipoVehiculo());
            vehiculo.setCapacidadCarga(vehiculoActualizado.getCapacidadCarga());
            vehiculo.setDocumentoPropiedad(vehiculoActualizado.getDocumentoPropiedad());
            vehiculo.setPlacaVehiculo(vehiculoActualizado.getPlacaVehiculo());

            return vehiculoRepository.save(vehiculo);
        }

        return null;
    }

    // Eliminar vehículo
    public boolean eliminar(Integer id) {
        if (vehiculoRepository.existsById(id)) {
            vehiculoRepository.deleteById(id);
            return true;
        }
        return false;
    }
}

