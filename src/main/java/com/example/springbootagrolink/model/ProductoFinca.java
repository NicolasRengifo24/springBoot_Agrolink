package com.example.springbootagrolink.model;

import jakarta.persistence.*;
import lombok.*;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "tb_productos_fincas")
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@ToString(exclude = {"finca", "producto"})
public class ProductoFinca implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_producto_finca")
    private Integer idProductoFinca;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
        name = "id_finca",
        nullable = false,
        foreignKey = @ForeignKey(name = "fk_productos_fincas_finca")
    )
    private Finca finca;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
        name = "id_producto",
        nullable = false,
        foreignKey = @ForeignKey(name = "fk_productos_fincas_producto")
    )
    private Producto producto;

    @Column(name = "cantidad_produccion", precision = 10, scale = 2)
    private BigDecimal cantidadProduccion;

    @Column(name = "fecha_cosecha")
    private LocalDate fechaCosecha;
}

