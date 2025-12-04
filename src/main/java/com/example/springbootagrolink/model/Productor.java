package com.example.springbootagrolink.model;

import jakarta.persistence.*;
import lombok.*;

import java.io.Serializable;
import java.util.List;

@Entity
@Table(
    name = "tb_productores",
    uniqueConstraints = {
        @UniqueConstraint(
            name = "uq_productores_usuario",
            columnNames = "id_usuario"
        )
    }
)
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@ToString(exclude = {"usuario", "calificacion", "fincas"})
public class Productor implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_productor")
    private Integer idProductor;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
        name = "id_usuario",
        nullable = false,
        unique = true,
        foreignKey = @ForeignKey(name = "fk_productores_usuario")
    )
    private Usuario usuario;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
        name = "id_calificacion",
        foreignKey = @ForeignKey(name = "fk_productores_calificacion")
    )
    private Calificacion calificacion;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_cultivo", length = 50)
    private TipoCultivo tipoCultivo;

    public enum TipoCultivo {
        Monocultivo,
        Policultivo,
        Huerta,
        Cultivo_Urbano
    }

    // Relación con Finca: un Productor puede tener muchas Fincas
    @OneToMany(mappedBy = "productor", cascade = CascadeType.REMOVE, orphanRemoval = true)
    private List<Finca> fincas;
}
