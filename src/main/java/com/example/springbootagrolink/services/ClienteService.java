package com.example.springbootagrolink.services;

import com.example.springbootagrolink.model.Cliente;
import com.example.springbootagrolink.repository.ClienteRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ClienteService {

    @Autowired
    private ClienteRepository clienteRepository;

    // Obtener todos los clientes
    public List<Cliente> obtenerTodosLosClientes() {
        return clienteRepository.findAll();
    }

    // Obtener cliente por ID
    public Optional<Cliente> obtenerClientePorId(Integer id) {
        return clienteRepository.findById(id);
    }

    // Crear cliente nuevo
    public Cliente crearCliente(Cliente cliente) {
        return clienteRepository.save(cliente);
    }

    // Actualizar cliente
    public Cliente actualizarCliente(Integer id, Cliente clienteActualizado) {

        return clienteRepository.findById(id).map(cliente -> {
            cliente.setPreferencias(clienteActualizado.getPreferencias());
            cliente.setCalificacion(clienteActualizado.getCalificacion());
            cliente.setUsuario(clienteActualizado.getUsuario());
            return clienteRepository.save(cliente);
        }).orElse(null);
    }

    // Eliminar cliente
    public boolean eliminarCliente(Integer id) {
        if (clienteRepository.existsById(id)) {
            clienteRepository.deleteById(id);
            return true;
        }
        return false;
    }
}

