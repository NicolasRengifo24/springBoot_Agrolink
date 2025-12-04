package com.example.springbootagrolink.model;

import jakarta.persistence.*;
import lombok.*;

import java.io.Serializable;

@Entity
@Table(name = "tb_asesores")
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@ToString(exclude = {"usuario", "calificacion"})
public class Asesor implements Serializable {

    @Id
    @Column(name = "id_usuario")
    private Integer idUsuario;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @MapsId
    @JoinColumn(name = "id_usuario", foreignKey = @ForeignKey(name = "fk_asesores_usuarios"))
    private Usuario usuario;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_calificacion", foreignKey = @ForeignKey(name = "fk_asesores_calificacion"))
    private Calificacion calificacion;

    @Column(name = "tipo_asesoria", length = 50)
    private String tipoAsesoria;
}
