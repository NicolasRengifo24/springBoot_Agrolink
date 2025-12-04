package com.example.springbootagrolink.services;

import com.example.springbootagrolink.model.ImagenProducto;
import com.example.springbootagrolink.model.Producto;
import com.example.springbootagrolink.repository.ImagenProductoRepository;
import com.example.springbootagrolink.repository.ProductoRepository;
import com.example.springbootagrolink.services.Idao.Idao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class ImagenProductoService implements Idao<ImagenProducto, Integer> {

    @Autowired
    private ImagenProductoRepository imagenProductoRepository;

    @Autowired
    private ProductoRepository productoRepository;


    // LISTAR TODAS LAS IMÁGENES
    @Override
    @Transactional(readOnly = true)
    public List<ImagenProducto> obtenerTodos() {
        return imagenProductoRepository.findAll();
    }


    // BUSCAR POR ID
    @Override
    @Transactional(readOnly = true)
    public Optional<ImagenProducto> obtenerPorId(Integer id) {
        return imagenProductoRepository.findById(id);
    }


    // GUARDAR UNA IMAGEN (requiere producto)
    @Transactional
    public ImagenProducto guardar(ImagenProducto imagen, Integer productoId) {
        Producto producto = productoRepository.findById(productoId)
                .orElseThrow(() -> new RuntimeException("Producto no encontrado con ID: " + productoId));

        imagen.setProducto(producto);
        return imagenProductoRepository.save(imagen);
    }


    // ACTUALIZAR
    @Transactional
    public ImagenProducto actualizar(Integer id, ImagenProducto cambios, Integer nuevoProductoId) {

        ImagenProducto existente = imagenProductoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Imagen no encontrada con ID: " + id));

        // Actualizar campos simples
        existente.setUrlImagen(cambios.getUrlImagen());
        existente.setDescripcion(cambios.getDescripcion());

        // Si cambia el producto asociado
        if (nuevoProductoId != null) {
            Producto nuevoProducto = productoRepository.findById(nuevoProductoId)
                    .orElseThrow(() -> new RuntimeException("Producto no encontrado con ID: " + nuevoProductoId));

            existente.setProducto(nuevoProducto);
        }

        return imagenProductoRepository.save(existente);
    }


    // ELIMINAR
    @Override
    @Transactional
    public boolean eliminar(Integer id) {
        if (imagenProductoRepository.existsById(id)) {
            imagenProductoRepository.deleteById(id);
            return true;
        }
        return false;
    }

    // LISTAR IMÁGENES POR PRODUCTO
    @Transactional(readOnly = true)
    public List<ImagenProducto> listarPorProducto(Integer productoId) {
        return imagenProductoRepository.findByProductoIdProducto(productoId);
    }
}
