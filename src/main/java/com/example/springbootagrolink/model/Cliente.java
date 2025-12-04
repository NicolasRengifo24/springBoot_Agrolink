package com.example.springbootagrolink.model;

import jakarta.persistence.*;
import lombok.*;

import java.io.Serializable;

@Entity
@Table(name = "tb_clientes")
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@ToString(exclude = {"usuario", "calificacion"})
public class Cliente implements Serializable {

    @Id
    @Column(name = "id_usuario")
    private Integer idUsuario;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @MapsId
    @JoinColumn(name = "id_usuario", foreignKey = @ForeignKey(name = "fk_clientes_usuarios"))
    private Usuario usuario;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_calificacion", foreignKey = @ForeignKey(name = "fk_clientes_calificacion"))
    private Calificacion calificacion;

    @Column(name = "preferencias", length = 150)
    private String preferencias = "Sin Preferencias";
}
