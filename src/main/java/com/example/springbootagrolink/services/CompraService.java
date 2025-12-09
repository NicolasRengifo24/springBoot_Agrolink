package com.example.springbootagrolink.services;

import com.example.springbootagrolink.model.Compra;
import com.example.springbootagrolink.model.DetalleCompra;
import com.example.springbootagrolink.model.Producto;
import com.example.springbootagrolink.model.Cliente;
import com.example.springbootagrolink.repository.CompraRepository;
import com.example.springbootagrolink.repository.DetalleCompraRepository;
import com.example.springbootagrolink.repository.ProductoRepository;
import com.example.springbootagrolink.repository.ClienteRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class CompraService {

    private final CompraRepository compraRepository;
    private final DetalleCompraRepository detalleCompraRepository;
    private final ProductoRepository productoRepository;
    private final ClienteRepository clienteRepository;

    public CompraService(CompraRepository compraRepository,
                         DetalleCompraRepository detalleCompraRepository,
                         ProductoRepository productoRepository,
                         ClienteRepository clienteRepository) {
        this.compraRepository = compraRepository;
        this.detalleCompraRepository = detalleCompraRepository;
        this.productoRepository = productoRepository;
        this.clienteRepository = clienteRepository;
    }

    @Transactional
    public Compra crearCompra(Integer idCliente, String metodoPago, String direccionEntrega) {
        Compra compra = new Compra();
        // Asociar cliente por id
        if (idCliente != null) {
            Optional<Cliente> clienteOpt = clienteRepository.findById(idCliente);
            clienteOpt.ifPresent(compra::setCliente);
        }
        compra.setFechaHoraCompra(LocalDateTime.now());
        compra.setSubtotal(BigDecimal.ZERO);
        compra.setImpuestos(BigDecimal.ZERO);
        compra.setValorEnvio(BigDecimal.ZERO);
        compra.setTotal(BigDecimal.ZERO);
        compra.setMetodoPago(metodoPago);
        compra.setDireccionEntrega(direccionEntrega);
        return compraRepository.save(compra);
    }

    @Transactional
    public DetalleCompra agregarProducto(Integer idCompra, Integer idProducto, Integer cantidad) {
        Compra compra = compraRepository.findById(idCompra)
                .orElseThrow(() -> new IllegalArgumentException("Compra no encontrada"));
        Producto producto = productoRepository.findById(idProducto)
                .orElseThrow(() -> new IllegalArgumentException("Producto no encontrado"));

        if (producto.getStock() == null || producto.getStock() < cantidad) {
            throw new IllegalStateException("Stock insuficiente");
        }

        BigDecimal precioUnitario = producto.getPrecio();
        BigDecimal subtotalDetalle = precioUnitario.multiply(BigDecimal.valueOf(cantidad));

        DetalleCompra detalle = new DetalleCompra();
        detalle.setCompra(compra);
        detalle.setProducto(producto);
        detalle.setCantidad(cantidad);
        detalle.setPrecioUnitario(precioUnitario);
        detalle.setSubtotal(subtotalDetalle);
        detalleCompraRepository.save(detalle);

        // Actualizar stock
        producto.setStock(producto.getStock() - cantidad);
        productoRepository.save(producto);

        // Obtener todos los detalles de la compra y recalcular peso total y totales
        List<DetalleCompra> detalles = detalleCompraRepository.findByCompra_IdCompra(idCompra);

        BigDecimal pesoTotal = detalles.stream()
                .map(d -> BigDecimal.valueOf(d.getCantidad()).multiply(d.getProducto().getPesoKg()))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal valorEnvio = pesoTotal.multiply(BigDecimal.valueOf(800));

        BigDecimal nuevoSubtotal = detalles.stream()
                .map(DetalleCompra::getSubtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal impuestos = nuevoSubtotal.multiply(BigDecimal.valueOf(0.08));
        BigDecimal total = nuevoSubtotal.add(impuestos).add(valorEnvio);

        compra.setSubtotal(nuevoSubtotal);
        compra.setImpuestos(impuestos);
        compra.setValorEnvio(valorEnvio);
        compra.setTotal(total);
        compraRepository.save(compra);

        return detalle;
    }

    @Transactional
    public void cancelarCompra(Integer idCompra) {
        Optional<Compra> compraOpt = compraRepository.findById(idCompra);
        if (compraOpt.isEmpty()) return;
        // Restaurar stock basado en detalles de la compra y eliminar
        List<DetalleCompra> detalles = detalleCompraRepository.findByCompra_IdCompra(idCompra);
        detalles.forEach(d -> {
            Producto p = d.getProducto();
            p.setStock(p.getStock() + d.getCantidad());
            productoRepository.save(p);
            detalleCompraRepository.delete(d);
        });
        compraRepository.deleteById(idCompra);
    }

    public Page<Compra> buscarCompras(Specification<Compra> spec, Pageable pageable) {
        return compraRepository.findAll(spec, pageable);
    }

    public List<Compra> obtenerTodas() {
        return compraRepository.findAll();
    }

    public Optional<Compra> obtenerPorId(Integer id) {
        return compraRepository.findById(id);
    }
}
