package com.example.springbootagrolink.services;

import com.example.springbootagrolink.model.Producto;
import com.example.springbootagrolink.repository.ProductoRepository;
import com.example.springbootagrolink.services.Idao.Idao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class ProductoService  implements Idao <Producto, Integer> {

    @Autowired
    private ProductoRepository productoRepository;


    @Override
    @Transactional(readOnly = true)
    public List<Producto> obtenerTodos() {
        return productoRepository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Producto> obtenerPorId(Integer id) {
        return productoRepository.findById(id);
    }


    @Override
    @Transactional
    public Producto guardar(Producto producto) {
        return productoRepository.save(producto);
    }

    @Override
    @Transactional
    public Producto actualizar(Integer id, Producto producto) {
        Optional<Producto> productoExistente = productoRepository.findById(id);
        if(productoExistente.isPresent()){
            Producto prodActual = productoExistente.get();
            // Actualización parcial: sólo aplicar cambios no nulos para no pisar relaciones/campos obligatorios
            if (producto.getNombreProducto() != null) {
                prodActual.setNombreProducto(producto.getNombreProducto());
            }
            if (producto.getDescripcionProducto() != null) {
                prodActual.setDescripcionProducto(producto.getDescripcionProducto());
            }
            if (producto.getPrecio() != null) {
                prodActual.setPrecio(producto.getPrecio());
            }
            if (producto.getStock() != null) {
                prodActual.setStock(producto.getStock());
            }
            if (producto.getCategoria() != null) {
                prodActual.setCategoria(producto.getCategoria());
            }
            return productoRepository.save(prodActual);
        } else {
            throw new RuntimeException("Producto no encontrado con ID: " + id);
        }
    }

    @Override
    @Transactional
    public boolean eliminar(Integer id) {
        if(productoRepository.existsById(id)){
            productoRepository.deleteById(id);
            return true;
        } else {
            return false;
        }
    }

    @Transactional(readOnly = true)
    public List<Producto> buscarPorUbicacionYCategoria(String ubicacion, Integer categoriaId) {
        return productoRepository.buscarPorUbicacionYCategoria(ubicacion, categoriaId);
    }

    // Método para obtener productos por categoría
    @Transactional(readOnly = true)
    public List<Producto> obtenerPorCategoria(Integer categoriaId) {
        return productoRepository.findByCategoriaIdCategoria(categoriaId);
    }

    // Método para búsqueda avanzada en múltiples campos
    @Transactional(readOnly = true)
    public List<Producto> busquedaAvanzada(String termino) {
        return productoRepository.busquedaAvanzada(termino);
    }

    // Método para filtrar productos por ubicación
    @Transactional(readOnly = true)
    public List<Producto> filtrarPorUbicacion(String ubicacion) {
        return productoRepository.filtrarPorUbicacion(ubicacion);
    }

    // Método para filtrar productos por rango de precio
    @Transactional(readOnly = true)
    public List<Producto> filtrarPorRangoPrecio(Double precioMin, Double precioMax) {
        return productoRepository.filtrarPorRangoPrecio(precioMin, precioMax);
    }

    // Método para filtrar por múltiples criterios (actualizado)
    @Transactional(readOnly = true)
    public List<Producto> filtrarPorMultiplesCriterios(Integer categoriaId, String ubicacion, Double precioMin, Double precioMax, Double calificacionMin) {
        return productoRepository.filtrarPorMultiplesCriterios(categoriaId, ubicacion, precioMin, precioMax, calificacionMin);
    }

    // Método para obtener ubicaciones disponibles
    @Transactional(readOnly = true)
    public List<String> obtenerUbicacionesDisponibles() {
        return productoRepository.obtenerUbicacionesDisponibles();
    }

    // Método para obtener precio mínimo
    @Transactional(readOnly = true)
    public Double obtenerPrecioMinimo() {
        return productoRepository.obtenerPrecioMinimo();
    }

    // Método para obtener precio máximo
    @Transactional(readOnly = true)
    public Double obtenerPrecioMaximo() {
        return productoRepository.obtenerPrecioMaximo();
    }

    // Método para filtrar por calificación mínima
    @Transactional(readOnly = true)
    public List<Producto> filtrarPorCalificacion(Double calificacionMin) {
        return productoRepository.filtrarPorCalificacion(calificacionMin);
    }

    // Método para obtener el producto más vendido (destacado)
    @Transactional(readOnly = true)
    public Producto obtenerProductoMasVendido() {
        List<Producto> productos = productoRepository.obtenerProductoMasVendido();
        return productos.isEmpty() ? productoRepository.findFirstByOrderByIdProductoDesc() : productos.get(0);
    }

}
