package com.example.springbootagrolink.model;


import jakarta.persistence.*;

@Entity
@Table(name = "tb_categorias_productos")
public class CategoriaProducto {

     @Id
     @GeneratedValue(strategy = GenerationType.IDENTITY)
     @Column(name = "id_categoria")
     private Integer idCategoria;

     @Column(name = "nombre_categoria", length = 50, nullable = false)
     private String nombreCategoria;

          // Constructores
     public CategoriaProducto() { }

     public CategoriaProducto(String nombreCategoria) {
          this.idCategoria = idCategoria;
          this.nombreCategoria = nombreCategoria;
     }

         // Getters y Setters
     public Integer getIdCategoria() {
          return idCategoria;
     }

     public void setIdCategoria(Integer idCategoria) {
          this.idCategoria = idCategoria;
     }

     public String getNombreCategoria() {
          return nombreCategoria;
     }

     public void setNombreCategoria(String nombreCategoria) {
          this.nombreCategoria = nombreCategoria;
     }
}
