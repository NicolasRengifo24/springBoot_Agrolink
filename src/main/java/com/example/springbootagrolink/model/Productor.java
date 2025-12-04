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

    // La PK de tb_productores es id_usuario
    @Id
    @Column(name = "id_usuario")
    private Integer idProductor;

    // Relación 1:1 con Usuario usando la misma PK (FK-PK) mediante @MapsId
    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @MapsId
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
        Cultivo_Urbano // Nota: en BD el valor tiene espacio; podemos mapear con converter si es necesario
    }

    // Relación con Finca: un Productor puede tener muchas Fincas
    @OneToMany(mappedBy = "productor", cascade = CascadeType.REMOVE, orphanRemoval = true)
    private List<Finca> fincas;
}
