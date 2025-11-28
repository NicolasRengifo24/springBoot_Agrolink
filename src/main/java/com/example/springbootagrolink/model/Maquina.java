package com.example.springbootagrolink.model;

import jakarta.persistence.*;
import lombok.*;

import java.io.Serializable;

@Entity
@Table(name = "tb_maquinas")
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@ToString(exclude = "asesor")
public class Maquina implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_maquina")
    private Integer idMaquina;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
        name = "id_asesor",
        nullable = false,
        foreignKey = @ForeignKey(name = "fk_maquinas_asesor")
    )
    private Asesor asesor;

    @Column(name = "tipo_maquina", length = 50, nullable = false)
    private String tipoMaquina;

    @Column(name = "documento_propiedad", length = 50, nullable = false)
    private String documentoPropiedad;

    @Column(name = "modelo", length = 50)
    private String modelo;

    @Column(name = "registro_RNMA", length = 200)
    private String registroRNMA = "Sin Certificado";

    @Column(name = "tarjeta_registro_maquinaria", length = 300)
    private String tarjetaRegistroMaquinaria = "Sin Tarjeta";
}

