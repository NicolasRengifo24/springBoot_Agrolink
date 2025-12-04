package com.example.springbootagrolink.model;

import jakarta.persistence.*;
import lombok.*;

import java.io.Serializable;
import java.math.BigDecimal;

@Entity
@Table(name = "tb_vehiculos")
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@ToString(exclude = "transportista")
public class Vehiculo implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_vehiculo")
    private Integer idVehiculo;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
        name = "id_transportista",
        referencedColumnName = "id_usuario",
        nullable = false,
        foreignKey = @ForeignKey(name = "fk_vehiculos_transportista")
    )
    private Transportista transportista;

    @Column(name = "tipo_vehiculo", length = 50)
    private String tipoVehiculo;

    @Column(name = "capacidad_carga", precision = 10, scale = 2)
    private BigDecimal capacidadCarga;

    @Column(name = "documento_propiedad", length = 250)
    private String documentoPropiedad;

    @Column(name = "placa_vehiculo", length = 15)
    private String placaVehiculo;
}
