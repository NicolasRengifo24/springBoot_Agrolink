package com.example.springbootagrolink.model;

import jakarta.persistence.*;
import lombok.*;

import java.io.Serializable;

@Entity
@Table(name = "tb_imagenes_productos")
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@ToString(exclude = "producto")
public class ImagenProducto implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_imagen")
    private Integer idImagen;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
        name = "id_producto",
        nullable = false,
        foreignKey = @ForeignKey(name = "fk_imagenes_producto")
    )
    private Producto producto;

    @Column(name = "url_imagen", length = 255, nullable = false)
    private String urlImagen;

    @Column(name = "es_principal")
    private Boolean esPrincipal = false;
}

