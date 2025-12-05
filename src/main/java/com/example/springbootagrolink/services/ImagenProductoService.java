package com.example.springbootagrolink.services;

import com.example.springbootagrolink.model.ImagenProducto;
import com.example.springbootagrolink.model.Producto;
import com.example.springbootagrolink.repository.ImagenProductoRepository;
import com.example.springbootagrolink.repository.ProductoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class ImagenProductoService {

    @Autowired
    private ImagenProductoRepository imagenProductoRepository;

    @Autowired
    private ProductoRepository productoRepository;

    @Transactional(readOnly = true)
    public List<ImagenProducto> obtenerTodos() {
        return imagenProductoRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Optional<ImagenProducto> obtenerPorId(Integer id) {
        return imagenProductoRepository.findById(id);
    }

    @Transactional
    public ImagenProducto guardar(ImagenProducto imagen, Integer productoId) {
        // Validar producto
        Producto producto = productoRepository.findById(productoId)
                .orElseThrow(() -> new IllegalArgumentException("Producto no encontrado con ID: " + productoId));
        imagen.setProducto(producto);
        return imagenProductoRepository.save(imagen);
    }

    @Transactional
    public ImagenProducto actualizar(Integer id, ImagenProducto cambios, Integer productoId) {
        ImagenProducto existente = imagenProductoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Imagen no encontrada con ID: " + id));
        // Si se cambia el producto, validar y asignar
        if (productoId != null) {
            Producto producto = productoRepository.findById(productoId)
                    .orElseThrow(() -> new IllegalArgumentException("Producto no encontrado con ID: " + productoId));
            existente.setProducto(producto);
        }
        if (cambios.getUrlImagen() != null && !cambios.getUrlImagen().isBlank()) {
            existente.setUrlImagen(cambios.getUrlImagen());
        }
        // Actualiza el flag de imagen principal si se proporciona
        if (cambios.getEsPrincipal() != null) {
            existente.setEsPrincipal(cambios.getEsPrincipal());
        }
        return imagenProductoRepository.save(existente);
    }

    @Transactional
    public boolean eliminar(Integer id) {
        if (imagenProductoRepository.existsById(id)) {
            imagenProductoRepository.deleteById(id);
            return true;
        }
        return false;
    }

    @Transactional(readOnly = true)
    public List<ImagenProducto> listarPorProducto(Integer productoId) {
        return imagenProductoRepository.findByProducto_IdProducto(productoId);
    }
}
