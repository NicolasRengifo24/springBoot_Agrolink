package com.example.springbootagrolink.model;

import jakarta.persistence.*;
import lombok.*;

import java.io.Serializable;

@Entity
@Table(
    name = "tb_transportistas",
    uniqueConstraints = {
        @UniqueConstraint(name = "uq_transportistas_usuario", columnNames = "id_usuario")
    }
)
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@ToString(exclude = {"usuario", "calificacion"})
public class Transportista implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_transportista")
    private Integer idTransportista;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
        name = "id_usuario",
        nullable = false,
        unique = true,
        foreignKey = @ForeignKey(name = "fk_transportistas_usuario")
    )
    private Usuario usuario;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
        name = "id_calificacion",
        foreignKey = @ForeignKey(name = "fk_transportistas_calificacion")
    )
    private Calificacion calificacion;

    @Column(name = "zonas_entrega", length = 250)
    private String zonasEntrega;
}

