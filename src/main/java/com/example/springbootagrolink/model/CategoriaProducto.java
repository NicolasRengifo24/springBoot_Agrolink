package com.example.springbootagrolink.model;


import jakarta.persistence.*;
import lombok.*;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@ToString
@Table(name = "tb_categorias_productos")
public class CategoriaProducto {

     @Id
     @GeneratedValue(strategy = GenerationType.IDENTITY)
     @Column(name = "id_categoria")
     private Integer idCategoria;

     @Column(name = "nombre_categoria", length = 50, nullable = false)
     private String nombreCategoria;


}
