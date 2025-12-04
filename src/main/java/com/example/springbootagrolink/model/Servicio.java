package com.example.springbootagrolink.model;

import jakarta.persistence.*;
import lombok.*;

import java.io.Serializable;

@Entity
@Table(name = "tb_servicios")
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@ToString(exclude = "asesor")
public class Servicio implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_servicio")
    private Integer idServicio;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
        name = "id_asesor",
        referencedColumnName = "id_usuario",
        nullable = false,
        foreignKey = @ForeignKey(name = "fk_servicios_asesor")
    )
    private Asesor asesor;

    @Column(name = "descripcion", length = 250)
    private String descripcion;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado", length = 20)
    private EstadoServicio estado;

    public enum EstadoServicio {
        Activo,
        Inactivo
    }
}

