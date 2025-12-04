package com.example.springbootagrolink.services;

import com.example.springbootagrolink.model.DetalleCompra;
import com.example.springbootagrolink.repository.DetalleCompraRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class DetalleCompraService {
    private final DetalleCompraRepository detalleCompraRepository;

    public DetalleCompraService(DetalleCompraRepository detalleCompraRepository) {
        this.detalleCompraRepository = detalleCompraRepository;
    }

    public List<DetalleCompra> listarPorCompra(Integer idCompra) {
        return detalleCompraRepository.findByCompra_IdCompra(idCompra);
    }
}
