package com.example.springbootagrolink.model;

import jakarta.persistence.*;
import lombok.*;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

@Entity
@Table(name = "tb_productos")
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@ToString(exclude = {"productor", "categoria", "calificacion", "productoFincas"})
public class Producto implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_producto")
    private Integer idProducto;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
        name = "id_usuario",
        nullable = false,
        foreignKey = @ForeignKey(name = "fk_productos_productor")
    )
    private Productor productor;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
        name = "id_categoria",
        nullable = false,
        foreignKey = @ForeignKey(name = "fk_productos_categoria")
    )
    private CategoriaProducto categoria;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
        name = "id_calificacion",
        foreignKey = @ForeignKey(name = "fk_productos_calificacion")
    )
    private Calificacion calificacion;

    @Column(name = "precio", precision = 12, scale = 2)
    private BigDecimal precio;

    @Column(name = "nombre_producto", length = 100, nullable = false)
    private String nombreProducto;

    @Column(name = "descripcion_producto", length = 255)
    private String descripcionProducto;

    @Column(name = "stock")
    private Integer stock;

    @Column(name = "peso_kg", precision = 10, scale = 2)
    private BigDecimal pesoKg = BigDecimal.valueOf(1.00);

    // Relación con ProductoFinca: un Producto puede estar asociado a muchas Fincas
    @OneToMany(mappedBy = "producto", cascade = CascadeType.REMOVE, orphanRemoval = true)
    private List<ProductoFinca> productoFincas;
}
