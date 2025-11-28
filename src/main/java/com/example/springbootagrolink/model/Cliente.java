package com.example.springbootagrolink.model;

import jakarta.persistence.*;
import lombok.*;

import java.io.Serializable;

@Entity
@Table(
    name = "tb_clientes",
    uniqueConstraints = {
        @UniqueConstraint(name = "uq_clientes_usuario", columnNames = "id_usuario")
    }
)
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@ToString(exclude = {"usuario", "calificacion"})
public class Cliente implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_cliente")
    private Integer idCliente;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
        name = "id_usuario",
        nullable = false,
        unique = true,
        foreignKey = @ForeignKey(name = "fk_clientes_usuario")
    )
    private Usuario usuario;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
        name = "id_calificacion",
        foreignKey = @ForeignKey(name = "fk_clientes_calificacion")
    )
    private Calificacion calificacion;

    @Column(name = "preferencias", length = 150)
    private String preferencias = "Sin Preferencias";
}

