package com.example.springbootagrolink.services;

import com.example.springbootagrolink.model.Producto;
import com.example.springbootagrolink.repository.ProductoRepository;
import com.example.springbootagrolink.services.Idao.Idao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ProductoService  implements Idao <Producto, Integer> {

    @Autowired
    private ProductoRepository productoRepository;

    @Override
    public List<Producto> obtenerTodos() {
        return productoRepository.findAll();
    }

    @Override
    public Optional<Producto> obtenerPorId(Integer id) {
        return productoRepository.findById(id);
    }


    @Override
    public Producto guardar(Producto producto) {
        return productoRepository.save(producto);
    }

    @Override
    public Producto actualizar(Integer id, Producto producto) {
        Optional<Producto> productoExistente = productoRepository.findById(id);
        if(productoExistente.isPresent()){
        Producto prodActual = productoExistente.get();
            prodActual.setNombreProducto(producto.getNombreProducto());
            prodActual.setDescripcionProducto(producto.getDescripcionProducto());
            prodActual.setPrecio(producto.getPrecio());
            prodActual.setStock(producto.getStock());
            prodActual.setCategoria(producto.getCategoria());
            return productoRepository.save(prodActual);
        } else {
            return null;
        }
    }

    @Override
    public boolean eliminar(Integer id) {
        if(productoRepository.existsById(id)){
            productoRepository.deleteById(id);
            return true;
        } else {
            return false;
        }
    }








}
