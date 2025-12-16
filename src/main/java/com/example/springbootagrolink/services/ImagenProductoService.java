package com.example.springbootagrolink.services;

import com.example.springbootagrolink.model.ImagenProducto;
import com.example.springbootagrolink.model.Producto;
import com.example.springbootagrolink.repository.ImagenProductoRepository;
import com.example.springbootagrolink.repository.ProductoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

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
    public ImagenProducto guardar(ImagenProducto imagen, Integer productoId, MultipartFile archivo) {
        try {
            Producto producto = productoRepository.findById(productoId)
                    .orElseThrow(() -> new IllegalArgumentException("Producto no encontrado con ID: " + productoId));

            // Paths para guardar: desarrollo y producción (same as ProductoController)
            String devPath = "src/main/resources/static/images/products/";
            String prodPath = "target/classes/static/images/products/";

            Path devUploadPath = Paths.get(devPath);
            Path prodUploadPath = Paths.get(prodPath);

            if (!Files.exists(devUploadPath)) {
                Files.createDirectories(devUploadPath);
            }
            if (!Files.exists(prodUploadPath)) {
                Files.createDirectories(prodUploadPath);
            }

            String originalFilename = archivo.getOriginalFilename();
            String extension = (originalFilename != null && originalFilename.contains("."))
                    ? originalFilename.substring(originalFilename.lastIndexOf('.'))
                    : ".jpg";
            String nuevoNombre = UUID.randomUUID() + extension;

            byte[] bytes = archivo.getBytes();

            try {
                Path devFilePath = devUploadPath.resolve(nuevoNombre);
                Files.write(devFilePath, bytes);
            } catch (IOException e) {
                // ignore dev save failure
            }

            Path prodFilePath = prodUploadPath.resolve(nuevoNombre);
            Files.write(prodFilePath, bytes);

            // Guardar entidad
            imagen.setProducto(producto);
            String urlRelativa = "images/products/" + nuevoNombre;
            imagen.setUrlImagen(urlRelativa.startsWith("/") ? urlRelativa : "/" + urlRelativa);
            if (imagen.getEsPrincipal() == null) imagen.setEsPrincipal(false);

            return imagenProductoRepository.save(imagen);
        } catch (IOException e) {
            throw new RuntimeException("Error al guardar el archivo de imagen: " + e.getMessage(), e);
        }
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
