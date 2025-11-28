package com.example.springbootagrolink.model;

import jakarta.persistence.*;
import lombok.*;

import java.io.Serializable;
import java.time.LocalDate;

@Entity
@Table(name = "tb_envios")
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@ToString(exclude = {"compra", "vehiculo", "transportista"})
public class Envio implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_envio")
    private Integer idEnvio;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
        name = "id_compra",
        nullable = false,
        foreignKey = @ForeignKey(name = "fk_envios_compra")
    )
    private Compra compra;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
        name = "id_vehiculo",
        foreignKey = @ForeignKey(name = "fk_envios_vehiculo")
    )
    private Vehiculo vehiculo;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
        name = "id_transportista",
        foreignKey = @ForeignKey(name = "fk_envios_transportista")
    )
    private Transportista transportista;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado_envio", length = 50)
    private EstadoEnvio estadoEnvio = EstadoEnvio.Buscando_Transporte;

    @Column(name = "fecha_salida")
    private LocalDate fechaSalida;

    @Column(name = "fecha_entrega")
    private LocalDate fechaEntrega;

    @Column(name = "numero_seguimiento", length = 50)
    private String numeroSeguimiento;

    public enum EstadoEnvio {
        Buscando_Transporte,
        Asignado,
        Finalizado
    }
}

