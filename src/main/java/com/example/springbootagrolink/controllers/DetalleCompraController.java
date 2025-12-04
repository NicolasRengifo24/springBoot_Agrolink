package com.example.springbootagrolink.controllers;

import com.example.springbootagrolink.model.DetalleCompra;
import com.example.springbootagrolink.services.DetalleCompraService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/detalles-compra")
public class DetalleCompraController {

    private final DetalleCompraService detalleCompraService;

    public DetalleCompraController(DetalleCompraService detalleCompraService) {
        this.detalleCompraService = detalleCompraService;
    }

    @GetMapping
    public ResponseEntity<List<DetalleCompra>> listarPorCompra(@RequestParam Integer idCompra) {
        return ResponseEntity.ok(detalleCompraService.listarPorCompra(idCompra));
    }
}
