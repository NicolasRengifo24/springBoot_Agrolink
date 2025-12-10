package com.example.springbootagrolink.model;

import jakarta.persistence.*;
import lombok.*;

import java.io.Serializable;
import java.math.BigDecimal;
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

    // ========== CAMPOS PARA CÁLCULO DE COSTOS ==========

    @Column(name = "direccion_origen", length = 300)
    private String direccionOrigen;

    @Column(name = "direccion_destino", length = 300)
    private String direccionDestino;

    @Column(name = "latitud_origen")
    private Double latitudOrigen;

    @Column(name = "longitud_origen")
    private Double longitudOrigen;

    @Column(name = "latitud_destino")
    private Double latitudDestino;

    @Column(name = "longitud_destino")
    private Double longitudDestino;

    @Column(name = "distancia_km")
    private Double distanciaKm;

    @Column(name = "peso_total_kg")
    private Double pesoTotalKg;

    @Column(name = "costo_base", precision = 10, scale = 2)
    private BigDecimal costoBase = BigDecimal.ZERO;

    @Column(name = "costo_peso", precision = 10, scale = 2)
    private BigDecimal costoPeso = BigDecimal.ZERO;

    @Column(name = "costo_total", precision = 10, scale = 2)
    private BigDecimal costoTotal = BigDecimal.ZERO;

    @Column(name = "tarifa_por_km", precision = 10, scale = 2)
    private BigDecimal tarifaPorKm = new BigDecimal("2500"); // $2,500 COP por km por defecto

    @Column(name = "tarifa_por_kg", precision = 10, scale = 2)
    private BigDecimal tarifaPorKg = new BigDecimal("50"); // $50 COP por kg por defecto

    public enum EstadoEnvio {
        Buscando_Transporte,
        Asignado,
        En_Transito,
        Finalizado,
        Cancelado
    }
}

