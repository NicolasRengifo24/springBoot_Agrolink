package com.example.springbootagrolink.services;


import jakarta.servlet.http.HttpSession;

import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;

import com.example.springbootagrolink.repository.ProductoRepository;
import com.example.springbootagrolink.model.Producto;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Service
public class CarritoService {

    @Autowired
    private ProductoRepository productoRepository;

    @SuppressWarnings("unchecked")
    public Map<Integer, Integer> obtenerCarrito(HttpSession session) {
        Map<Integer, Integer> carrito =
                (Map<Integer, Integer>) session.getAttribute("carrito");

        if (carrito == null) {
            carrito = new HashMap<>();
            session.setAttribute("carrito", carrito);
        }
        return carrito;
    }

    // Métodos nuevos/compatibilidad con controladores existentes

    /**
     * Compatibilidad: obtener(session) -> obtiene el mapa idProducto->cantidad
     */
    public Map<Integer, Integer> obtener(HttpSession session) {
        return obtenerCarrito(session);
    }

    /**
     * Agrega 1 unidad del producto al carrito (crea la entrada si no existe)
     */
    public void agregar(HttpSession session, Integer idProducto) {
        Map<Integer, Integer> carrito = obtenerCarrito(session);
        carrito.merge(idProducto, 1, Integer::sum);
        session.setAttribute("carrito", carrito);
    }

    /**
     * Elimina el producto del carrito por completo
     */
    public void eliminar(HttpSession session, Integer idProducto) {
        Map<Integer, Integer> carrito = obtenerCarrito(session);
        carrito.remove(idProducto);
        session.setAttribute("carrito", carrito);
    }

    /**
     * Vacía el carrito
     */
    public void vaciar(HttpSession session) {
        limpiar(session);
    }

    public void limpiar(HttpSession session) {
        session.removeAttribute("carrito");
    }

    public boolean estaVacio(HttpSession session) {
        return obtenerCarrito(session).isEmpty();
    }

    public int contarItems(HttpSession session) {
        return obtenerCarrito(session).values()
                .stream()
                .mapToInt(Integer::intValue)
                .sum();
    }

    /**
     * Compatibilidad: totalItems(session)
     */
    public int totalItems(HttpSession session) {
        return contarItems(session);
    }

    /**
     * Calcula el total monetario del carrito consultando los precios en BD
     */
    public BigDecimal calcularTotal(HttpSession session) {
        Map<Integer, Integer> carrito = obtenerCarrito(session);
        BigDecimal total = BigDecimal.ZERO;

        if (carrito.isEmpty()) return total;

        for (Map.Entry<Integer, Integer> entry : carrito.entrySet()) {
            Integer idProducto = entry.getKey();
            Integer cantidad = entry.getValue();

            Optional<Producto> opt = productoRepository.findById(idProducto);
            if (opt.isPresent()) {
                Producto p = opt.get();
                if (p.getPrecio() != null) {
                    total = total.add(p.getPrecio().multiply(BigDecimal.valueOf(cantidad)));
                }
            }
        }
        return total;
    }
}
