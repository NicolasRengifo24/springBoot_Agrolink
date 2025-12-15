
package com.example.springbootagrolink.model;

import java.math.BigDecimal;
import java.util.Map;

/**
 * Pequeña clase/record para devolver resumen de compra (subtotal, impuestos, envio, total, items)
 */
public record CompraResumen(
        Map<Integer, Integer> items,
        BigDecimal subtotal,
        BigDecimal impuestos,
        BigDecimal envio,
        BigDecimal total
) {
}

