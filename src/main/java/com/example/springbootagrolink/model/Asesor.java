package com.example.springbootagrolink.model;

import jakarta.persistence.*;
import lombok.*;

import java.io.Serializable;

@Entity
@Table(
    name = "tb_asesores",
    uniqueConstraints = {
        @UniqueConstraint(name = "uq_asesores_usuario", columnNames = "id_usuario")
    }
)
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@ToString(exclude = {"usuario", "calificacion"})
public class Asesor implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_asesor")
    private Integer idAsesor;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
        name = "id_usuario",
        nullable = false,
        unique = true,
        foreignKey = @ForeignKey(name = "fk_asesores_usuario")
    )
    private Usuario usuario;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
        name = "id_calificacion",
        foreignKey = @ForeignKey(name = "fk_asesores_calificacion")
    )
    private Calificacion calificacion;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_asesoria", length = 50)
    private TipoAsesoria tipoAsesoria;

    public enum TipoAsesoria {
        Asesor_Agricola,
        Veterinario,
        Maquinista
    }
}

