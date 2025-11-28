package com.example.springbootagrolink.model;

import jakarta.persistence.*;
import lombok.*;

import java.io.Serializable;
import java.math.BigDecimal;


@Entity
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@ToString
@Table(name = "tb_calificacion")
public class Calificacion implements Serializable {


     @Id
     @GeneratedValue(strategy = GenerationType.IDENTITY)
     @Column(name = "id_calificacion")
     private Integer idCalificacion;

     @Column(name = "puntaje", precision = 5, scale = 2)
     private BigDecimal puntaje;

     @Column(name = "promedio", precision = 5, scale = 2)
     private BigDecimal promedio;




}
