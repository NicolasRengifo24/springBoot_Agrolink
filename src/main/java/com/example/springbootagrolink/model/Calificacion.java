package com.example.springbootagrolink.model;

import jakarta.persistence.*;

import java.io.Serializable;


@Entity
@Table(name = "tb_calificacion")
public class Calificacion implements Serializable {


     @Id
     @GeneratedValue(strategy = GenerationType.IDENTITY)
     @Column(name = "id_calificacion")
     private Integer idCalificacion;

     @Column(name = "puntaje", precision = 5, scale = 2)
     private Double puntaje;

     @Column(name = "promedio", precision = 5, scale = 2)
     private Double promedio;



     // Constructor
     public Calificacion(Integer idCalificacion, Double puntaje, Double promedio) {
          this.idCalificacion = idCalificacion;
          this.puntaje = puntaje;
          this.promedio = promedio;
     }

     public Calificacion() {

     }


     public Integer getIdCalificacion() {
          return idCalificacion;
     }

     public void setIdCalificacion(Integer idCalificacion) {
          this.idCalificacion = idCalificacion;
     }

     public Double getPuntaje() {
          return puntaje;
     }

     public void setPuntaje(Double puntaje) {
          this.puntaje = puntaje;
     }

     public Double getPromedio() {
          return promedio;
     }

     public void setPromedio(Double promedio) {
          this.promedio = promedio;
     }
}

